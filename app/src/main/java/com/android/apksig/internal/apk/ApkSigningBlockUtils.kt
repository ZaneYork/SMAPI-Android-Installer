/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.apksig.internal.apk

import com.android.apksig.ApkVerifier.Issue
import com.android.apksig.ApkVerifier.IssueWithParams
import com.android.apksig.SigningCertificateLineage
import com.android.apksig.apk.ApkFormatException
import com.android.apksig.apk.ApkSigningBlockNotFoundException
import com.android.apksig.apk.ApkUtils
import com.android.apksig.apk.ApkUtils.ZipSections
import com.android.apksig.internal.util.ByteBufferDataSource
import com.android.apksig.internal.util.ChainedDataSource
import com.android.apksig.internal.util.Pair
import com.android.apksig.internal.util.VerityTreeBuilder
import com.android.apksig.internal.zip.ZipUtils
import com.android.apksig.util.DataSink
import com.android.apksig.util.DataSinks
import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import com.android.apksig.util.RunnablesExecutor
import java.io.IOException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.DigestException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.Arrays
import java.util.EnumMap
import java.util.concurrent.atomic.AtomicInteger

object ApkSigningBlockUtils {
    private val HEX_DIGITS = "01234567890abcdef".toCharArray()
    private const val CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES = (1024 * 1024).toLong()
    const val ANDROID_COMMON_PAGE_ALIGNMENT_BYTES = 4096
    val APK_SIGNING_BLOCK_MAGIC = byteArrayOf(
        0x41,
        0x50,
        0x4b,
        0x20,
        0x53,
        0x69,
        0x67,
        0x20,
        0x42,
        0x6c,
        0x6f,
        0x63,
        0x6b,
        0x20,
        0x34,
        0x32
    )
    private const val VERITY_PADDING_BLOCK_ID = 0x42726577
    const val VERSION_JAR_SIGNATURE_SCHEME = 1
    const val VERSION_APK_SIGNATURE_SCHEME_V2 = 2
    const val VERSION_APK_SIGNATURE_SCHEME_V3 = 3

    /**
     * Returns positive number if `alg1` is preferred over `alg2`, `-1` if
     * `alg2` is preferred over `alg1`, and `0` if there is no preference.
     */
    fun compareSignatureAlgorithm(alg1: SignatureAlgorithm, alg2: SignatureAlgorithm): Int {
        val digestAlg1 = alg1.contentDigestAlgorithm
        val digestAlg2 = alg2.contentDigestAlgorithm
        return compareContentDigestAlgorithm(digestAlg1, digestAlg2)
    }

    /**
     * Returns a positive number if `alg1` is preferred over `alg2`, a negative number
     * if `alg2` is preferred over `alg1`, or `0` if there is no preference.
     */
    private fun compareContentDigestAlgorithm(
        alg1: ContentDigestAlgorithm, alg2: ContentDigestAlgorithm
    ): Int {
        return when (alg1) {
            ContentDigestAlgorithm.CHUNKED_SHA256 -> when (alg2) {
                ContentDigestAlgorithm.CHUNKED_SHA256 -> 0
                ContentDigestAlgorithm.CHUNKED_SHA512, ContentDigestAlgorithm.VERITY_CHUNKED_SHA256 -> -1
            }

            ContentDigestAlgorithm.CHUNKED_SHA512 -> when (alg2) {
                ContentDigestAlgorithm.CHUNKED_SHA256, ContentDigestAlgorithm.VERITY_CHUNKED_SHA256 -> 1
                ContentDigestAlgorithm.CHUNKED_SHA512 -> 0
            }

            ContentDigestAlgorithm.VERITY_CHUNKED_SHA256 -> when (alg2) {
                ContentDigestAlgorithm.CHUNKED_SHA256 -> 1
                ContentDigestAlgorithm.VERITY_CHUNKED_SHA256 -> 0
                ContentDigestAlgorithm.CHUNKED_SHA512 -> -1
            }

        }
    }

    /**
     * Verifies integrity of the APK outside of the APK Signing Block by computing digests of the
     * APK and comparing them against the digests listed in APK Signing Block. The expected digests
     * are taken from `SignerInfos` of the provided `result`.
     *
     *
     * This method adds one or more errors to the `result` if a verification error is
     * expected to be encountered on Android. No errors are added to the `result` if the APK's
     * integrity is expected to verify on Android for each algorithm in
     * `contentDigestAlgorithms`.
     *
     *
     * The reason this method is currently not parameterized by a
     * `[minSdkVersion, maxSdkVersion]` range is that up until now content digest algorithms
     * exhibit the same behavior on all Android platform versions.
     */
    @JvmStatic
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun verifyIntegrity(
        executor: RunnablesExecutor,
        beforeApkSigningBlock: DataSource,
        centralDir: DataSource,
        eocd: ByteBuffer,
        contentDigestAlgorithms: Set<ContentDigestAlgorithm>,
        result: Result
    ) {
        if (contentDigestAlgorithms.isEmpty()) {
            // This should never occur because this method is invoked once at least one signature
            // is verified, meaning at least one content digest is known.
            throw RuntimeException("No content digests found")
        }

        // For the purposes of verifying integrity, ZIP End of Central Directory (EoCD) must be
        // treated as though its Central Directory offset points to the start of APK Signing Block.
        // We thus modify the EoCD accordingly.
        val modifiedEocd = ByteBuffer.allocate(eocd.remaining())
        val eocdSavedPos = eocd.position()
        modifiedEocd.order(ByteOrder.LITTLE_ENDIAN)
        modifiedEocd.put(eocd)
        modifiedEocd.flip()

        // restore eocd to position prior to modification in case it is to be used elsewhere
        eocd.position(eocdSavedPos)
        ZipUtils.setZipEocdCentralDirectoryOffset(modifiedEocd, beforeApkSigningBlock.size())
        val actualContentDigests: Map<ContentDigestAlgorithm, ByteArray>
        try {
            actualContentDigests = computeContentDigests(
                executor,
                contentDigestAlgorithms,
                beforeApkSigningBlock,
                centralDir,
                ByteBufferDataSource(modifiedEocd)
            )
            // Special checks for the verity algorithm requirements.
            if (actualContentDigests.containsKey(ContentDigestAlgorithm.VERITY_CHUNKED_SHA256)) {
                if (beforeApkSigningBlock.size() % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES != 0L) {
                    throw RuntimeException(
                        "APK Signing Block is not aligned on 4k boundary: " + beforeApkSigningBlock.size()
                    )
                }
                val centralDirOffset = ZipUtils.getZipEocdCentralDirectoryOffset(eocd)
                val signingBlockSize = centralDirOffset - beforeApkSigningBlock.size()
                if (signingBlockSize % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES != 0L) {
                    throw RuntimeException(
                        "APK Signing Block size is not multiple of page size: " + signingBlockSize
                    )
                }
            }
        } catch (e: DigestException) {
            throw RuntimeException("Failed to compute content digests", e)
        }
        if (contentDigestAlgorithms != actualContentDigests.keys) {
            throw RuntimeException(
                "Mismatch between sets of requested and computed content digests" + " . Requested: " + contentDigestAlgorithms + ", computed: " + actualContentDigests.keys
            )
        }

        // Compare digests computed over the rest of APK against the corresponding expected digests
        // in signer blocks.
        for (signerInfo in result.signers) {
            for (expected in signerInfo.contentDigests) {
                val signatureAlgorithm = SignatureAlgorithm.findById(
                    expected.signatureAlgorithmId
                ) ?: continue
                val contentDigestAlgorithm = signatureAlgorithm.contentDigestAlgorithm
                // if the current digest algorithm is not in the list provided by the caller then
                // ignore it; the signer may contain digests not recognized by the specified SDK
                // range.
                if (!contentDigestAlgorithms.contains(contentDigestAlgorithm)) {
                    continue
                }
                val expectedDigest = expected.value
                val actualDigest = actualContentDigests[contentDigestAlgorithm]
                if (!Arrays.equals(expectedDigest, actualDigest)) {
                    if (result.signatureSchemeVersion == VERSION_APK_SIGNATURE_SCHEME_V2) {
                        signerInfo.addError(
                            Issue.V2_SIG_APK_DIGEST_DID_NOT_VERIFY,
                            contentDigestAlgorithm,
                            toHex(expectedDigest),
                            toHex(actualDigest)
                        )
                    } else if (result.signatureSchemeVersion == VERSION_APK_SIGNATURE_SCHEME_V3) {
                        signerInfo.addError(
                            Issue.V3_SIG_APK_DIGEST_DID_NOT_VERIFY,
                            contentDigestAlgorithm,
                            toHex(expectedDigest),
                            toHex(actualDigest)
                        )
                    }
                    continue
                }
                signerInfo.verifiedContentDigests[contentDigestAlgorithm] = actualDigest
            }
        }
    }

    @Throws(SignatureNotFoundException::class)
    fun findApkSignatureSchemeBlock(
        apkSigningBlock: ByteBuffer, blockId: Int, result: Result?
    ): ByteBuffer {
        checkByteOrderLittleEndian(apkSigningBlock)
        // FORMAT:
        // OFFSET       DATA TYPE  DESCRIPTION
        // * @+0  bytes uint64:    size in bytes (excluding this field)
        // * @+8  bytes pairs
        // * @-24 bytes uint64:    size in bytes (same as the one above)
        // * @-16 bytes uint128:   magic
        val pairs = sliceFromTo(apkSigningBlock, 8, apkSigningBlock.capacity() - 24)
        var entryCount = 0
        while (pairs.hasRemaining()) {
            entryCount++
            if (pairs.remaining() < 8) {
                throw SignatureNotFoundException(
                    "Insufficient data to read size of APK Signing Block entry #$entryCount"
                )
            }
            val lenLong = pairs.long
            if (lenLong < 4 || lenLong > Int.MAX_VALUE) {
                throw SignatureNotFoundException(
                    "APK Signing Block entry #" + entryCount + " size out of range: " + lenLong
                )
            }
            val len = lenLong.toInt()
            val nextEntryPos = pairs.position() + len
            if (len > pairs.remaining()) {
                throw SignatureNotFoundException(
                    "APK Signing Block entry #" + entryCount + " size out of range: " + len + ", available: " + pairs.remaining()
                )
            }
            val id = pairs.int
            if (id == blockId) {
                return getByteBuffer(pairs, len - 4)
            }
            pairs.position(nextEntryPos)
        }
        throw SignatureNotFoundException(
            "No APK Signature Scheme block in APK Signing Block with ID: $blockId"
        )
    }

    @JvmStatic
    fun checkByteOrderLittleEndian(buffer: ByteBuffer) {
        require(buffer.order() == ByteOrder.LITTLE_ENDIAN) { "ByteBuffer byte order must be little endian" }
    }

    /**
     * Returns new byte buffer whose content is a shared subsequence of this buffer's content
     * between the specified start (inclusive) and end (exclusive) positions. As opposed to
     * [ByteBuffer.slice], the returned buffer's byte order is the same as the source
     * buffer's byte order.
     */
    private fun sliceFromTo(source: ByteBuffer, start: Int, end: Int): ByteBuffer {
        require(start >= 0) { "start: $start" }
        require(end >= start) { "end < start: $end < $start" }
        val capacity = source.capacity()
        require(end <= source.capacity()) { "end > capacity: $end > $capacity" }
        val originalLimit = source.limit()
        val originalPosition = source.position()
        return try {
            source.position(0)
            source.limit(end)
            source.position(start)
            val result = source.slice()
            result.order(source.order())
            result
        } finally {
            source.position(0)
            source.limit(originalLimit)
            source.position(originalPosition)
        }
    }

    /**
     * Relative *get* method for reading `size` number of bytes from the current
     * position of this buffer.
     *
     *
     * This method reads the next `size` bytes at this buffer's current position,
     * returning them as a `ByteBuffer` with start set to 0, limit and capacity set to
     * `size`, byte order set to this buffer's byte order; and then increments the position by
     * `size`.
     */
    private fun getByteBuffer(source: ByteBuffer, size: Int): ByteBuffer {
        require(size >= 0) { "size: $size" }
        val originalLimit = source.limit()
        val position = source.position()
        val limit = position + size
        if (limit < position || limit > originalLimit) {
            throw BufferUnderflowException()
        }
        source.limit(limit)
        return try {
            val result = source.slice()
            result.order(source.order())
            source.position(limit)
            result
        } finally {
            source.limit(originalLimit)
        }
    }

    @JvmStatic
    @Throws(ApkFormatException::class)
    fun getLengthPrefixedSlice(source: ByteBuffer): ByteBuffer {
        if (source.remaining() < 4) {
            throw ApkFormatException(
                "Remaining buffer too short to contain length of length-prefixed field" + ". Remaining: " + source.remaining()
            )
        }
        val len = source.int
        require(len >= 0) { "Negative length" }
        if (len > source.remaining()) {
            throw ApkFormatException(
                "Length-prefixed field longer than remaining buffer" + ". Field length: " + len + ", remaining: " + source.remaining()
            )
        }
        return getByteBuffer(source, len)
    }

    @JvmStatic
    @Throws(ApkFormatException::class)
    fun readLengthPrefixedByteArray(buf: ByteBuffer): ByteArray {
        val len = buf.int
        if (len < 0) {
            throw ApkFormatException("Negative length")
        } else if (len > buf.remaining()) {
            throw ApkFormatException(
                "Underflow while reading length-prefixed value. Length: " + len + ", available: " + buf.remaining()
            )
        }
        val result = ByteArray(len)
        buf[result]
        return result
    }

    @JvmStatic
    fun toHex(value: ByteArray?): String {
        val sb = StringBuilder(value!!.size * 2)
        val len = value.size
        for (i in 0 until len) {
            val hi = value[i].toInt() and 0xff ushr 4
            val lo = value[i].toInt() and 0x0f
            sb.append(HEX_DIGITS[hi]).append(HEX_DIGITS[lo])
        }
        return sb.toString()
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, DigestException::class)
    fun computeContentDigests(
        executor: RunnablesExecutor,
        digestAlgorithms: Set<ContentDigestAlgorithm>,
        beforeCentralDir: DataSource,
        centralDir: DataSource,
        eocd: DataSource
    ): Map<ContentDigestAlgorithm, ByteArray> {
        val contentDigests: MutableMap<ContentDigestAlgorithm, ByteArray> =
            EnumMap(com.android.apksig.internal.apk.ContentDigestAlgorithm::class.java)
        val oneMbChunkBasedAlgorithm = digestAlgorithms.filter { a ->
            a == ContentDigestAlgorithm.CHUNKED_SHA256 || a == ContentDigestAlgorithm.CHUNKED_SHA512
        }.toSet()
        computeOneMbChunkContentDigests(
            executor,
            oneMbChunkBasedAlgorithm,
            arrayOf(beforeCentralDir, centralDir, eocd),
            contentDigests
        )
        if (digestAlgorithms.contains(ContentDigestAlgorithm.VERITY_CHUNKED_SHA256)) {
            computeApkVerityDigest(beforeCentralDir, centralDir, eocd, contentDigests)
        }
        return contentDigests
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, DigestException::class)
    fun computeOneMbChunkContentDigests(
        digestAlgorithms: Set<ContentDigestAlgorithm>,
        contents: Array<DataSource>,
        outputContentDigests: MutableMap<ContentDigestAlgorithm?, ByteArray?>
    ) {
        // For each digest algorithm the result is computed as follows:
        // 1. Each segment of contents is split into consecutive chunks of 1 MB in size.
        //    The final chunk will be shorter iff the length of segment is not a multiple of 1 MB.
        //    No chunks are produced for empty (zero length) segments.
        // 2. The digest of each chunk is computed over the concatenation of byte 0xa5, the chunk's
        //    length in bytes (uint32 little-endian) and the chunk's contents.
        // 3. The output digest is computed over the concatenation of the byte 0x5a, the number of
        //    chunks (uint32 little-endian) and the concatenation of digests of chunks of all
        //    segments in-order.
        var chunkCountLong: Long = 0
        for (input in contents) {
            chunkCountLong += getChunkCount(input.size(), CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES)
        }
        if (chunkCountLong > Int.MAX_VALUE) {
            throw DigestException("Input too long: $chunkCountLong chunks")
        }
        val chunkCount = chunkCountLong.toInt()
        val digestAlgorithmsArray = digestAlgorithms.toTypedArray()
        val mds = arrayOfNulls<MessageDigest>(digestAlgorithmsArray.size)
        val digestsOfChunks = arrayOfNulls<ByteArray>(digestAlgorithmsArray.size)
        val digestOutputSizes = IntArray(digestAlgorithmsArray.size)
        for (i in digestAlgorithmsArray.indices) {
            val digestAlgorithm = digestAlgorithmsArray[i]
            val digestOutputSizeBytes = digestAlgorithm.chunkDigestOutputSizeBytes
            digestOutputSizes[i] = digestOutputSizeBytes
            val concatenationOfChunkCountAndChunkDigests =
                ByteArray(5 + chunkCount * digestOutputSizeBytes)
            concatenationOfChunkCountAndChunkDigests[0] = 0x5a
            setUnsignedInt32LittleEndian(
                chunkCount, concatenationOfChunkCountAndChunkDigests, 1
            )
            digestsOfChunks[i] = concatenationOfChunkCountAndChunkDigests
            val jcaAlgorithm = digestAlgorithm.jcaMessageDigestAlgorithm
            mds[i] = MessageDigest.getInstance(jcaAlgorithm)
        }
        val mdSink = DataSinks.asDataSink(*mds)
        val chunkContentPrefix = ByteArray(5)
        chunkContentPrefix[0] = 0xa5.toByte()
        var chunkIndex = 0
        // Optimization opportunity: digests of chunks can be computed in parallel. However,
        // determining the number of computations to be performed in parallel is non-trivial. This
        // depends on a wide range of factors, such as data source type (e.g., in-memory or fetched
        // from file), CPU/memory/disk cache bandwidth and latency, interconnect architecture of CPU
        // cores, load on the system from other threads of execution and other processes, size of
        // input.
        // For now, we compute these digests sequentially and thus have the luxury of improving
        // performance by writing the digest of each chunk into a pre-allocated buffer at exactly
        // the right position. This avoids unnecessary allocations, copying, and enables the final
        // digest to be more efficient because it's presented with all of its input in one go.
        for (input in contents) {
            var inputOffset: Long = 0
            var inputRemaining = input.size()
            while (inputRemaining > 0) {
                val chunkSize =
                    Math.min(inputRemaining, CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES).toInt()
                setUnsignedInt32LittleEndian(chunkSize, chunkContentPrefix, 1)
                for (i in mds.indices) {
                    mds[i]!!.update(chunkContentPrefix)
                }
                try {
                    input.feed(inputOffset, chunkSize.toLong(), mdSink)
                } catch (e: IOException) {
                    throw IOException("Failed to read chunk #$chunkIndex", e)
                }
                for (i in digestAlgorithmsArray.indices) {
                    val md = mds[i]
                    val concatenationOfChunkCountAndChunkDigests = digestsOfChunks[i]
                    val expectedDigestSizeBytes = digestOutputSizes[i]
                    val actualDigestSizeBytes = concatenationOfChunkCountAndChunkDigests?.let {
                        md!!.digest(
                            it, 5 + chunkIndex * expectedDigestSizeBytes, expectedDigestSizeBytes
                        )
                    }
                    if (actualDigestSizeBytes != expectedDigestSizeBytes) {
                        throw RuntimeException(
                            "Unexpected output size of " + md!!.algorithm + " digest: " + actualDigestSizeBytes
                        )
                    }
                }
                inputOffset += chunkSize.toLong()
                inputRemaining -= chunkSize.toLong()
                chunkIndex++
            }
        }
        for (i in digestAlgorithmsArray.indices) {
            val digestAlgorithm = digestAlgorithmsArray[i]
            val concatenationOfChunkCountAndChunkDigests = digestsOfChunks[i]
            val md = mds[i]
            val digest = concatenationOfChunkCountAndChunkDigests?.let { md!!.digest(it) }
            outputContentDigests[digestAlgorithm] = digest
        }
    }

    @Throws(NoSuchAlgorithmException::class, DigestException::class)
    fun computeOneMbChunkContentDigests(
        executor: RunnablesExecutor,
        digestAlgorithms: Set<ContentDigestAlgorithm>,
        contents: Array<DataSource>,
        outputContentDigests: MutableMap<ContentDigestAlgorithm, ByteArray>
    ) {
        var chunkCountLong: Long = 0
        for (input in contents) {
            chunkCountLong += getChunkCount(input.size(), CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES)
        }
        if (chunkCountLong > Int.MAX_VALUE) {
            throw DigestException("Input too long: $chunkCountLong chunks")
        }
        val chunkCount = chunkCountLong.toInt()
        val chunkDigestsList: MutableList<ChunkDigests> = ArrayList(digestAlgorithms.size)
        for (algorithms in digestAlgorithms) {
            chunkDigestsList.add(ChunkDigests(algorithms, chunkCount))
        }
        val chunkSupplier = ChunkSupplier(contents)
        executor.execute { ChunkDigester(chunkSupplier, chunkDigestsList) }

        // Compute and write out final digest for each algorithm.
        for (chunkDigests in chunkDigestsList) {
            val messageDigest = chunkDigests.createMessageDigest()
            outputContentDigests[chunkDigests.algorithm] =
                messageDigest.digest(chunkDigests.concatOfDigestsOfChunks)
        }
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun computeApkVerityDigest(
        beforeCentralDir: DataSource,
        centralDir: DataSource,
        eocd: DataSource,
        outputContentDigests: MutableMap<ContentDigestAlgorithm, ByteArray>
    ) {
        // FORMAT:
        // OFFSET       DATA TYPE  DESCRIPTION
        // * @+0  bytes uint8[32]  Merkle tree root hash of SHA-256
        // * @+32 bytes int64      Length of source data
        val backBufferSize =
            ContentDigestAlgorithm.VERITY_CHUNKED_SHA256.chunkDigestOutputSizeBytes + java.lang.Long.SIZE / java.lang.Byte.SIZE
        val encoded = ByteBuffer.allocate(backBufferSize)
        encoded.order(ByteOrder.LITTLE_ENDIAN)

        // Use 0s as salt for now.  This also needs to be consistent in the fsverify header for
        // kernel to use.
        val builder = VerityTreeBuilder(ByteArray(8))
        val rootHash = builder.generateVerityTreeRootHash(beforeCentralDir, centralDir, eocd)
        encoded.put(rootHash)
        encoded.putLong(beforeCentralDir.size() + centralDir.size() + eocd.size())
        outputContentDigests[ContentDigestAlgorithm.VERITY_CHUNKED_SHA256] = encoded.array()
    }

    private fun getChunkCount(inputSize: Long, chunkSize: Long): Long {
        return (inputSize + chunkSize - 1) / chunkSize
    }

    private fun setUnsignedInt32LittleEndian(value: Int, result: ByteArray, offset: Int) {
        result[offset] = (value and 0xff).toByte()
        result[offset + 1] = (value shr 8 and 0xff).toByte()
        result[offset + 2] = (value shr 16 and 0xff).toByte()
        result[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    @JvmStatic
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    fun encodePublicKey(publicKey: PublicKey): ByteArray {
        var encodedPublicKey: ByteArray? = null
        if ("X.509" == publicKey.format) {
            encodedPublicKey = publicKey.encoded
        }
        if (encodedPublicKey == null) {
            encodedPublicKey = try {
                KeyFactory.getInstance(publicKey.algorithm)
                    .getKeySpec(publicKey, X509EncodedKeySpec::class.java).encoded
            } catch (e: InvalidKeySpecException) {
                throw InvalidKeyException(
                    "Failed to obtain X.509 encoded form of public key " + publicKey + " of class " + publicKey.javaClass.name,
                    e
                )
            }
        }
        if (encodedPublicKey == null || encodedPublicKey.size == 0) {
            throw InvalidKeyException(
                "Failed to obtain X.509 encoded form of public key " + publicKey + " of class " + publicKey.javaClass.name
            )
        }
        return encodedPublicKey
    }

    @JvmStatic
    @Throws(CertificateEncodingException::class)
    fun encodeCertificates(certificates: List<X509Certificate>): List<ByteArray> {
        val result: MutableList<ByteArray> = ArrayList(certificates.size)
        for (certificate in certificates) {
            result.add(certificate.encoded)
        }
        return result
    }

    @JvmStatic
    fun encodeAsLengthPrefixedElement(bytes: ByteArray?): ByteArray {
        val adapterBytes = listOf(bytes)
        return encodeAsSequenceOfLengthPrefixedElements(adapterBytes)
    }

    @JvmStatic
    fun encodeAsSequenceOfLengthPrefixedElements(arrays: Array<ByteArray>): ByteArray {
        return encodeAsSequenceOfLengthPrefixedElements(arrays.asList())
    }

    @JvmStatic
    fun encodeAsSequenceOfLengthPrefixedElements(sequence: List<ByteArray?>): ByteArray {
        var payloadSize = 0
        for (element in sequence) {
            payloadSize += 4 + element!!.size
        }
        val result = ByteBuffer.allocate(payloadSize)
        result.order(ByteOrder.LITTLE_ENDIAN)
        for (element in sequence) {
            result.putInt(element!!.size)
            result.put(element)
        }
        return result.array()
    }

    @JvmStatic
    fun encodeAsSequenceOfLengthPrefixedPairsOfIntAndLengthPrefixedBytes(
        sequence: List<Pair<Int?, ByteArray>>
    ): ByteArray {
        var resultSize = 0
        for (element in sequence) {
            resultSize += 12 + element.second.size
        }
        val result = ByteBuffer.allocate(resultSize)
        result.order(ByteOrder.LITTLE_ENDIAN)
        for (element in sequence) {
            val second = element.second
            result.putInt(8 + second.size)
            result.putInt(element.first!!)
            result.putInt(second.size)
            result.put(second)
        }
        return result.array()
    }

    /**
     * Returns the APK Signature Scheme block contained in the provided APK file for the given ID
     * and the additional information relevant for verifying the block against the file.
     *
     * @param blockId the ID value in the APK Signing Block's sequence of ID-value pairs
     * identifying the appropriate block to find, e.g. the APK Signature Scheme v2
     * block ID.
     *
     * @throws SignatureNotFoundException if the APK is not signed using given APK Signature Scheme
     * @throws IOException if an I/O error occurs while reading the APK
     */
    @JvmStatic
    @Throws(IOException::class, SignatureNotFoundException::class)
    fun findSignature(
        apk: DataSource?, zipSections: ZipSections, blockId: Int, result: Result?
    ): SignatureInfo {
        // Find the APK Signing Block.
        val apkSigningBlock: DataSource
        val apkSigningBlockOffset: Long
        try {
            val apkSigningBlockInfo = ApkUtils.findApkSigningBlock(apk, zipSections)
            apkSigningBlockOffset = apkSigningBlockInfo.startOffset
            apkSigningBlock = apkSigningBlockInfo.contents
        } catch (e: ApkSigningBlockNotFoundException) {
            throw SignatureNotFoundException(e.message, e)
        }
        val apkSigningBlockBuf = apkSigningBlock.getByteBuffer(0, apkSigningBlock.size().toInt())
        apkSigningBlockBuf.order(ByteOrder.LITTLE_ENDIAN)

        // Find the APK Signature Scheme Block inside the APK Signing Block.
        val apkSignatureSchemeBlock =
            findApkSignatureSchemeBlock(apkSigningBlockBuf, blockId, result)
        return SignatureInfo(
            apkSignatureSchemeBlock,
            apkSigningBlockOffset,
            zipSections.zipCentralDirectoryOffset,
            zipSections.zipEndOfCentralDirectoryOffset,
            zipSections.zipEndOfCentralDirectory
        )
    }

    /**
     * Generates a new DataSource representing the APK contents before the Central Directory with
     * padding, if padding is requested.  If the existing data entries before the Central Directory
     * are already aligned, or no padding is requested, the original DataSource is used.  This
     * padding is used to allow for verity-based APK verification.
     *
     * @return `Pair` containing the potentially new `DataSource` and the amount of
     * padding used.
     */
    @JvmStatic
    fun generateApkSigningBlockPadding(
        beforeCentralDir: DataSource, apkSigningBlockPaddingSupported: Boolean
    ): Pair<DataSource, Int> {

        // Ensure APK Signing Block starts from page boundary.
        var centralDir = beforeCentralDir
        var padSizeBeforeSigningBlock = 0
        if (apkSigningBlockPaddingSupported && centralDir.size() % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES != 0L) {
            padSizeBeforeSigningBlock =
                (ANDROID_COMMON_PAGE_ALIGNMENT_BYTES - centralDir.size() % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES).toInt()
            centralDir = ChainedDataSource(
                centralDir, DataSources.asDataSource(
                    ByteBuffer.allocate(padSizeBeforeSigningBlock)
                )
            )
        }
        return Pair.of(centralDir, padSizeBeforeSigningBlock)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copyWithModifiedCDOffset(
        beforeCentralDir: DataSource, eocd: DataSource
    ): DataSource {

        // Ensure that, when digesting, ZIP End of Central Directory record's Central Directory
        // offset field is treated as pointing to the offset at which the APK Signing Block will
        // start.
        val centralDirOffsetForDigesting = beforeCentralDir.size()
        val eocdBuf = ByteBuffer.allocate(eocd.size().toInt())
        eocdBuf.order(ByteOrder.LITTLE_ENDIAN)
        eocd.copyTo(0, eocd.size().toInt(), eocdBuf)
        eocdBuf.flip()
        ZipUtils.setZipEocdCentralDirectoryOffset(eocdBuf, centralDirOffsetForDigesting)
        return DataSources.asDataSource(eocdBuf)
    }

    @JvmStatic
    fun generateApkSigningBlock(
        apkSignatureSchemeBlockPairs: List<Pair<ByteArray, Int>>
    ): ByteArray {
        // FORMAT:
        // uint64:  size (excluding this field)
        // repeated ID-value pairs:
        //     uint64:           size (excluding this field)
        //     uint32:           ID
        //     (size - 4) bytes: value
        // (extra dummy ID-value for padding to make block size a multiple of 4096 bytes)
        // uint64:  size (same as the one above)
        // uint128: magic
        var blocksSize = 0
        for (schemeBlockPair in apkSignatureSchemeBlockPairs) {
            blocksSize += 8 + 4 + schemeBlockPair.first.size // size + id + value
        }
        var resultSize = (8 // size
                + blocksSize + 8 // size
                + 16) // magic
        var paddingPair: ByteBuffer? = null
        if (resultSize % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES != 0) {
            var padding =
                ANDROID_COMMON_PAGE_ALIGNMENT_BYTES - resultSize % ANDROID_COMMON_PAGE_ALIGNMENT_BYTES
            if (padding < 12) {  // minimum size of an ID-value pair
                padding += ANDROID_COMMON_PAGE_ALIGNMENT_BYTES
            }
            paddingPair = ByteBuffer.allocate(padding).order(ByteOrder.LITTLE_ENDIAN)
            paddingPair.putLong((padding - 8).toLong())
            paddingPair.putInt(VERITY_PADDING_BLOCK_ID)
            paddingPair.rewind()
            resultSize += padding
        }
        val result = ByteBuffer.allocate(resultSize)
        result.order(ByteOrder.LITTLE_ENDIAN)
        val blockSizeFieldValue = resultSize - 8L
        result.putLong(blockSizeFieldValue)
        for (schemeBlockPair in apkSignatureSchemeBlockPairs) {
            val apkSignatureSchemeBlock = schemeBlockPair.first
            val apkSignatureSchemeId = schemeBlockPair.second
            val pairSizeFieldValue = 4L + apkSignatureSchemeBlock.size
            result.putLong(pairSizeFieldValue)
            result.putInt(apkSignatureSchemeId)
            result.put(apkSignatureSchemeBlock)
        }
        if (paddingPair != null) {
            result.put(paddingPair)
        }
        result.putLong(blockSizeFieldValue)
        result.put(APK_SIGNING_BLOCK_MAGIC)
        return result.array()
    }

    /**
     * Computes the digests of the given APK components according to the algorithms specified in the
     * given SignerConfigs.
     *
     * @param signerConfigs signer configurations, one for each signer At least one signer config
     * must be provided.
     *
     * @throws IOException if an I/O error occurs
     * @throws NoSuchAlgorithmException if a required cryptographic algorithm implementation is
     * missing
     * @throws SignatureException if an error occurs when computing digests of generating
     * signatures
     */
    @JvmStatic
    @Throws(IOException::class, NoSuchAlgorithmException::class, SignatureException::class)
    fun computeContentDigests(
        executor: RunnablesExecutor,
        beforeCentralDir: DataSource,
        centralDir: DataSource,
        eocd: DataSource,
        signerConfigs: List<SignerConfig>
    ): Pair<List<SignerConfig>, Map<ContentDigestAlgorithm, ByteArray>> {
        require(!signerConfigs.isEmpty()) { "No signer configs provided. At least one is required" }

        // Figure out which digest(s) to use for APK contents.
        val contentDigestAlgorithms: MutableSet<ContentDigestAlgorithm> = HashSet(1)
        for (signerConfig in signerConfigs) {
            for (signatureAlgorithm in signerConfig.signatureAlgorithms!!) {
                contentDigestAlgorithms.add(signatureAlgorithm.contentDigestAlgorithm)
            }
        }

        // Compute digests of APK contents.
        val contentDigests: Map<ContentDigestAlgorithm, ByteArray> // digest algorithm ID -> digest
        contentDigests = try {
            computeContentDigests(
                executor, contentDigestAlgorithms, beforeCentralDir, centralDir, eocd
            )
        } catch (e: IOException) {
            throw IOException("Failed to read APK being signed", e)
        } catch (e: DigestException) {
            throw SignatureException("Failed to compute digests of APK", e)
        }

        // Sign the digests and wrap the signatures and signer info into an APK Signing Block.
        return Pair.of(signerConfigs, contentDigests)
    }

    /**
     * Returns the subset of signatures which are expected to be verified by at least one Android
     * platform version in the `[minSdkVersion, maxSdkVersion]` range. The returned result is
     * guaranteed to contain at least one signature.
     *
     *
     * Each Android platform version typically verifies exactly one signature from the provided
     * `signatures` set. This method returns the set of these signatures collected over all
     * requested platform versions. As a result, the result may contain more than one signature.
     *
     * @throws NoSupportedSignaturesException if no supported signatures were
     * found for an Android platform version in the range.
     */
    @JvmStatic
    @Throws(NoSupportedSignaturesException::class)
    fun getSignaturesToVerify(
        signatures: List<SupportedSignature>, minSdkVersion: Int, maxSdkVersion: Int
    ): List<SupportedSignature> {
        // Pick the signature with the strongest algorithm at all required SDK versions, to mimic
        // Android's behavior on those versions.
        //
        // Here we assume that, once introduced, a signature algorithm continues to be supported in
        // all future Android versions. We also assume that the better-than relationship between
        // algorithms is exactly the same on all Android platform versions (except that older
        // platforms might support fewer algorithms). If these assumption are no longer true, the
        // logic here will need to change accordingly.
        val bestSigAlgorithmOnSdkVersion: MutableMap<Int, SupportedSignature> = HashMap()
        var minProvidedSignaturesVersion = Int.MAX_VALUE
        for (sig in signatures) {
            val sigAlgorithm = sig.algorithm
            val sigMinSdkVersion = sigAlgorithm.minSdkVersion
            if (sigMinSdkVersion > maxSdkVersion) {
                continue
            }
            if (sigMinSdkVersion < minProvidedSignaturesVersion) {
                minProvidedSignaturesVersion = sigMinSdkVersion
            }
            val candidate = bestSigAlgorithmOnSdkVersion[sigMinSdkVersion]
            if (candidate == null || compareSignatureAlgorithm(
                    sigAlgorithm, candidate.algorithm
                ) > 0
            ) {
                bestSigAlgorithmOnSdkVersion[sigMinSdkVersion] = sig
            }
        }

        // Must have some supported signature algorithms for minSdkVersion.
        if (minSdkVersion < minProvidedSignaturesVersion) {
            throw NoSupportedSignaturesException(
                "Minimum provided signature version " + minProvidedSignaturesVersion + " < minSdkVersion " + minSdkVersion
            )
        }
        if (bestSigAlgorithmOnSdkVersion.isEmpty()) {
            throw NoSupportedSignaturesException("No supported signature")
        }
        return bestSigAlgorithmOnSdkVersion.values.sortedWith { sig1, sig2 ->
            sig1.algorithm.id.compareTo(sig2.algorithm.id)
        }.toList()
    }

    /**
     * uses the SignatureAlgorithms in the provided signerConfig to sign the provided data
     *
     * @return list of signature algorithm IDs and their corresponding signatures over the data.
     */
    @JvmStatic
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class, SignatureException::class)
    fun generateSignaturesOverData(
        signerConfig: SignerConfig, data: ByteArray?
    ): List<Pair<Int, ByteArray>> {
        val signatures: MutableList<Pair<Int, ByteArray>> = ArrayList(
            signerConfig.signatureAlgorithms!!.size
        )
        val publicKey = signerConfig.certificates!![0].publicKey
        for (signatureAlgorithm in signerConfig.signatureAlgorithms!!) {
            val sigAlgAndParams = signatureAlgorithm.jcaSignatureAlgorithmAndParams
            val jcaSignatureAlgorithm = sigAlgAndParams.first
            val jcaSignatureAlgorithmParams = sigAlgAndParams.second
            var signatureBytes: ByteArray
            signatureBytes = try {
                val signature = Signature.getInstance(jcaSignatureAlgorithm)
                signature.initSign(signerConfig.privateKey)
                if (jcaSignatureAlgorithmParams != null) {
                    signature.setParameter(jcaSignatureAlgorithmParams)
                }
                signature.update(data)
                signature.sign()
            } catch (e: InvalidKeyException) {
                throw InvalidKeyException("Failed to sign using $jcaSignatureAlgorithm", e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw SignatureException("Failed to sign using $jcaSignatureAlgorithm", e)
            } catch (e: SignatureException) {
                throw SignatureException("Failed to sign using $jcaSignatureAlgorithm", e)
            }
            try {
                val signature = Signature.getInstance(jcaSignatureAlgorithm)
                signature.initVerify(publicKey)
                if (jcaSignatureAlgorithmParams != null) {
                    signature.setParameter(jcaSignatureAlgorithmParams)
                }
                signature.update(data)
                if (!signature.verify(signatureBytes)) {
                    throw SignatureException(
                        "Failed to verify generated " + jcaSignatureAlgorithm + " signature using public key from certificate"
                    )
                }
            } catch (e: InvalidKeyException) {
                throw InvalidKeyException(
                    "Failed to verify generated " + jcaSignatureAlgorithm + " signature using" + " public key from certificate",
                    e
                )
            } catch (e: InvalidAlgorithmParameterException) {
                throw SignatureException(
                    "Failed to verify generated " + jcaSignatureAlgorithm + " signature using" + " public key from certificate",
                    e
                )
            } catch (e: SignatureException) {
                throw SignatureException(
                    "Failed to verify generated " + jcaSignatureAlgorithm + " signature using" + " public key from certificate",
                    e
                )
            }
            signatures.add(Pair.of(signatureAlgorithm.id, signatureBytes))
        }
        return signatures
    }

    private class ChunkDigests(val algorithm: ContentDigestAlgorithm, chunkCount: Int) {
        val digestOutputSize: Int
        val concatOfDigestsOfChunks: ByteArray

        init {
            digestOutputSize = algorithm.chunkDigestOutputSizeBytes
            concatOfDigestsOfChunks = ByteArray(1 + 4 + chunkCount * digestOutputSize)

            // Fill the initial values of the concatenated digests of chunks, which is
            // {0x5a, 4-bytes-of-little-endian-chunk-count, digests*...}.
            concatOfDigestsOfChunks[0] = 0x5a
            setUnsignedInt32LittleEndian(chunkCount, concatOfDigestsOfChunks, 1)
        }

        @Throws(NoSuchAlgorithmException::class)
        fun createMessageDigest(): MessageDigest {
            return MessageDigest.getInstance(algorithm.jcaMessageDigestAlgorithm)
        }

        fun getOffset(chunkIndex: Int): Int {
            return 1 + 4 + chunkIndex * digestOutputSize
        }
    }

    /**
     * A per-thread digest worker.
     */
    private class ChunkDigester(
        private val dataSupplier: ChunkSupplier, private val chunkDigests: List<ChunkDigests>
    ) : Runnable {
        private val messageDigests: MutableList<MessageDigest>
        private val mdSink: DataSink

        init {
            messageDigests = ArrayList(chunkDigests.size)
            for (chunkDigest in chunkDigests) {
                try {
                    messageDigests.add(chunkDigest.createMessageDigest())
                } catch (ex: NoSuchAlgorithmException) {
                    throw RuntimeException(ex)
                }
            }
            mdSink = DataSinks.asDataSink(*messageDigests.toTypedArray())
        }

        override fun run() {
            val chunkContentPrefix = ByteArray(5)
            chunkContentPrefix[0] = 0xa5.toByte()
            try {
                var chunk = dataSupplier.get()
                while (chunk != null) {
                    val size = chunk.dataSource.size()
                    if (size > CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES) {
                        throw RuntimeException("Chunk size greater than expected: $size")
                    }

                    // First update with the chunk prefix.
                    setUnsignedInt32LittleEndian(size.toInt(), chunkContentPrefix, 1)
                    mdSink.consume(chunkContentPrefix, 0, chunkContentPrefix.size)

                    // Then update with the chunk data.
                    chunk.dataSource.feed(0, size, mdSink)

                    // Now finalize chunk for all algorithms.
                    for (i in chunkDigests.indices) {
                        val chunkDigest = chunkDigests[i]
                        val actualDigestSize = messageDigests[i].digest(
                            chunkDigest.concatOfDigestsOfChunks,
                            chunkDigest.getOffset(chunk.chunkIndex),
                            chunkDigest.digestOutputSize
                        )
                        if (actualDigestSize != chunkDigest.digestOutputSize) {
                            throw RuntimeException(
                                "Unexpected output size of " + chunkDigest.algorithm + " digest: " + actualDigestSize
                            )
                        }
                    }
                    chunk = dataSupplier.get()
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (e: DigestException) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * Thread-safe 1MB DataSource chunk supplier. When bounds are met in a
     * supplied [DataSource], the data from the next [DataSource]
     * are NOT concatenated. Only the next call to get() will fetch from the
     * next [DataSource] in the input [DataSource] array.
     */
    private class ChunkSupplier(private val dataSources: Array<DataSource>) {
        private val chunkCounts: IntArray
        private val totalChunkCount: Int
        private val nextIndex: AtomicInteger

        init {
            chunkCounts = IntArray(dataSources.size)
            var totalChunkCount = 0
            for (i in dataSources.indices) {
                val chunkCount = getChunkCount(
                    dataSources[i].size(), CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES
                )
                if (chunkCount > Int.MAX_VALUE) {
                    throw RuntimeException(
                        String.format(
                            "Number of chunks in dataSource[%d] is greater than max int.", i
                        )
                    )
                }
                chunkCounts[i] = chunkCount.toInt()
                totalChunkCount += chunkCount.toInt()
            }
            this.totalChunkCount = totalChunkCount
            nextIndex = AtomicInteger(0)
        }

        /**
         * We map an integer index to the termination-adjusted dataSources 1MB chunks.
         * Note that [Chunk]s could be less than 1MB, namely the last 1MB-aligned
         * blocks in each input [DataSource] (unless the DataSource itself is
         * 1MB-aligned).
         */
        fun get(): Chunk? {
            val index = nextIndex.getAndIncrement()
            if (index < 0 || index >= totalChunkCount) {
                return null
            }
            var dataSourceIndex = 0
            var dataSourceChunkOffset = index
            while (dataSourceIndex < dataSources.size) {
                if (dataSourceChunkOffset < chunkCounts[dataSourceIndex]) {
                    break
                }
                dataSourceChunkOffset -= chunkCounts[dataSourceIndex]
                dataSourceIndex++
            }
            val remainingSize = Math.min(
                dataSources[dataSourceIndex].size() - dataSourceChunkOffset * CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES,
                CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES
            )
            // Note that slicing may involve its own locking. We may wish to reimplement the
            // underlying mechanism to get rid of that lock (e.g. ByteBufferDataSource should
            // probably get reimplemented to a delegate model, such that grabbing a slice
            // doesn't incur a lock).
            return Chunk(
                dataSources[dataSourceIndex].slice(
                    dataSourceChunkOffset * CONTENT_DIGESTED_CHUNK_MAX_SIZE_BYTES, remainingSize
                ), index
            )
        }

        class Chunk(val dataSource: DataSource, val chunkIndex: Int)
    }

    class NoSupportedSignaturesException(message: String?) : Exception(message) {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    class SignatureNotFoundException : Exception {
        constructor(message: String?) : super(message) {}
        constructor(message: String?, cause: Throwable?) : super(message, cause) {}

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    /**
     * Signer configuration.
     */
    class SignerConfig {
        /** Private key.  */
        @JvmField
        var privateKey: PrivateKey? = null

        /**
         * Certificates, with the first certificate containing the public key corresponding to
         * [.privateKey].
         */
        @JvmField
        var certificates: List<X509Certificate>? = null

        /**
         * List of signature algorithms with which to sign.
         */
        @JvmField
        var signatureAlgorithms: List<SignatureAlgorithm>? = null

        @JvmField
        var minSdkVersion = 0

        @JvmField
        var maxSdkVersion = 0

        @JvmField
        var mSigningCertificateLineage: SigningCertificateLineage? = null
    }

    class Result(val signatureSchemeVersion: Int) {
        /** Whether the APK's APK Signature Scheme signature verifies.  */
        @JvmField
        var verified = false

        @JvmField
        val signers: List<SignerInfo> = ArrayList()

        @JvmField
        var signingCertificateLineage: SigningCertificateLineage? = null
        private val mWarnings: MutableList<IssueWithParams> = ArrayList()
        private val mErrors: MutableList<IssueWithParams> = ArrayList()
        fun containsErrors(): Boolean {
            if (!mErrors.isEmpty()) {
                return true
            }
            if (!signers.isEmpty()) {
                for (signer in signers) {
                    if (signer.containsErrors()) {
                        return true
                    }
                }
            }
            return false
        }

        fun addError(msg: Issue?, vararg parameters: Any?) {
            mErrors.add(IssueWithParams(msg, parameters))
        }

        fun addWarning(msg: Issue?, vararg parameters: Any?) {
            mWarnings.add(IssueWithParams(msg, parameters))
        }

        val errors: List<IssueWithParams>
            get() = mErrors
        val warnings: List<IssueWithParams>
            get() = mWarnings

        class SignerInfo {
            @JvmField
            var index = 0

            @JvmField
            var certs: MutableList<X509Certificate> = ArrayList()

            @JvmField
            var contentDigests: List<ContentDigest> = ArrayList()
            var verifiedContentDigests: MutableMap<ContentDigestAlgorithm, ByteArray?> =
                EnumMap(com.android.apksig.internal.apk.ContentDigestAlgorithm::class.java)

            @JvmField
            var signatures: List<Signature> = ArrayList()

            @JvmField
            var verifiedSignatures: Map<SignatureAlgorithm, ByteArray> =
                EnumMap(com.android.apksig.internal.apk.SignatureAlgorithm::class.java)

            @JvmField
            var additionalAttributes: List<AdditionalAttribute> = ArrayList()

            @JvmField
            var signedData: ByteArray? = null

            @JvmField
            var minSdkVersion = 0

            @JvmField
            var maxSdkVersion = 0

            @JvmField
            var signingCertificateLineage: SigningCertificateLineage? = null
            private val mWarnings: MutableList<IssueWithParams> = ArrayList()
            private val mErrors: MutableList<IssueWithParams> = ArrayList()
            fun addError(msg: Issue?, vararg parameters: Any?) {
                mErrors.add(IssueWithParams(msg, parameters))
            }

            fun addWarning(msg: Issue?, vararg parameters: Any?) {
                mWarnings.add(IssueWithParams(msg, parameters))
            }

            fun containsErrors(): Boolean {
                return !mErrors.isEmpty()
            }

            val errors: List<IssueWithParams>
                get() = mErrors
            val warnings: List<IssueWithParams>
                get() = mWarnings

            class ContentDigest(val signatureAlgorithmId: Int, val value: ByteArray)
            class Signature(val algorithmId: Int, val value: ByteArray)
            class AdditionalAttribute(val id: Int, value: ByteArray) {
                private val mValue: ByteArray

                init {
                    mValue = value.clone()
                }

                val value: ByteArray
                    get() = mValue.clone()
            }
        }
    }

    class SupportedSignature(val algorithm: SignatureAlgorithm, val signature: ByteArray)
}