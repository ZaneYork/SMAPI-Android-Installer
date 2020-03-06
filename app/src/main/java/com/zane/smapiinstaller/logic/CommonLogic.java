package com.zane.smapiinstaller.logic;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;

import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;

public class CommonLogic {

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
        if(file.exists()){
            return new FileInputStream(file);
        }
        return context.getAssets().open(filename);
    }

    public static <T> T getFileJson(File file, Type type) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(CharStreams.toString(reader), type);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static <T> T getFileJson(File file, Class<T> tClass) {
        try {
            InputStream inputStream = new FileInputStream(file);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(CharStreams.toString(reader), tClass);
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
                writer.write(new Gson().toJson(content));
            }
            finally {
                FileUtils.moveFile(file, new File(context.getFilesDir(), filename));
            }
        } catch (Exception ignored) {
        }
    }

    public static <T> T getAssetJson(Context context, String filename, Class<T> tClass) {
        try {
            InputStream inputStream = getLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(CharStreams.toString(reader), tClass);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static <T> T getAssetJson(Context context, String filename, Type type) {
        try {
            InputStream inputStream = getLocalAsset(context, filename);
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(CharStreams.toString(reader), type);
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

    public static void setProgressDialogState(View view, MaterialDialog dialog, int message, int progress) {
        Activity activity = getActivityFromView(view);
        if(activity != null && !activity.isFinishing() && !dialog.isCancelled()) {
            activity.runOnUiThread(()->{
                dialog.incrementProgress(progress - dialog.getCurrentProgress());
                dialog.setContent(message);
            });
        }
    }

    public static Activity getActivityFromView(View view) {
        if (null != view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        return null;
    }

    public static void showAlertDialog(View view, int title, String message) {
        Activity activity = getActivityFromView(view);
        if(activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(()-> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.ok).show());
        }
    }
    public static void showAlertDialog(View view, int title, int message) {
        Activity activity = getActivityFromView(view);
        if(activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(()-> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.ok).show());
        }
    }

    public static void showConfirmDialog(View view, int title, int message, MaterialDialog.SingleButtonCallback callback) {
        Activity activity = getActivityFromView(view);
        if(activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(()-> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.confirm).negativeText(R.string.cancel).onAny(callback).show());
        }
    }

    public static void showConfirmDialog(View view, int title, String message, MaterialDialog.SingleButtonCallback callback) {
        Activity activity = getActivityFromView(view);
        if(activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(()-> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.confirm).negativeText(R.string.cancel).onAny(callback).show());
        }
    }

    public static List<ApkFilesManifest> findAllApkFileManifest(Context context) {
        ApkFilesManifest apkFilesManifest = CommonLogic.getAssetJson(context, "apk_files_manifest.json", ApkFilesManifest.class);
        ArrayList<ApkFilesManifest> apkFilesManifests = Lists.newArrayList(apkFilesManifest);
        File compatFolder = new File(context.getFilesDir(), "compat");
        if(compatFolder.exists()) {
            for (File directory : compatFolder.listFiles(File::isDirectory)) {
                File manifestFile = new File(directory, "apk_files_manifest.json");
                if(manifestFile.exists()) {
                    ApkFilesManifest manifest = getFileJson(manifestFile, ApkFilesManifest.class);
                    if(manifest != null) {
                        apkFilesManifests.add(manifest);
                    }
                }
            }
        }
        Collections.sort(apkFilesManifests, (a, b)-> Long.compare(b.getMinBuildCode(), a.getMinBuildCode()));
        return apkFilesManifests;
    }

    public static boolean unpackSmapiFiles(Context context, String apkPath, boolean checkMod) {
        List<ManifestEntry> manifestEntries = CommonLogic.getAssetJson(context, "smapi_files_manifest.json", new TypeToken<List<ManifestEntry>>() {
        }.getType());
        if(manifestEntries == null)
            return false;
        File basePath = new File(Environment.getExternalStorageDirectory() + "/StardewValley/");
        if(!basePath.exists()) {
            if(!basePath.mkdir()) {
                return false;
            }
        }
        File noMedia = new File(basePath,".nomedia");
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (IOException ignored) {
            }
        }
        for (ManifestEntry entry : manifestEntries) {
            File targetFile = new File(basePath, entry.getTargetPath());
            switch (entry.getOrigin()) {
                case 0:
                    if(!checkMod || !targetFile.exists()) {
                        try (InputStream inputStream = context.getAssets().open(entry.getAssetPath())) {
                            if (!targetFile.getParentFile().exists()) {
                                if (!targetFile.getParentFile().mkdirs()) {
                                    return false;
                                }
                            }
                            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                                ByteStreams.copy(inputStream, outputStream);
                            }
                        } catch (IOException e) {
                            Log.e("COMMON", "Copy Error", e);
                        }
                    }
                    break;
                case 1:
                    if(!checkMod || !targetFile.exists()) {
                        ZipUtil.unpackEntry(new File(apkPath), entry.getAssetPath(), targetFile);
                    }
                    break;
            }
        }
        return true;
    }

    public static void openUrl(Context context, String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        context.startActivity(intent);
    }

    public static byte[] modifyManifest(byte[] bytes, Predicate<ManifestTagVisitor.AttrArgs> processLogic) throws IOException {
        AxmlReader reader = new AxmlReader(bytes);
        AxmlWriter writer = new AxmlWriter();
        reader.accept(new AxmlVisitor(writer) {
            @Override
            public NodeVisitor child(String ns, String name) {
                NodeVisitor child = super.child(ns, name);
                return new ManifestTagVisitor(child, processLogic);
            }
        });
        return writer.toByteArray();
    }

    public static String toPrettyPath(String path) {
        return StringUtils.removeStart(path, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static String getFileHash(Context context, String filename) {
        try(InputStream inputStream = getLocalAsset(context, filename)){
            return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString();
        } catch (IOException ignored) {
        }
        return null;
    }

    public static String getFileHash(File file) {
        try(InputStream inputStream = new FileInputStream(file)){
            return Hashing.sha256().hashBytes(ByteStreams.toByteArray(inputStream)).toString();
        } catch (IOException ignored) {
        }
        return null;
    }

}
