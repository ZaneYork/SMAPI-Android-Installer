package com.zane.smapiinstaller.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.hjq.language.LanguagesManager;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 文件工具类
 *
 * @author Zane
 */
public class FileUtils extends org.zeroturnaround.zip.commons.FileUtils {
    /**
     * 读取文本文件
     *
     * @param file 文件
     * @return 文本
     */
    public static String getFileText(File file) {
        try {
            InputStream inputStream = new BOMInputStream(new FileInputStream(file));
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return CharStreams.toString(reader);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 读取本地资源或Asset资源
     *
     * @param context  context
     * @param filename 文件名
     * @return 输入流
     * @throws IOException 异常
     */
    public static InputStream getLocalAsset(Context context, String filename) throws IOException {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            return new BOMInputStream(new FileInputStream(file));
        }
        return context.getAssets().open(filename);
    }

    /**
     * 尝试获取本地化后的资源文件
     *
     * @param context  context
     * @param filename 文件名
     * @return 输入流
     * @throws IOException 异常
     */
    public static InputStream getLocaledLocalAsset(Context context, String filename) throws IOException {
        try {
            String language = LanguagesManager.getAppLanguage(context).getLanguage();
            String localedFilename = filename + '.' + language;
            File file = new File(context.getFilesDir(), localedFilename);
            if (file.exists()) {
                return new BOMInputStream(new FileInputStream(file));
            }
            return context.getAssets().open(localedFilename);
        } catch (IOException e) {
            Log.d("LOCALE", "No locale asset found", e);
        }
        return getLocalAsset(context, filename);
    }

    /**
     * 读取JSON文件
     *
     * @param file 文件
     * @param type 数据类型
     * @param <T>  泛型类型
     * @return 数据
     */
    public static <T> T getFileJson(File file, TypeReference<T> type) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8)) {
                return JsonUtil.fromJson(CharStreams.toString(reader), type);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 读取JSON文件
     *
     * @param file   文件
     * @param tClass 数据类型
     * @param <T>    泛型类型
     * @return 数据
     */
    public static <T> T getFileJson(File file, Class<T> tClass) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8)) {
                return JsonUtil.fromJson(CharStreams.toString(reader), tClass);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 写入JSON文件到本地
     *
     * @param context  context
     * @param filename 文件名
     * @param content  内容
     */
    public static void writeAssetJson(Context context, String filename, Object content) {
        try {
            String tmpFilename = filename + ".tmp";
            File file = new File(context.getFilesDir(), tmpFilename);
            FileOutputStream outputStream = new FileOutputStream(file);
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writer.write(JsonUtil.toJson(content));
            } finally {
                File distFile = new File(context.getFilesDir(), filename);
                if (distFile.exists()) {
                    org.zeroturnaround.zip.commons.FileUtils.forceDelete(distFile);
                }
                org.zeroturnaround.zip.commons.FileUtils.moveFile(file, distFile);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 写入JSON文件到本地
     *
     * @param file    文件
     * @param content 内容
     */
    public static void writeFileJson(File file, Object content) {
        try {
            if (!file.getParentFile().exists()) {
                org.zeroturnaround.zip.commons.FileUtils.forceMkdir(file.getParentFile());
            }
            String filename = file.getName();
            String tmpFilename = filename + ".tmp";
            File fileTmp = new File(file.getParent(), tmpFilename);
            FileOutputStream outputStream = new FileOutputStream(fileTmp);
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writer.write(JsonUtil.toJson(content));
            } finally {
                if (file.exists()) {
                    org.zeroturnaround.zip.commons.FileUtils.forceDelete(file);
                }
                org.zeroturnaround.zip.commons.FileUtils.moveFile(fileTmp, file);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 读取资源文本
     *
     * @param context  context
     * @param filename 文件名
     * @return 文本
     */
    public static String getAssetText(Context context, String filename) {
        try {
            InputStream inputStream = getLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return CharStreams.toString(reader);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 读取本地化后的资源文本
     *
     * @param context  context
     * @param filename 文件名
     * @return 文本
     */
    public static String getLocaledAssetText(Context context, String filename) {
        try {
            InputStream inputStream = getLocaledLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return CharStreams.toString(reader);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 读取JSON资源
     *
     * @param context  context
     * @param filename 资源名
     * @param tClass   数据类型
     * @param <T>      泛型类型
     * @return 数据
     */
    public static <T> T getAssetJson(Context context, String filename, Class<T> tClass) {
        String text = getAssetText(context, filename);
        if (text != null) {
            return JsonUtil.fromJson(text, tClass);
        }
        return null;
    }

    public static <T> T getLocaledAssetJson(Context context, String filename, Class<T> tClass) {
        try {
            InputStream inputStream = getLocaledLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JsonUtil.fromJson(CharStreams.toString(reader), tClass);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 读取JSON资源
     *
     * @param context  context
     * @param filename 资源名
     * @param type     数据类型
     * @param <T>      泛型类型
     * @return 数据
     */
    public static <T> T getAssetJson(Context context, String filename, TypeReference<T> type) {
        String text = getAssetText(context, filename);
        if (text != null) {
            return JsonUtil.fromJson(text, type);
        }
        return null;
    }

    /**
     * 读取资源为字节数组
     *
     * @param context  context
     * @param filename 文件名
     * @return 字节数组
     */
    public static byte[] getAssetBytes(Context context, String filename) {
        try {
            try (InputStream inputStream = getLocalAsset(context, filename)) {
                return ByteStreams.toByteArray(inputStream);
            }
        } catch (IOException ignored) {
        }
        return new byte[0];
    }

    /**
     * 简化路径前缀
     *
     * @param path 文件路径
     * @return 移除前缀后的路径
     */
    public static String toPrettyPath(String path) {
        return StringUtils.removeStart(path, FileUtils.getStadewValleyBasePath());
    }

    /**
     * 计算资源文件SHA3-256
     *
     * @param context  context
     * @param filename 资源名
     * @return SHA3-256值
     */
    public static String getFileHash(Context context, String filename) {
        try (InputStream inputStream = getLocalAsset(context, filename)) {
            return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString();
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 计算文件SHA3-256
     *
     * @param file 文件
     * @return SHA3-256值
     */
    public static String getFileHash(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString();
        } catch (IOException ignored) {
        }
        return null;
    }

    public static String getStadewValleyBasePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static List<String> listAll(String basePath, Predicate<File> filter) {
        return Lists.newArrayList(
                Iterables.transform(
                        Iterables.filter(Files.fileTraverser().breadthFirst(new File(basePath)), filter::test),
                        File::getAbsolutePath)
        );
    }
}
