package com.zane.smapiinstaller.utils

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.common.io.ByteSource
import com.google.common.io.ByteStreams
import com.zane.smapiinstaller.dto.AssemblyStoreAssembly
import com.zane.smapiinstaller.dto.Tuple2
import net.fornwall.apksigner.zipio.ZioEntry
import net.fornwall.apksigner.zipio.ZipInput
import net.fornwall.apksigner.zipio.ZipOutput
import net.jpountz.lz4.LZ4Factory
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Zane
 */
object ZipUtils {
    private val MAGIC_BLOB =
        byteArrayOf('X'.code.toByte(), 'A'.code.toByte(), 'B'.code.toByte(), 'A'.code.toByte())
    private val MAGIC_COMPRESSED =
        byteArrayOf('X'.code.toByte(), 'A'.code.toByte(), 'L'.code.toByte(), 'Z'.code.toByte())

    fun fromBytes(bytes: ByteArray): Int {
        return bytes[0].toInt() and 255 or (bytes[1].toInt() and 255 shl 8) or (bytes[2].toInt() and 255 shl 16) or (bytes[3].toInt() and 255 shl 24)
    }

    fun decompressXALZ(bytes: ByteArray?): ByteArray? {
        if (bytes != null && Arrays.equals(ByteUtils.subArray(bytes, 0, 4), MAGIC_COMPRESSED)) {
            val length = ByteUtils.subArray(bytes, 8, 12)
            val len =
                length[0].toInt() and 255 or (length[1].toInt() and 255 shl 8) or (length[2].toInt() and 255 shl 16) or (length[3].toInt() and 255 shl 24)
            return LZ4Factory.fastestJavaInstance().fastDecompressor().decompress(bytes, 12, len)
        }
        return ByteArray(0)
    }

    fun unpackXABA(manifestBytes: ByteArray, xabaBytes: ByteArray): Map<String, ByteArray?> {
        val manifest = Splitter.on('\n').omitEmptyStrings().splitToList(
            String(
                manifestBytes, StandardCharsets.UTF_8
            )
        ).drop(1).map { line ->
            Splitter.on(
                CharMatcher.whitespace()
            ).omitEmptyStrings().splitToList(line)
        }.toList()
        val source = ByteSource.wrap(xabaBytes)
        val result: MutableMap<String, ByteArray?> = HashMap()
        try {
            var offset = 0
            var buffer = source.slice(offset.toLong(), 4).read()
            if (!Arrays.equals(buffer, MAGIC_BLOB)) {
                return result
            }
            buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
            val version = fromBytes(buffer)
            if (version > 1) {
                throw RuntimeException()
            }
            buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
            val lec = fromBytes(buffer)
            buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
            val gec = fromBytes(buffer)
            buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
            val storeId = fromBytes(buffer)
            for (i in 0 until lec) {
                val assembly = AssemblyStoreAssembly()
                buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
                assembly.dataOffset = fromBytes(buffer)
                buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
                assembly.dataSize = fromBytes(buffer)
                buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
                assembly.debugDataOffset = fromBytes(buffer)
                buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
                assembly.debugDataSize = fromBytes(buffer)
                buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
                assembly.configDataOffset = fromBytes(buffer)
                buffer = source.slice(4.let { offset += it; offset }.toLong(), 4).read()
                assembly.configDataSize = fromBytes(buffer)
                buffer = source.slice(assembly.dataOffset.toLong(), 4).read()
                var bytes: ByteArray?
                bytes = if (Arrays.equals(buffer, MAGIC_COMPRESSED)) {
                    val lzBytes =
                        source.slice(assembly.dataOffset.toLong(), assembly.dataSize.toLong())
                            .read()
                    decompressXALZ(lzBytes)
                } else {
                    source.slice(assembly.dataOffset.toLong(), assembly.dataSize.toLong()).read()
                }
                result[manifest[i][4] + ".dll"] = bytes
            }
        } catch (ignored: IOException) {
        }
        return result
    }

    @Throws(IOException::class)
    fun addOrReplaceEntries(
        inputZipFilename: String?,
        resourcePacks: Array<String?>?,
        entrySources: List<ZipEntrySource>,
        outputZipFilename: String?,
        removePredict: (String) -> Boolean,
        progressCallback: (Int) -> Unit
    ): Tuple2<ByteArray, Set<String>> {
        val inFile = inputZipFilename?.let { File(it).canonicalFile }
        val outFile = outputZipFilename?.let { File(it).canonicalFile }
        require(inFile != outFile) { "Input and output files are the same" }
        val entryMap = Maps.uniqueIndex(entrySources) { obj -> obj!!.path }
        var originManifest: ByteArray? = null
        val originEntryName = ConcurrentHashMap<String, Boolean>()
        try {
            ZipOutput(FileOutputStream(outputZipFilename)).use { zipOutput ->
                ZipInput(inputZipFilename).use { input ->
                    val size = input.entries.values.size
                    val count = AtomicLong()
                    val reportInterval = size / 100
                    val replacedFileSet = ConcurrentHashMap<String?, Boolean>(entryMap.size)
                    var taskBundle = MultiprocessingUtil.newTaskBundle { zioEntry: ZioEntry? ->
                        try {
                            zipOutput.write(zioEntry)
                            val index = count.incrementAndGet()
                            if (index % reportInterval == 0L) {
                                progressCallback.invoke((index * 95.0 / size).toInt())
                            }
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    }
                    val manifest = input.entries["META-INF/MANIFEST.MF"]
                    if (manifest != null) {
                        originManifest = manifest.data
                    }
                    for (inEntry in input.entries.values) {
                        if (removePredict.invoke(inEntry.name)) {
                            continue
                        }
                        taskBundle.submitTask {
                            val source = entryMap[inEntry.name]
                            if (source != null) {
                                val zioEntry = ZioEntry(inEntry.name)
                                zioEntry.setCompression(source.compressionMethod)
                                try {
                                    source.dataStream.use { inputStream ->
                                        if (inputStream != null) {
                                            ByteStreams.copy(inputStream, zioEntry.outputStream)
                                        }
                                    }
                                } catch (e: IOException) {
                                    throw RuntimeException(e)
                                }
                                replacedFileSet[inEntry.name] = true
                                return@submitTask zioEntry
                            } else {
                                originEntryName[inEntry.name] = true
                                return@submitTask inEntry
                            }
                        }
                    }
                    taskBundle.join()
                    val difference = Sets.difference(entryMap.keys, replacedFileSet.keys)
                    count.set(0)
                    taskBundle = MultiprocessingUtil.newTaskBundle { zioEntry: ZioEntry? ->
                        try {
                            zipOutput.write(zioEntry)
                            val index = count.incrementAndGet()
                            progressCallback.invoke(95 + (index * 5.0 / difference.size).toInt())
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    }
                    for (name in difference) {
                        taskBundle.submitTask {
                            entryMap[name]?.let { source ->
                                val zioEntry = ZioEntry(name)
                                zioEntry.setCompression(source.compressionMethod)
                                try {
                                    source.dataStream.use { inputStream ->
                                        inputStream?.let {
                                            ByteStreams.copy(
                                                it, zioEntry.outputStream
                                            )
                                        }
                                    }
                                } catch (e: IOException) {
                                    throw RuntimeException(e)
                                }
                                zioEntry
                            }
                        }
                    }
                    taskBundle.join()
                    progressCallback.invoke(100)
                }
                if (resourcePacks != null) {
                    for (resourcePack in resourcePacks) {
                        ZipInput(resourcePack).use { input ->
                            for (inEntry in input.entries.values) {
                                if (inEntry.name.startsWith("assets/Content")) {
                                    val zioEntry = ZioEntry(inEntry.name)
                                    zioEntry.setCompression(inEntry.compression.toInt())
                                    try {
                                        inEntry.inputStream.use { inputStream ->
                                            ByteStreams.copy(
                                                inputStream, zioEntry.outputStream
                                            )
                                        }
                                    } catch (e: IOException) {
                                        throw RuntimeException(e)
                                    }
                                    zipOutput.write(zioEntry)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: RuntimeException) {
            if (e.cause != null && e.cause is IOException) {
                throw e.cause as IOException
            }
            throw e
        }
        return Tuple2(originManifest, originEntryName.keys)
    }

    @Throws(IOException::class)
    fun removeEntries(
        inputZipFilename: String?,
        prefix: String,
        outputZipFilename: String?,
        progressCallback: (Int?) -> Unit
    ) {
        val inFile = inputZipFilename?.let { File(it).canonicalFile }
        val outFile = outputZipFilename?.let { File(it).canonicalFile }
        require(inFile != outFile) { "Input and output files are the same" }
        ZipInput(inputZipFilename).use { input ->
            val size = input.entries.values.size
            var index = 0
            val reportInterval = size / 100
            ZipOutput(FileOutputStream(outFile)).use { zipOutput ->
                for (inEntry in input.entries.values) {
                    if (!inEntry.name.startsWith(prefix)) {
                        zipOutput.write(inEntry)
                    }
                    index++
                    if (index % reportInterval == 0) {
                        progressCallback.invoke((index * 100.0 / size).toInt())
                    }
                }
                progressCallback.invoke(100)
            }
        }
    }

    class ZipEntrySource {
        var path: String
        var compressionMethod: Int
        var dataSupplier: (() -> InputStream?)? = null

        constructor(path: String, bytes: ByteArray?, compressionMethod: Int) {
            this.path = path
            this.compressionMethod = compressionMethod
            dataSupplier = { ByteArrayInputStream(bytes) }
        }

        val dataStream: InputStream?
            get() = dataSupplier?.invoke()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ZipEntrySource

            if (path != other.path) return false

            return true
        }

        override fun hashCode(): Int {
            return path.hashCode()
        }

        constructor(path: String, compressionMethod: Int, dataSupplier: (() -> InputStream?)?) {
            this.path = path
            this.compressionMethod = compressionMethod
            this.dataSupplier = dataSupplier
        }

    }
}