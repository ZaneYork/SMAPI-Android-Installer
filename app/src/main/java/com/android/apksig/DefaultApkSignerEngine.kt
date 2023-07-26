/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.apksig

import android.os.Build
import com.android.apksig.ApkSignerEngine.InputJarEntryInstructions
import com.android.apksig.ApkSignerEngine.InputJarEntryInstructions.OutputPolicy
import com.android.apksig.ApkSignerEngine.InspectJarEntryRequest
import com.android.apksig.ApkSignerEngine.OutputApkSigningBlockRequest
import com.android.apksig.ApkSignerEngine.OutputApkSigningBlockRequest2
import com.android.apksig.ApkSignerEngine.OutputJarSignatureRequest
import com.android.apksig.DefaultApkSignerEngine.Builder
import com.android.apksig.apk.ApkFormatException
import com.android.apksig.apk.ApkUtils
import com.android.apksig.internal.apk.ApkSigningBlockUtils
import com.android.apksig.internal.apk.ApkSigningBlockUtils.copyWithModifiedCDOffset
import com.android.apksig.internal.apk.ApkSigningBlockUtils.generateApkSigningBlock
import com.android.apksig.internal.apk.ApkSigningBlockUtils.generateApkSigningBlockPadding
import com.android.apksig.internal.apk.SignatureAlgorithm
import com.android.apksig.internal.apk.v1.DigestAlgorithm
import com.android.apksig.internal.apk.v1.V1SchemeSigner
import com.android.apksig.internal.apk.v1.V1SchemeVerifier
import com.android.apksig.internal.apk.v1.V1SchemeVerifier.NamedDigest
import com.android.apksig.internal.apk.v2.V2SchemeSigner
import com.android.apksig.internal.apk.v3.V3SchemeSigner
import com.android.apksig.internal.jar.ManifestParser
import com.android.apksig.internal.util.AndroidSdkVersion
import com.android.apksig.internal.util.Pair
import com.android.apksig.internal.util.TeeDataSink
import com.android.apksig.util.DataSink
import com.android.apksig.util.DataSinks
import com.android.apksig.util.DataSource
import com.android.apksig.util.RunnablesExecutor
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.SignatureException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Arrays
import java.util.Collections

/**
 * Default implementation of [ApkSignerEngine].
 *
 *
 * Use [Builder] to obtain instances of this engine.
 */
class DefaultApkSignerEngine private constructor(
    signerConfigs: List<SignerConfig>,
    minSdkVersion: Int,
    v1SigningEnabled: Boolean,
    v2SigningEnabled: Boolean,
    v3SigningEnabled: Boolean,
    debuggableApkPermitted: Boolean,
    otherSignersSignaturesPreserved: Boolean,
    createdBy: String,
    signingCertificateLineage: SigningCertificateLineage?
) : ApkSignerEngine {
    // IMPLEMENTATION NOTE: This engine generates a signed APK as follows:
    // 1. The engine asks its client to output input JAR entries which are not part of JAR
    //    signature.
    // 2. If JAR signing (v1 signing) is enabled, the engine inspects the output JAR entries to
    //    compute their digests, to be placed into output META-INF/MANIFEST.MF. It also inspects
    //    the contents of input and output META-INF/MANIFEST.MF to borrow the main section of the
    //    file. It does not care about individual (i.e., JAR entry-specific) sections. It then
    //    emits the v1 signature (a set of JAR entries) and asks the client to output them.
    // 3. If APK Signature Scheme v2 (v2 signing) is enabled, the engine emits an APK Signing Block
    //    from outputZipSections() and asks its client to insert this block into the output.
    // 4. If APK Signature Scheme v3 (v3 signing) is enabled, the engine includes it in the APK
    //    Signing BLock output from outputZipSections() and asks its client to insert this block
    //    into the output.  If both v2 and v3 signing is enabled, they are both added to the APK
    //    Signing Block before asking the client to insert it into the output.
    private val mV1SigningEnabled: Boolean
    private val mV2SigningEnabled: Boolean
    private val mV3SigningEnabled: Boolean
    private val mDebuggableApkPermitted: Boolean
    private val mOtherSignersSignaturesPreserved: Boolean
    private val mCreatedBy: String
    private val mSignerConfigs: List<SignerConfig>
    private val mMinSdkVersion: Int
    private val mSigningCertificateLineage: SigningCertificateLineage?
    private lateinit var mV1SignerConfigs: ArrayList<V1SchemeSigner.SignerConfig>
    private var mV1ContentDigestAlgorithm: DigestAlgorithm? = null
    private var mClosed = false
    private var mV1SignaturePending: Boolean

    /**
     * Names of JAR entries which this engine is expected to output as part of v1 signing.
     */
    private var mSignatureExpectedOutputJarEntryNames = emptySet<String>()

    /** Requests for digests of output JAR entries.  */
    private val mOutputJarEntryDigestRequests: MutableMap<String, GetJarEntryDataDigestRequest> =
        HashMap()

    /** Digests of output JAR entries.  */
    private val mOutputJarEntryDigests: MutableMap<String, ByteArray> = HashMap()

    /** Data of JAR entries emitted by this engine as v1 signature.  */
    private val mEmittedSignatureJarEntryData: MutableMap<String, ByteArray> = HashMap()

    /** Requests for data of output JAR entries which comprise the v1 signature.  */
    private val mOutputSignatureJarEntryDataRequests: MutableMap<String, GetJarEntryDataRequest> =
        HashMap()

    /**
     * Request to obtain the data of MANIFEST.MF or `null` if the request hasn't been issued.
     */
    private var mInputJarManifestEntryDataRequest: GetJarEntryDataRequest? = null

    /**
     * Request to obtain the data of AndroidManifest.xml or `null` if the request hasn't been
     * issued.
     */
    private var mOutputAndroidManifestEntryDataRequest: GetJarEntryDataRequest? = null

    /**
     * Whether the package being signed is marked as `android:debuggable` or `null`
     * if this is not yet known.
     */
    private var mDebuggable: Boolean? = null

    /**
     * Request to output the emitted v1 signature or `null` if the request hasn't been issued.
     */
    private var mAddV1SignatureRequest: OutputJarSignatureRequestImpl? = null
    private var mV2SignaturePending: Boolean
    private var mV3SignaturePending: Boolean

    /**
     * Request to output the emitted v2 and/or v3 signature(s) `null` if the request hasn't
     * been issued.
     */
    private var mAddSigningBlockRequest: OutputApkSigningBlockRequestImpl? = null
    private var mExecutor = RunnablesExecutor.SINGLE_THREADED

    init {
        require(!signerConfigs.isEmpty()) { "At least one signer config must be provided" }
        if (otherSignersSignaturesPreserved) {
            throw UnsupportedOperationException(
                "Preserving other signer's signatures is not yet implemented"
            )
        }
        mV1SigningEnabled = v1SigningEnabled
        mV2SigningEnabled = v2SigningEnabled
        mV3SigningEnabled = v3SigningEnabled
        mV1SignaturePending = v1SigningEnabled
        mV2SignaturePending = v2SigningEnabled
        mV3SignaturePending = v3SigningEnabled
        mDebuggableApkPermitted = debuggableApkPermitted
        mOtherSignersSignaturesPreserved = otherSignersSignaturesPreserved
        mCreatedBy = createdBy
        mSignerConfigs = signerConfigs
        mMinSdkVersion = minSdkVersion
        mSigningCertificateLineage = signingCertificateLineage
        if (v1SigningEnabled) {
            if (v3SigningEnabled) {

                // v3 signing only supports single signers, of which the oldest (first) will be the
                // one to use for v1 and v2 signing
                val oldestConfig = signerConfigs[0]

                // in the event of signing certificate changes, make sure we have the oldest in the
                // signing history to sign with v1
                if (signingCertificateLineage != null) {
                    val subLineage = signingCertificateLineage.getSubLineage(
                        oldestConfig.certificates[0]
                    )
                    require(subLineage.size() == 1) {
                        ("v1 signing enabled but the oldest signer in the " + "SigningCertificateLineage is missing.  Please provide the oldest" + " signer to enable v1 signing")
                    }
                }
                createV1SignerConfigs(listOf(oldestConfig), minSdkVersion)
            } else {
                createV1SignerConfigs(signerConfigs, minSdkVersion)
            }
        }
    }

    @Throws(InvalidKeyException::class)
    private fun createV1SignerConfigs(signerConfigs: List<SignerConfig>, minSdkVersion: Int) {
        mV1SignerConfigs = ArrayList(signerConfigs.size)
        val v1SignerNameToSignerIndex: MutableMap<String, Int> = HashMap(signerConfigs.size)
        var v1ContentDigestAlgorithm: DigestAlgorithm? = null
        for (i in signerConfigs.indices) {
            val signerConfig = signerConfigs[i]
            val certificates = signerConfig.certificates
            val publicKey = certificates[0].publicKey
            val v1SignerName = V1SchemeSigner.getSafeSignerName(signerConfig.name)
            // Check whether the signer's name is unique among all v1 signers
            val indexOfOtherSignerWithSameName = v1SignerNameToSignerIndex.put(v1SignerName, i)
            require(indexOfOtherSignerWithSameName == null) {
                ("Signers #" + (indexOfOtherSignerWithSameName!! + 1) + " and #" + (i + 1) + " have the same name: " + v1SignerName + ". v1 signer names must be unique")
            }
            val v1SignatureDigestAlgorithm = V1SchemeSigner.getSuggestedSignatureDigestAlgorithm(
                publicKey, minSdkVersion
            )
            val v1SignerConfig = V1SchemeSigner.SignerConfig()
            v1SignerConfig.name = v1SignerName
            v1SignerConfig.privateKey = signerConfig.privateKey
            v1SignerConfig.certificates = certificates
            v1SignerConfig.signatureDigestAlgorithm = v1SignatureDigestAlgorithm
            // For digesting contents of APK entries and of MANIFEST.MF, pick the algorithm
            // of comparable strength to the digest algorithm used for computing the signature.
            // When there are multiple signers, pick the strongest digest algorithm out of their
            // signature digest algorithms. This avoids reducing the digest strength used by any
            // of the signers to protect APK contents.
            if (v1ContentDigestAlgorithm == null) {
                v1ContentDigestAlgorithm = v1SignatureDigestAlgorithm
            } else {
                if (DigestAlgorithm.BY_STRENGTH_COMPARATOR.compare(
                        v1SignatureDigestAlgorithm, v1ContentDigestAlgorithm
                    ) > 0
                ) {
                    v1ContentDigestAlgorithm = v1SignatureDigestAlgorithm
                }
            }
            mV1SignerConfigs.add(v1SignerConfig)
        }
        mV1ContentDigestAlgorithm = v1ContentDigestAlgorithm
        mSignatureExpectedOutputJarEntryNames = V1SchemeSigner.getOutputEntryNames(mV1SignerConfigs)
    }

    @Throws(InvalidKeyException::class)
    private fun createV2SignerConfigs(
        apkSigningBlockPaddingSupported: Boolean
    ): List<ApkSigningBlockUtils.SignerConfig> {
        return if (mV3SigningEnabled) {

            // v3 signing only supports single signers, of which the oldest (first) will be the one
            // to use for v1 and v2 signing
            val signerConfig: MutableList<ApkSigningBlockUtils.SignerConfig> = ArrayList()
            val oldestConfig = mSignerConfigs[0]

            // first make sure that if we have signing certificate history that the oldest signer
            // corresponds to the oldest ancestor
            if (mSigningCertificateLineage != null) {
                val subLineage =
                    mSigningCertificateLineage.getSubLineage(oldestConfig.certificates[0])
                if (subLineage.size() != 1) {
                    throw IllegalArgumentException(
                        "v2 signing enabled but the oldest signer in" + " the SigningCertificateLineage is missing.  Please provide" + " the oldest signer to enable v2 signing."
                    )
                }
            }
            signerConfig.add(
                createSigningBlockSignerConfig(
                    mSignerConfigs[0],
                    apkSigningBlockPaddingSupported,
                    ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V2
                )
            )
            signerConfig
        } else {
            createSigningBlockSignerConfigs(
                apkSigningBlockPaddingSupported,
                ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V2
            )
        }
    }

    @Throws(InvalidKeyException::class)
    private fun createV3SignerConfigs(
        apkSigningBlockPaddingSupported: Boolean
    ): List<ApkSigningBlockUtils.SignerConfig> {
        val rawConfigs = createSigningBlockSignerConfigs(
            apkSigningBlockPaddingSupported, ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V3
        )
        val processedConfigs: MutableList<ApkSigningBlockUtils.SignerConfig> = ArrayList()

        // we have our configs, now touch them up to appropriately cover all SDK levels since APK
        // signature scheme v3 was introduced
        var currentMinSdk = Int.MAX_VALUE
        for (i in rawConfigs.indices.reversed()) {
            val config = rawConfigs[i]
            if (config.signatureAlgorithms == null) {
                // no valid algorithm was found for this signer, and we haven't yet covered all
                // platform versions, something's wrong
                val keyAlgorithm = config.certificates!![0].publicKey.algorithm
                throw InvalidKeyException(
                    "Unsupported key algorithm " + keyAlgorithm + " is " + "not supported for APK Signature Scheme v3 signing"
                )
            }
            if (i == rawConfigs.size - 1) {
                // first go through the loop, config should support all future platform versions.
                // this assumes we don't deprecate support for signers in the future.  If we do,
                // this needs to change
                config.maxSdkVersion = Int.MAX_VALUE
            } else {
                // otherwise, we only want to use this signer up to the minimum platform version
                // on which a newer one is acceptable
                config.maxSdkVersion = currentMinSdk - 1
            }
            config.minSdkVersion = getMinSdkFromV3SignatureAlgorithms(config.signatureAlgorithms)
            if (mSigningCertificateLineage != null) {
                config.mSigningCertificateLineage =
                    mSigningCertificateLineage.getSubLineage(config.certificates!![0])
            }
            // we know that this config will be used, so add it to our result, order doesn't matter
            // at this point (and likely only one will be needed
            processedConfigs.add(config)
            currentMinSdk = config.minSdkVersion
            if (currentMinSdk <= mMinSdkVersion || currentMinSdk <= AndroidSdkVersion.P) {
                // this satisfies all we need, stop here
                break
            }
        }
        if (currentMinSdk > AndroidSdkVersion.P && currentMinSdk > mMinSdkVersion) {
            // we can't cover all desired SDK versions, abort
            throw InvalidKeyException(
                "Provided key algorithms not supported on all desired " + "Android SDK versions"
            )
        }
        return processedConfigs
    }

    private fun getMinSdkFromV3SignatureAlgorithms(algorithms: List<SignatureAlgorithm>?): Int {
        var min = Int.MAX_VALUE
        for (algorithm in algorithms!!) {
            val current = algorithm.minSdkVersion
            if (current < min) {
                min = if (current <= mMinSdkVersion || current <= AndroidSdkVersion.P) {
                    // this algorithm satisfies all of our needs, no need to keep looking
                    return current
                } else {
                    current
                }
            }
        }
        return min
    }

    @Throws(InvalidKeyException::class)
    private fun createSigningBlockSignerConfigs(
        apkSigningBlockPaddingSupported: Boolean, schemeId: Int
    ): List<ApkSigningBlockUtils.SignerConfig> {
        val signerConfigs: MutableList<ApkSigningBlockUtils.SignerConfig> =
            ArrayList(mSignerConfigs.size)
        for (i in mSignerConfigs.indices) {
            val signerConfig = mSignerConfigs[i]
            signerConfigs.add(
                createSigningBlockSignerConfig(
                    signerConfig, apkSigningBlockPaddingSupported, schemeId
                )
            )
        }
        return signerConfigs
    }

    @Throws(InvalidKeyException::class)
    private fun createSigningBlockSignerConfig(
        signerConfig: SignerConfig, apkSigningBlockPaddingSupported: Boolean, schemeId: Int
    ): ApkSigningBlockUtils.SignerConfig {
        val certificates = signerConfig.certificates
        val publicKey = certificates[0].publicKey
        val newSignerConfig = ApkSigningBlockUtils.SignerConfig()
        newSignerConfig.privateKey = signerConfig.privateKey
        newSignerConfig.certificates = certificates
        when (schemeId) {
            ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V2 -> newSignerConfig.signatureAlgorithms =
                V2SchemeSigner.getSuggestedSignatureAlgorithms(
                    publicKey, mMinSdkVersion, apkSigningBlockPaddingSupported
                )

            ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V3 -> try {
                newSignerConfig.signatureAlgorithms =
                    V3SchemeSigner.getSuggestedSignatureAlgorithms(
                        publicKey, mMinSdkVersion, apkSigningBlockPaddingSupported
                    )
            } catch (e: InvalidKeyException) {

                // It is possible for a signer used for v1/v2 signing to not be allowed for use
                // with v3 signing.  This is ok as long as there exists a more recent v3 signer
                // that covers all supported platform versions.  Populate signatureAlgorithm
                // with null, it will be cleaned-up in a later step.
                newSignerConfig.signatureAlgorithms = null
            }

            else -> throw IllegalArgumentException("Unknown APK Signature Scheme ID requested")
        }
        return newSignerConfig
    }

    private fun isDebuggable(entryName: String): Boolean {
        return (mDebuggableApkPermitted || ApkUtils.ANDROID_MANIFEST_ZIP_ENTRY_NAME != entryName)
    }

    /**
     * Initializes DefaultApkSignerEngine with the existing MANIFEST.MF. This reads existing digests
     * from the MANIFEST.MF file (they are assumed correct) and stores them for the final signature
     * without recalculation. This step has a significant performance benefit in case of incremental
     * build.
     *
     * This method extracts and stored computed digest for every entry that it would compute it for
     * in the [.outputJarEntry] method
     *
     * @param manifestBytes raw representation of MANIFEST.MF file
     * @param entryNames a set of expected entries names
     * @return set of entry names which were processed by the engine during the initialization, a
     * subset of entryNames
     */
    override fun initWith(manifestBytes: ByteArray, entryNames: Set<String>): Set<String> {
        val dummyResult = V1SchemeVerifier.Result()
        val sections = V1SchemeVerifier.parseManifest(manifestBytes, entryNames, dummyResult)
        val alg = V1SchemeSigner.getJcaMessageDigestAlgorithm(mV1ContentDigestAlgorithm)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sections.second.entries.parallelStream().filter { entry ->
                (V1SchemeSigner.isJarEntryDigestNeededInManifest(entry.key) && isDebuggable(
                    entry.key
                ) && entryNames.contains(entry.key))
            }.forEach { entry ->
                val extractedDigest = V1SchemeVerifier.getDigestsToVerify(
                    entry.value, "-Digest", mMinSdkVersion, Int.MAX_VALUE
                ).firstOrNull { d -> (d.jcaDigestAlgorithm == alg) }
                extractedDigest?.let { namedDigest ->
                    mOutputJarEntryDigests[entry.key] = namedDigest.digest
                }
            }
        } else {
            sections.second.entries.asSequence().filter { entry ->
                (V1SchemeSigner.isJarEntryDigestNeededInManifest(entry.key) && isDebuggable(
                    entry.key
                ) && entryNames.contains(entry.key))
            }.forEach { entry ->
                val extractedDigest = V1SchemeVerifier.getDigestsToVerify(
                    entry.value, "-Digest", mMinSdkVersion, Int.MAX_VALUE
                ).firstOrNull { d -> (d.jcaDigestAlgorithm == alg) }
                extractedDigest?.let { namedDigest ->
                    mOutputJarEntryDigests[entry.key] = namedDigest.digest
                }
            }
        }
        return mOutputJarEntryDigests.keys
    }

    override fun setExecutor(executor: RunnablesExecutor) {
        mExecutor = executor
    }

    override fun inputApkSigningBlock(apkSigningBlock: DataSource) {
        checkNotClosed()
        if (apkSigningBlock.size() == 0L) {
            return
        }
        if (mOtherSignersSignaturesPreserved) {
            // TODO: Preserve blocks other than APK Signature Scheme v2 blocks of signers configured
            // in this engine.
            return
        }
        // TODO: Preserve blocks other than APK Signature Scheme v2 blocks.
    }

    override fun inputJarEntry(entryName: String): InputJarEntryInstructions {
        checkNotClosed()
        val outputPolicy = getInputJarEntryOutputPolicy(entryName)
        return when (outputPolicy) {
            OutputPolicy.SKIP -> InputJarEntryInstructions(OutputPolicy.SKIP)
            OutputPolicy.OUTPUT -> InputJarEntryInstructions(OutputPolicy.OUTPUT)
            OutputPolicy.OUTPUT_BY_ENGINE -> {
                if (V1SchemeSigner.MANIFEST_ENTRY_NAME == entryName) {
                    // We copy the main section of the JAR manifest from input to output. Thus, this
                    // invalidates v1 signature and we need to see the entry's data.
                    mInputJarManifestEntryDataRequest = GetJarEntryDataRequest(entryName)
                    return InputJarEntryInstructions(
                        OutputPolicy.OUTPUT_BY_ENGINE, mInputJarManifestEntryDataRequest
                    )
                }
                InputJarEntryInstructions(
                    OutputPolicy.OUTPUT_BY_ENGINE
                )
            }

        }
    }

    override fun outputJarEntry(entryName: String): InspectJarEntryRequest? {
        checkNotClosed()
        invalidateV2Signature()
        if (!isDebuggable(entryName)) {
            forgetOutputApkDebuggableStatus()
        }
        if (!mV1SigningEnabled) {
            // No need to inspect JAR entries when v1 signing is not enabled.
            if (!isDebuggable(entryName)) {
                // To reject debuggable APKs we need to inspect the APK's AndroidManifest.xml to
                // check whether it declares that the APK is debuggable
                mOutputAndroidManifestEntryDataRequest = GetJarEntryDataRequest(entryName)
                return mOutputAndroidManifestEntryDataRequest!!
            }
            return null
        }
        // v1 signing is enabled
        if (V1SchemeSigner.isJarEntryDigestNeededInManifest(entryName)) {
            // This entry is covered by v1 signature. We thus need to inspect the entry's data to
            // compute its digest(s) for v1 signature.

            // TODO: Handle the case where other signer's v1 signatures are present and need to be
            // preserved. In that scenario we can't modify MANIFEST.MF and add/remove JAR entries
            // covered by v1 signature.
            invalidateV1Signature()
            val dataDigestRequest = GetJarEntryDataDigestRequest(
                entryName, V1SchemeSigner.getJcaMessageDigestAlgorithm(mV1ContentDigestAlgorithm)
            )
            mOutputJarEntryDigestRequests[entryName] = dataDigestRequest
            mOutputJarEntryDigests.remove(entryName)
            if (!mDebuggableApkPermitted && ApkUtils.ANDROID_MANIFEST_ZIP_ENTRY_NAME == entryName) {
                // To reject debuggable APKs we need to inspect the APK's AndroidManifest.xml to
                // check whether it declares that the APK is debuggable
                mOutputAndroidManifestEntryDataRequest = GetJarEntryDataRequest(entryName)
                return CompoundInspectJarEntryRequest(
                    entryName, mOutputAndroidManifestEntryDataRequest!!, dataDigestRequest
                )
            }
            return dataDigestRequest
        }
        if (mSignatureExpectedOutputJarEntryNames.contains(entryName)) {
            // This entry is part of v1 signature generated by this engine. We need to check whether
            // the entry's data is as output by the engine.
            invalidateV1Signature()
            val dataRequest: GetJarEntryDataRequest?
            if (V1SchemeSigner.MANIFEST_ENTRY_NAME == entryName) {
                dataRequest = GetJarEntryDataRequest(entryName)
                mInputJarManifestEntryDataRequest = dataRequest
            } else {
                // If this entry is part of v1 signature which has been emitted by this engine,
                // check whether the output entry's data matches what the engine emitted.
                dataRequest =
                    if (mEmittedSignatureJarEntryData.containsKey(entryName)) GetJarEntryDataRequest(
                        entryName
                    ) else null
            }
            if (dataRequest != null) {
                mOutputSignatureJarEntryDataRequests[entryName] = dataRequest
            }
            return dataRequest
        }

        // This entry is not covered by v1 signature and isn't part of v1 signature.
        return null
    }

    override fun inputJarEntryRemoved(entryName: String): OutputPolicy {
        checkNotClosed()
        return getInputJarEntryOutputPolicy(entryName)
    }

    override fun outputJarEntryRemoved(entryName: String) {
        checkNotClosed()
        invalidateV2Signature()
        if (!mV1SigningEnabled) {
            return
        }
        if (V1SchemeSigner.isJarEntryDigestNeededInManifest(entryName)) {
            // This entry is covered by v1 signature.
            invalidateV1Signature()
            mOutputJarEntryDigests.remove(entryName)
            mOutputJarEntryDigestRequests.remove(entryName)
            mOutputSignatureJarEntryDataRequests.remove(entryName)
            return
        }
        if (mSignatureExpectedOutputJarEntryNames.contains(entryName)) {
            // This entry is part of the v1 signature generated by this engine.
            invalidateV1Signature()
            return
        }
    }

    @Throws(
        ApkFormatException::class,
        InvalidKeyException::class,
        SignatureException::class,
        NoSuchAlgorithmException::class
    )
    override fun outputJarEntries(): OutputJarSignatureRequest? {
        checkNotClosed()
        if (!mV1SignaturePending) {
            return null
        }
        check(
            !(mInputJarManifestEntryDataRequest != null && !mInputJarManifestEntryDataRequest!!.isDone)
        ) {
            ("Still waiting to inspect input APK's " + mInputJarManifestEntryDataRequest!!.entryName)
        }
        for (digestRequest in mOutputJarEntryDigestRequests.values) {
            val entryName = digestRequest.entryName
            check(digestRequest.isDone) { "Still waiting to inspect output APK's $entryName" }
            mOutputJarEntryDigests[entryName] = digestRequest.digest
        }
        mOutputJarEntryDigestRequests.clear()
        for (dataRequest in mOutputSignatureJarEntryDataRequests.values) {
            check(dataRequest.isDone) { "Still waiting to inspect output APK's " + dataRequest.entryName }
        }
        val apkSigningSchemeIds: MutableList<Int> = ArrayList()
        if (mV2SigningEnabled) {
            apkSigningSchemeIds.add(ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V2)
        }
        if (mV3SigningEnabled) {
            apkSigningSchemeIds.add(ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V3)
        }
        val inputJarManifest: ByteArray? = mInputJarManifestEntryDataRequest?.data

        // Check whether the most recently used signature (if present) is still fine.
        checkOutputApkNotDebuggableIfDebuggableMustBeRejected()
        val signatureZipEntries: MutableList<Pair<String, ByteArray>>
        if (mAddV1SignatureRequest == null || !mAddV1SignatureRequest!!.isDone) {
            signatureZipEntries = try {
                V1SchemeSigner.sign(
                    mV1SignerConfigs,
                    mV1ContentDigestAlgorithm,
                    mOutputJarEntryDigests,
                    apkSigningSchemeIds,
                    inputJarManifest,
                    mCreatedBy
                )
            } catch (e: CertificateException) {
                throw SignatureException("Failed to generate v1 signature", e)
            }
        } else {
            val newManifest = V1SchemeSigner.generateManifestFile(
                mV1ContentDigestAlgorithm, mOutputJarEntryDigests, inputJarManifest
            )
            val emittedSignatureManifest =
                mEmittedSignatureJarEntryData[V1SchemeSigner.MANIFEST_ENTRY_NAME]
            if (!Arrays.equals(newManifest.contents, emittedSignatureManifest)) {
                // Emitted v1 signature is no longer valid.
                signatureZipEntries = try {
                    V1SchemeSigner.signManifest(
                        mV1SignerConfigs,
                        mV1ContentDigestAlgorithm,
                        apkSigningSchemeIds,
                        mCreatedBy,
                        newManifest
                    )
                } catch (e: CertificateException) {
                    throw SignatureException("Failed to generate v1 signature", e)
                }
            } else {
                // Emitted v1 signature is still valid. Check whether the signature is there in the
                // output.
                signatureZipEntries = ArrayList()
                for ((entryName, expectedData) in mEmittedSignatureJarEntryData) {
                    val actualDataRequest = mOutputSignatureJarEntryDataRequests[entryName]
                    if (actualDataRequest == null) {
                        // This signature entry hasn't been output.
                        signatureZipEntries.add(Pair.of(entryName, expectedData))
                        continue
                    }
                    val actualData: ByteArray = actualDataRequest.data
                    if (!Arrays.equals(expectedData, actualData)) {
                        signatureZipEntries.add(Pair.of(entryName, expectedData))
                    }
                }
                if (signatureZipEntries.isEmpty()) {
                    // v1 signature in the output is valid
                    return null
                }
                // v1 signature in the output is not valid.
            }
        }
        if (signatureZipEntries.isEmpty()) {
            // v1 signature in the output is valid
            mV1SignaturePending = false
            return null
        }
        val sigEntries: MutableList<OutputJarSignatureRequest.JarEntry> =
            ArrayList(signatureZipEntries.size)
        for (entry in signatureZipEntries) {
            val entryName = entry.first
            val entryData = entry.second
            sigEntries.add(OutputJarSignatureRequest.JarEntry(entryName, entryData))
            mEmittedSignatureJarEntryData[entryName] = entryData
        }
        mAddV1SignatureRequest = OutputJarSignatureRequestImpl(sigEntries)
        return mAddV1SignatureRequest!!
    }

    @Deprecated("")
    @Throws(
        IOException::class,
        InvalidKeyException::class,
        SignatureException::class,
        NoSuchAlgorithmException::class
    )
    override fun outputZipSections(
        zipEntries: DataSource, zipCentralDirectory: DataSource, zipEocd: DataSource
    ): OutputApkSigningBlockRequest {
        return outputZipSectionsInternal(zipEntries, zipCentralDirectory, zipEocd, false)!!
    }

    @Throws(
        IOException::class,
        InvalidKeyException::class,
        SignatureException::class,
        NoSuchAlgorithmException::class
    )
    override fun outputZipSections2(
        zipEntries: DataSource, zipCentralDirectory: DataSource, zipEocd: DataSource
    ): OutputApkSigningBlockRequest2 {
        return outputZipSectionsInternal(zipEntries, zipCentralDirectory, zipEocd, true)!!
    }

    @Throws(
        IOException::class,
        InvalidKeyException::class,
        SignatureException::class,
        NoSuchAlgorithmException::class
    )
    private fun outputZipSectionsInternal(
        zipEntries: DataSource,
        zipCentralDirectory: DataSource,
        zipEocd: DataSource,
        apkSigningBlockPaddingSupported: Boolean
    ): OutputApkSigningBlockRequestImpl? {
        checkNotClosed()
        checkV1SigningDoneIfEnabled()
        if (!mV2SigningEnabled && !mV3SigningEnabled) {
            return null
        }
        checkOutputApkNotDebuggableIfDebuggableMustBeRejected()

        // adjust to proper padding
        val paddingPair = generateApkSigningBlockPadding(
            zipEntries, apkSigningBlockPaddingSupported
        )
        val beforeCentralDir = paddingPair.first
        val padSizeBeforeApkSigningBlock = paddingPair.second
        val eocd = copyWithModifiedCDOffset(beforeCentralDir, zipEocd)
        val signingSchemeBlocks: MutableList<Pair<ByteArray, Int>> = ArrayList()

        // create APK Signature Scheme V2 Signature if requested
        if (mV2SigningEnabled) {
            invalidateV2Signature()
            val v2SignerConfigs = createV2SignerConfigs(apkSigningBlockPaddingSupported)
            signingSchemeBlocks.add(
                V2SchemeSigner.generateApkSignatureSchemeV2Block(
                    mExecutor,
                    beforeCentralDir,
                    zipCentralDirectory,
                    eocd,
                    v2SignerConfigs,
                    mV3SigningEnabled
                )
            )
        }
        if (mV3SigningEnabled) {
            invalidateV3Signature()
            val v3SignerConfigs = createV3SignerConfigs(apkSigningBlockPaddingSupported)
            signingSchemeBlocks.add(
                V3SchemeSigner.generateApkSignatureSchemeV3Block(
                    mExecutor, beforeCentralDir, zipCentralDirectory, eocd, v3SignerConfigs
                )
            )
        }

        // create APK Signing Block with v2 and/or v3 blocks
        val apkSigningBlock = generateApkSigningBlock(signingSchemeBlocks)
        mAddSigningBlockRequest = OutputApkSigningBlockRequestImpl(
            apkSigningBlock, padSizeBeforeApkSigningBlock
        )
        return mAddSigningBlockRequest
    }

    override fun outputDone() {
        checkNotClosed()
        checkV1SigningDoneIfEnabled()
        checkSigningBlockDoneIfEnabled()
    }

    override fun close() {
        mClosed = true
        mAddV1SignatureRequest = null
        mInputJarManifestEntryDataRequest = null
        mOutputAndroidManifestEntryDataRequest = null
        mDebuggable = null
        mOutputJarEntryDigestRequests.clear()
        mOutputJarEntryDigests.clear()
        mEmittedSignatureJarEntryData.clear()
        mOutputSignatureJarEntryDataRequests.clear()
        mAddSigningBlockRequest = null
    }

    private fun invalidateV1Signature() {
        if (mV1SigningEnabled) {
            mV1SignaturePending = true
        }
        invalidateV2Signature()
    }

    private fun invalidateV2Signature() {
        if (mV2SigningEnabled) {
            mV2SignaturePending = true
            mAddSigningBlockRequest = null
        }
    }

    private fun invalidateV3Signature() {
        if (mV3SigningEnabled) {
            mV3SignaturePending = true
            mAddSigningBlockRequest = null
        }
    }

    private fun checkNotClosed() {
        check(!mClosed) { "Engine closed" }
    }

    private fun checkV1SigningDoneIfEnabled() {
        if (!mV1SignaturePending) {
            return
        }
        checkNotNull(mAddV1SignatureRequest) { "v1 signature (JAR signature) not yet generated. Skipped outputJarEntries()?" }
        check(mAddV1SignatureRequest!!.isDone) {
            ("v1 signature (JAR signature) addition requested by outputJarEntries() hasn't" + " been fulfilled")
        }
        for ((entryName, expectedData) in mEmittedSignatureJarEntryData) {
            val actualDataRequest = mOutputSignatureJarEntryDataRequests[entryName]
            checkNotNull(actualDataRequest) {
                ("APK entry " + entryName + " not yet output despite this having been" + " requested")
            }
            check(actualDataRequest.isDone) { "Still waiting to inspect output APK's $entryName" }
            val actualData: ByteArray = actualDataRequest.data
            check(
                Arrays.equals(
                    expectedData, actualData
                )
            ) { "Output APK entry $entryName data differs from what was requested" }
        }
        mV1SignaturePending = false
    }

    private fun checkSigningBlockDoneIfEnabled() {
        if (!mV2SignaturePending && !mV3SignaturePending) {
            return
        }
        checkNotNull(mAddSigningBlockRequest) { "Signed APK Signing BLock not yet generated. Skipped outputZipSections()?" }
        check(mAddSigningBlockRequest!!.isDone) {
            ("APK Signing Block addition of signature(s) requested by" + " outputZipSections() hasn't been fulfilled yet")
        }
        mAddSigningBlockRequest = null
        mV2SignaturePending = false
        mV3SignaturePending = false
    }

    @Throws(SignatureException::class)
    private fun checkOutputApkNotDebuggableIfDebuggableMustBeRejected() {
        if (mDebuggableApkPermitted) {
            return
        }
        try {
            if (isOutputApkDebuggable) {
                throw SignatureException(
                    "APK is debuggable (see android:debuggable attribute) and this engine is" + " configured to refuse to sign debuggable APKs"
                )
            }
        } catch (e: ApkFormatException) {
            throw SignatureException("Failed to determine whether the APK is debuggable", e)
        }
    }

    /**
     * Returns whether the output APK is debuggable according to its
     * `android:debuggable` declaration.
     */
    @get:Throws(ApkFormatException::class)
    private val isOutputApkDebuggable: Boolean
        get() {
            if (mDebuggable != null) {
                return mDebuggable as Boolean
            }
            checkNotNull(mOutputAndroidManifestEntryDataRequest) {
                ("Cannot determine debuggable status of output APK because " + ApkUtils.ANDROID_MANIFEST_ZIP_ENTRY_NAME + " entry contents have not yet been requested")
            }
            check(mOutputAndroidManifestEntryDataRequest!!.isDone) {
                ("Still waiting to inspect output APK's " + mOutputAndroidManifestEntryDataRequest!!.entryName)
            }
            mDebuggable = ApkUtils.getDebuggableFromBinaryAndroidManifest(
                ByteBuffer.wrap(mOutputAndroidManifestEntryDataRequest!!.data)
            )
            return mDebuggable!!
        }

    private fun forgetOutputApkDebuggableStatus() {
        mDebuggable = null
    }

    /**
     * Returns the output policy for the provided input JAR entry.
     */
    private fun getInputJarEntryOutputPolicy(entryName: String): OutputPolicy {
        if (mSignatureExpectedOutputJarEntryNames.contains(entryName)) {
            return OutputPolicy.OUTPUT_BY_ENGINE
        }
        return if (((mOtherSignersSignaturesPreserved) || (V1SchemeSigner.isJarEntryDigestNeededInManifest(
                entryName
            )))
        ) {
            OutputPolicy.OUTPUT
        } else OutputPolicy.SKIP
    }

    private class OutputJarSignatureRequestImpl(additionalZipEntries: List<OutputJarSignatureRequest.JarEntry>) :
        OutputJarSignatureRequest {
        private val mAdditionalJarEntries: List<OutputJarSignatureRequest.JarEntry>

        @Volatile
        var isDone = false

        init {
            mAdditionalJarEntries = Collections.unmodifiableList(ArrayList(additionalZipEntries))
        }

        override fun getAdditionalJarEntries(): List<OutputJarSignatureRequest.JarEntry> {
            return mAdditionalJarEntries
        }

        override fun done() {
            isDone = true
        }
    }

    private class OutputApkSigningBlockRequestImpl(apkSigingBlock: ByteArray, paddingBefore: Int) :
        OutputApkSigningBlockRequest, OutputApkSigningBlockRequest2 {
        private val mApkSigningBlock: ByteArray
        private val mPaddingBeforeApkSigningBlock: Int

        @Volatile
        var isDone = false

        init {
            mApkSigningBlock = apkSigingBlock.clone()
            mPaddingBeforeApkSigningBlock = paddingBefore
        }

        override fun getApkSigningBlock(): ByteArray {
            return mApkSigningBlock.clone()
        }

        override fun done() {
            isDone = true
        }

        override fun getPaddingSizeBeforeApkSigningBlock(): Int {
            return mPaddingBeforeApkSigningBlock
        }
    }

    /**
     * JAR entry inspection request which obtain the entry's uncompressed data.
     */
    private class GetJarEntryDataRequest(private val mEntryName: String) : InspectJarEntryRequest {
        private val mLock = Any()
        private var mDone = false
        private var mDataSink: DataSink? = null
        private var mDataSinkBuf: ByteArrayOutputStream? = null
        override fun getEntryName(): String {
            return mEntryName
        }

        override fun getDataSink(): DataSink {
            synchronized(mLock) {
                checkNotDone()
                if (mDataSinkBuf == null) {
                    mDataSinkBuf = ByteArrayOutputStream()
                }
                if (mDataSink == null) {
                    mDataSink = DataSinks.asDataSink(mDataSinkBuf)
                }
                return (mDataSink)!!
            }
        }

        override fun done() {
            synchronized(mLock) {
                if (mDone) {
                    return
                }
                mDone = true
            }
        }

        val isDone: Boolean
            get() {
                synchronized(mLock) { return mDone }
            }

        @Throws(IllegalStateException::class)
        private fun checkNotDone() {
            synchronized(mLock) {
                if (mDone) {
                    throw IllegalStateException("Already done")
                }
            }
        }

        val data: ByteArray
            get() {
                synchronized(mLock) {
                    if (!mDone) {
                        throw IllegalStateException("Not yet done")
                    }
                    return if ((mDataSinkBuf != null)) mDataSinkBuf!!.toByteArray() else ByteArray(0)
                }
            }
    }

    /**
     * JAR entry inspection request which obtains the digest of the entry's uncompressed data.
     */
    private class GetJarEntryDataDigestRequest(
        private val mEntryName: String, private val mJcaDigestAlgorithm: String
    ) : InspectJarEntryRequest {
        private val mLock = Any()
        private var mDone = false
        private var mDataSink: DataSink? = null
        private var mMessageDigest: MessageDigest? = null
        private var mDigest: ByteArray? = null
        override fun getEntryName(): String {
            return mEntryName
        }

        override fun getDataSink(): DataSink {
            synchronized(mLock) {
                checkNotDone()
                if (mDataSink == null) {
                    mDataSink = DataSinks.asDataSink(messageDigest)
                }
                return (mDataSink)!!
            }
        }

        private val messageDigest: MessageDigest?
            get() {
                synchronized(mLock) {
                    if (mMessageDigest == null) {
                        try {
                            mMessageDigest = MessageDigest.getInstance(mJcaDigestAlgorithm)
                        } catch (e: NoSuchAlgorithmException) {
                            throw RuntimeException(
                                mJcaDigestAlgorithm + " MessageDigest not available", e
                            )
                        }
                    }
                    return mMessageDigest
                }
            }

        override fun done() {
            synchronized(mLock) {
                if (mDone) {
                    return
                }
                mDone = true
                mDigest = messageDigest!!.digest()
                mMessageDigest = null
                mDataSink = null
            }
        }

        val isDone: Boolean
            get() {
                synchronized(mLock) { return mDone }
            }

        @Throws(IllegalStateException::class)
        private fun checkNotDone() {
            synchronized(mLock) {
                if (mDone) {
                    throw IllegalStateException("Already done")
                }
            }
        }

        val digest: ByteArray
            get() {
                synchronized(mLock) {
                    if (!mDone) {
                        throw IllegalStateException("Not yet done")
                    }
                    return mDigest!!.clone()
                }
            }
    }

    /**
     * JAR entry inspection request which transparently satisfies multiple such requests.
     */
    private class CompoundInspectJarEntryRequest(
        private val mEntryName: String, vararg requests: InspectJarEntryRequest
    ) : InspectJarEntryRequest {
        private val mRequests: Array<out InspectJarEntryRequest>
        private val mLock = Any()
        private var mSink: DataSink? = null

        init {
            mRequests = requests
        }

        override fun getEntryName(): String {
            return mEntryName
        }

        override fun getDataSink(): DataSink {
            synchronized(mLock) {
                if (mSink == null) {
                    val sinks: Array<DataSink?> = arrayOfNulls(mRequests.size)
                    for (i in sinks.indices) {
                        sinks[i] = mRequests.get(i).getDataSink()
                    }
                    mSink = TeeDataSink(sinks)
                }
                return mSink!!
            }
        }

        override fun done() {
            for (request in mRequests) {
                request.done()
            }
        }
    }

    /**
     * Configuration of a signer.
     *
     *
     * Use [Builder] to obtain configuration instances.
     */
    class SignerConfig private constructor(
        /**
         * Returns the name of this signer.
         */
        val name: String,
        /**
         * Returns the signing key of this signer.
         */
        val privateKey: PrivateKey, certificates: List<X509Certificate>
    ) {

        /**
         * Returns the certificate(s) of this signer. The first certificate's public key corresponds
         * to this signer's private key.
         */
        val certificates: List<X509Certificate>

        init {
            this.certificates = Collections.unmodifiableList(ArrayList(certificates))
        }

        /**
         * Builder of [SignerConfig] instances.
         */
        class Builder(
            name: String, privateKey: PrivateKey, certificates: List<X509Certificate>
        ) {
            private val mName: String
            private val mPrivateKey: PrivateKey
            private val mCertificates: List<X509Certificate>

            /**
             * Constructs a new `Builder`.
             *
             * @param name signer's name. The name is reflected in the name of files comprising the
             * JAR signature of the APK.
             * @param privateKey signing key
             * @param certificates list of one or more X.509 certificates. The subject public key of
             * the first certificate must correspond to the `privateKey`.
             */
            init {
                require(!name.isEmpty()) { "Empty name" }
                mName = name
                mPrivateKey = privateKey
                mCertificates = ArrayList(certificates)
            }

            /**
             * Returns a new `SignerConfig` instance configured based on the configuration of
             * this builder.
             */
            fun build(): SignerConfig {
                return SignerConfig(
                    mName, mPrivateKey, mCertificates
                )
            }
        }
    }

    /**
     * Builder of [DefaultApkSignerEngine] instances.
     */
    class Builder(
        signerConfigs: List<SignerConfig>, minSdkVersion: Int
    ) {
        private var mSignerConfigs: List<SignerConfig>
        private val mMinSdkVersion: Int
        private var mV1SigningEnabled = true
        private var mV2SigningEnabled = true
        private var mV3SigningEnabled = true
        private var mDebuggableApkPermitted = true
        private var mOtherSignersSignaturesPreserved = false
        private var mCreatedBy = "1.0 (Android)"
        private var mSigningCertificateLineage: SigningCertificateLineage? = null

        // APK Signature Scheme v3 only supports a single signing certificate, so to move to v3
        // signing by default, but not require prior clients to update to explicitly disable v3
        // signing for multiple signers, we modify the mV3SigningEnabled depending on the provided
        // inputs (multiple signers and mSigningCertificateLineage in particular).  Maintain two
        // extra variables to record whether or not mV3SigningEnabled has been set directly by a
        // client and so should override the default behavior.
        private var mV3SigningExplicitlyDisabled = false
        private var mV3SigningExplicitlyEnabled = false

        /**
         * Constructs a new `Builder`.
         *
         * @param signerConfigs information about signers with which the APK will be signed. At
         * least one signer configuration must be provided.
         * @param minSdkVersion API Level of the oldest Android platform on which the APK is
         * supposed to be installed. See `minSdkVersion` attribute in the APK's
         * `AndroidManifest.xml`. The higher the version, the stronger signing features
         * will be enabled.
         */
        init {
            require(!signerConfigs.isEmpty()) { "At least one signer config must be provided" }
            if (signerConfigs.size > 1) {
                // APK Signature Scheme v3 only supports single signer, unless a
                // SigningCertificateLineage is provided, in which case this will be reset to true,
                // since we don't yet have a v4 scheme about which to worry
                mV3SigningEnabled = false
            }
            mSignerConfigs = ArrayList(signerConfigs)
            mMinSdkVersion = minSdkVersion
        }

        /**
         * Returns a new `DefaultApkSignerEngine` instance configured based on the
         * configuration of this builder.
         */
        @Throws(InvalidKeyException::class)
        fun build(): DefaultApkSignerEngine {
            check(!(mV3SigningExplicitlyDisabled && mV3SigningExplicitlyEnabled)) {
                ("Builder configured to both enable and disable APK " + "Signature Scheme v3 signing")
            }
            if (mV3SigningExplicitlyDisabled) {
                mV3SigningEnabled = false
            } else if (mV3SigningExplicitlyEnabled) {
                mV3SigningEnabled = true
            }

            // make sure our signers are appropriately setup
            if (mSigningCertificateLineage != null) {
                mSigningCertificateLineage?.let {
                    try {
                        mSignerConfigs = it.sortSignerConfigs(mSignerConfigs)
                        check(!(!mV3SigningEnabled && mSignerConfigs.size > 1)) { // this is a strange situation: we've provided a valid rotation history, but
                            // are only signing with v1/v2.  blow up, since we don't know for sure with
                            // which signer the user intended to sign
                            ("Provided multiple signers which are part " + "of the SigningCertificateLineage, but not signing with APK " + "Signature Scheme v3")
                        }
                    } catch (e: IllegalArgumentException) {
                        throw IllegalStateException(
                            "Provided signer configs do not match the " + "provided SigningCertificateLineage",
                            e
                        )
                    }
                }
            } else if (mV3SigningEnabled && mSignerConfigs.size > 1) {
                throw IllegalStateException(
                    "Multiple signing certificates provided for use " + "with APK Signature Scheme v3 without an accompanying SigningCertificateLineage"
                )
            }
            return DefaultApkSignerEngine(
                mSignerConfigs,
                mMinSdkVersion,
                mV1SigningEnabled,
                mV2SigningEnabled,
                mV3SigningEnabled,
                mDebuggableApkPermitted,
                mOtherSignersSignaturesPreserved,
                mCreatedBy,
                mSigningCertificateLineage
            )
        }

        /**
         * Sets whether the APK should be signed using JAR signing (aka v1 signature scheme).
         *
         *
         * By default, the APK will be signed using this scheme.
         */
        fun setV1SigningEnabled(enabled: Boolean): Builder {
            mV1SigningEnabled = enabled
            return this
        }

        /**
         * Sets whether the APK should be signed using APK Signature Scheme v2 (aka v2 signature
         * scheme).
         *
         *
         * By default, the APK will be signed using this scheme.
         */
        fun setV2SigningEnabled(enabled: Boolean): Builder {
            mV2SigningEnabled = enabled
            return this
        }

        /**
         * Sets whether the APK should be signed using APK Signature Scheme v3 (aka v3 signature
         * scheme).
         *
         *
         * By default, the APK will be signed using this scheme.
         */
        fun setV3SigningEnabled(enabled: Boolean): Builder {
            mV3SigningEnabled = enabled
            if (enabled) {
                mV3SigningExplicitlyEnabled = true
            } else {
                mV3SigningExplicitlyDisabled = true
            }
            return this
        }

        /**
         * Sets whether the APK should be signed even if it is marked as debuggable
         * (`android:debuggable="true"` in its `AndroidManifest.xml`). For backward
         * compatibility reasons, the default value of this setting is `true`.
         *
         *
         * It is dangerous to sign debuggable APKs with production/release keys because Android
         * platform loosens security checks for such APKs. For example, arbitrary unauthorized code
         * may be executed in the context of such an app by anybody with ADB shell access.
         */
        fun setDebuggableApkPermitted(permitted: Boolean): Builder {
            mDebuggableApkPermitted = permitted
            return this
        }

        /**
         * Sets whether signatures produced by signers other than the ones configured in this engine
         * should be copied from the input APK to the output APK.
         *
         *
         * By default, signatures of other signers are omitted from the output APK.
         */
        fun setOtherSignersSignaturesPreserved(preserved: Boolean): Builder {
            mOtherSignersSignaturesPreserved = preserved
            return this
        }

        /**
         * Sets the value of the `Created-By` field in JAR signature files.
         */
        fun setCreatedBy(createdBy: String?): Builder {
            if (createdBy == null) {
                throw NullPointerException()
            }
            mCreatedBy = createdBy
            return this
        }

        /**
         * Sets the [SigningCertificateLineage] to use with the v3 signature scheme.  This
         * structure provides proof of signing certificate rotation linking [SignerConfig]
         * objects to previous ones.
         */
        fun setSigningCertificateLineage(
            signingCertificateLineage: SigningCertificateLineage?
        ): Builder {
            if (signingCertificateLineage != null) {
                mV3SigningEnabled = true
                mSigningCertificateLineage = signingCertificateLineage
            }
            return this
        }
    }
}