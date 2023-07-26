package com.zane.smapiinstaller.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.documentfile.provider.DocumentUtils
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import com.google.common.io.CharStreams
import com.google.common.io.Files
import com.hjq.language.MultiLanguages
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.logic.CommonLogic.checkDataRootPermission
import com.zane.smapiinstaller.logic.CommonLogic.pathToTreeUri
import org.apache.commons.io.input.BOMInputStream
import org.apache.commons.lang3.StringUtils
import org.zeroturnaround.zip.commons.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * 文件工具类
 *
 * @author Zane
 */
object FileUtils : FileUtils() {
    /**
     * 读取文本文件
     *
     * @param file 文件
     * @return 文本
     */
    fun getFileText(file: File?): String? {
        try {
            val inputStream: InputStream = BOMInputStream(FileInputStream(file))
            InputStreamReader(
                inputStream, StandardCharsets.UTF_8
            ).use { reader -> return CharStreams.toString(reader) }
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 读取本地资源或Asset资源
     *
     * @param context  context
     * @param filename 文件名
     * @return 输入流
     * @throws IOException 异常
     */
    @Throws(IOException::class)
    fun getLocalAsset(context: Context, filename: String): InputStream {
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            BOMInputStream(FileInputStream(file))
        } else context.assets.open(filename)
    }

    /**
     * 尝试获取本地化后的资源文件
     *
     * @param context  context
     * @param filename 文件名
     * @return 输入流
     * @throws IOException 异常
     */
    @Throws(IOException::class)
    fun getLocaledLocalAsset(context: Context, filename: String): InputStream {
        try {
            val language = MultiLanguages.getAppLanguage().language
            val localedFilename = "$filename.$language"
            val file = File(context.filesDir, localedFilename)
            return if (file.exists()) {
                BOMInputStream(FileInputStream(file))
            } else context.assets.open(localedFilename)
        } catch (e: IOException) {
            Log.d("LOCALE", "No locale asset found", e)
        }
        return getLocalAsset(context, filename)
    }

    /**
     * 读取JSON文件
     *
     * @param file 文件
     * @param type 数据类型
     * @param <T>  泛型类型
     * @return 数据
    </T> */
    fun <T> getFileJson(file: File?, type: TypeReference<T>?): T? {
        try {
            val inputStream: InputStream = FileInputStream(file)
            InputStreamReader(
                BOMInputStream(inputStream), StandardCharsets.UTF_8
            ).use { reader -> return JsonUtil.fromJson(CharStreams.toString(reader), type) }
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 读取JSON文件
     *
     * @param file   文件
     * @param tClass 数据类型
     * @param <T>    泛型类型
     * @return 数据
    </T> */
    fun <T> getFileJson(file: File?, tClass: Class<T>?): T? {
        try {
            val inputStream: InputStream = FileInputStream(file)
            InputStreamReader(
                BOMInputStream(inputStream), StandardCharsets.UTF_8
            ).use { reader -> return JsonUtil.fromJson(CharStreams.toString(reader), tClass) }
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 写入JSON文件到本地
     *
     * @param context  context
     * @param filename 文件名
     * @param content  内容
     */
    fun writeAssetJson(context: Context, filename: String, content: Any?) {
        try {
            val tmpFilename = "$filename.tmp"
            val file = File(context.filesDir, tmpFilename)
            val outputStream = FileOutputStream(file)
            try {
                OutputStreamWriter(
                    outputStream, StandardCharsets.UTF_8
                ).use { writer -> writer.write(JsonUtil.toJson(content)) }
            } finally {
                val distFile = File(context.filesDir, filename)
                if (distFile.exists()) {
                    forceDelete(distFile)
                }
                moveFile(file, distFile)
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 写入JSON文件到本地
     *
     * @param file    文件
     * @param content 内容
     */
    fun writeFileJson(file: File, content: Any?) {
        try {
            file.parentFile?.let {
                if (!it.exists()) {
                    forceMkdir(it)
                }
            }
            val filename = file.name
            val tmpFilename = "$filename.tmp"
            val fileTmp = File(file.parent, tmpFilename)
            val outputStream = FileOutputStream(fileTmp)
            try {
                OutputStreamWriter(
                    outputStream, StandardCharsets.UTF_8
                ).use { writer -> writer.write(JsonUtil.toJson(content)) }
            } finally {
                if (file.exists()) {
                    forceDelete(file)
                }
                moveFile(fileTmp, file)
            }
        } catch (ignored: Exception) {
        }
    }

    /**
     * 读取资源文本
     *
     * @param context  context
     * @param filename 文件名
     * @return 文本
     */
    fun getAssetText(context: Context, filename: String): String? {
        try {
            val inputStream = getLocalAsset(context, filename)
            InputStreamReader(
                inputStream, StandardCharsets.UTF_8
            ).use { reader -> return CharStreams.toString(reader) }
        } catch (ignored: IOException) {
        }
        return null
    }

    /**
     * 读取本地化后的资源文本
     *
     * @param context  context
     * @param filename 文件名
     * @return 文本
     */
    fun getLocaledAssetText(context: Context, filename: String): String? {
        try {
            val inputStream = getLocaledLocalAsset(context, filename)
            InputStreamReader(
                inputStream, StandardCharsets.UTF_8
            ).use { reader -> return CharStreams.toString(reader) }
        } catch (ignored: IOException) {
        }
        return null
    }

    /**
     * 读取JSON资源
     *
     * @param context  context
     * @param filename 资源名
     * @param tClass   数据类型
     * @param <T>      泛型类型
     * @return 数据
    </T> */
    fun <T> getAssetJson(context: Context, filename: String, tClass: Class<T>?): T? {
        val text = getAssetText(context, filename)
        return if (text != null) {
            JsonUtil.fromJson(text, tClass)
        } else null
    }

    fun <T> getLocaledAssetJson(context: Context, filename: String, tClass: Class<T>?): T? {
        try {
            val inputStream = getLocaledLocalAsset(context, filename)
            InputStreamReader(
                inputStream, StandardCharsets.UTF_8
            ).use { reader -> return JsonUtil.fromJson(CharStreams.toString(reader), tClass) }
        } catch (ignored: IOException) {
        }
        return null
    }

    /**
     * 读取资源为字节数组
     *
     * @param context  context
     * @param filename 文件名
     * @return 字节数组
     */
    fun getAssetBytes(context: Context, filename: String): ByteArray {
        try {
            getLocalAsset(context, filename).use { inputStream ->
                return ByteStreams.toByteArray(
                    inputStream
                )
            }
        } catch (ignored: IOException) {
        }
        return ByteArray(0)
    }

    /**
     * 读取JSON资源
     *
     * @param context  context
     * @param filename 资源名
     * @param type     数据类型
     * @param <T>      泛型类型
     * @return 数据
    </T> */
    fun <T> getAssetJson(context: Context, filename: String, type: TypeReference<T>): T? {
        val text = getAssetText(context, filename)
        return if (text != null) {
            JsonUtil.fromJson(text, type)
        } else null
    }

    /**
     * 简化路径前缀
     *
     * @param path 文件路径
     * @return 移除前缀后的路径
     */
    fun toPrettyPath(path: String?): String {
        return StringUtils.removeStart(path, stadewValleyBasePath)
    }

    /**
     * 计算资源文件SHA3-256
     *
     * @param context  context
     * @param filename 资源名
     * @return SHA3-256值
     */
    fun getFileHash(context: Context, filename: String): String? {
        try {
            getLocalAsset(context, filename).use { inputStream ->
                return Hashing.sha256().hashBytes(
                    ByteStreams.toByteArray(inputStream)
                ).toString()
            }
        } catch (ignored: IOException) {
        }
        return null
    }

    /**
     * 计算文件SHA3-256
     *
     * @param file 文件
     * @return SHA3-256值
     */
    fun getFileHash(file: File?): String? {
        try {
            FileInputStream(file).use { inputStream ->
                return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString()
            }
        } catch (ignored: IOException) {
        }
        return null
    }

    val stadewValleyBasePath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    fun listAll(basePath: String, filter: (File) -> Boolean): MutableList<String> {
        return Files.fileTraverser().breadthFirst(File(basePath)).filterNotNull().filter(filter)
            .map { obj -> obj.absolutePath }.toMutableList()
    }

    fun docOverlayFetch(context: Context, relativePath: String): File {
        val trimmedRelativePath = relativePath.replace("StardewValley/", "")
        if (checkDataRootPermission(context)) {
            val targetDirUri = pathToTreeUri(Constants.TARGET_DATA_FILE_URI)
            val documentFile = DocumentFile.fromTreeUri(context, targetDirUri)
            val filesDoc = DocumentUtils.findFile(context, documentFile, "files")
            if (filesDoc != null) {
                val split =
                    trimmedRelativePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var currentDoc = filesDoc
                for (path in split) {
                    currentDoc = DocumentUtils.findFile(context, currentDoc, path)
                    if (currentDoc == null) {
                        break
                    }
                }
                if (currentDoc != null && currentDoc.isFile) {
                    try {
                        context.contentResolver.openInputStream(currentDoc.uri).use { inputStream ->
                            val tempFile = currentDoc.name?.let { File.createTempFile(it, null) } ?: File.createTempFile("tmp_", null)
                            tempFile.deleteOnExit()
                            copy(inputStream, tempFile)
                            return tempFile
                        }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
        return File(stadewValleyBasePath, trimmedRelativePath)
    }
}