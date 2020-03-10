package com.zane.smapiinstaller.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String getFileText(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return CharStreams.toString(reader);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static InputStream getLocalAsset(Context context, String filename) throws IOException {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            return new BOMInputStream(new FileInputStream(file));
        }
        return context.getAssets().open(filename);
    }

    public static <T> T getFileJson(File file, TypeReference<T> type) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8)) {
                return JSONUtil.fromJson(CharStreams.toString(reader), type);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static <T> T getFileJson(File file, Class<T> tClass) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8)) {
                return JSONUtil.fromJson(CharStreams.toString(reader), tClass);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void writeAssetJson(Context context, String filename, Object content) {
        try {
            String tmpFilename = filename + ".tmp";
            File file = new File(context.getFilesDir(), tmpFilename);
            FileOutputStream outputStream = new FileOutputStream(file);
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writer.write(JSONUtil.toJson(content));
            } finally {
                org.zeroturnaround.zip.commons.FileUtils.moveFile(file, new File(context.getFilesDir(), filename));
            }
        } catch (Exception ignored) {
        }
    }

    public static <T> T getAssetJson(Context context, String filename, Class<T> tClass) {
        try {
            InputStream inputStream = getLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JSONUtil.fromJson(CharStreams.toString(reader), tClass);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static <T> T getAssetJson(Context context, String filename, TypeReference<T> type) {
        try {
            InputStream inputStream = getLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JSONUtil.fromJson(CharStreams.toString(reader), type);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static byte[] getAssetBytes(Context context, String filename) {
        try {
            try (InputStream inputStream = getLocalAsset(context, filename)) {
                return ByteStreams.toByteArray(inputStream);
            }
        } catch (IOException ignored) {
        }
        return new byte[0];
    }

    public static String toPrettyPath(String path) {
        return StringUtils.removeStart(path, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static String getFileHash(Context context, String filename) {
        try (InputStream inputStream = getLocalAsset(context, filename)) {
            return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString();
        } catch (IOException ignored) {
        }
        return null;
    }

    public static String getFileHash(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString();
        } catch (IOException ignored) {
        }
        return null;
    }
}
