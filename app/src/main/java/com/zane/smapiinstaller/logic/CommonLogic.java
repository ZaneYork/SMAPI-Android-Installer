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
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;

public class CommonLogic {

    public static void setProgressDialogState(View view, MaterialDialog dialog, int message, int progress) {
        Activity activity = getActivityFromView(view);
        if (activity != null && !activity.isFinishing() && !dialog.isCancelled()) {
            activity.runOnUiThread(() -> {
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
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.ok).show());
        }
    }

    public static void showAlertDialog(View view, int title, int message) {
        Activity activity = getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.ok).show());
        }
    }

    public static void showConfirmDialog(View view, int title, int message, MaterialDialog.SingleButtonCallback callback) {
        Activity activity = getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.confirm).negativeText(R.string.cancel).onAny(callback).show());
        }
    }

    public static void showConfirmDialog(View view, int title, String message, MaterialDialog.SingleButtonCallback callback) {
        Activity activity = getActivityFromView(view);
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> new MaterialDialog.Builder(activity).title(title).content(message).positiveText(R.string.confirm).negativeText(R.string.cancel).onAny(callback).show());
        }
    }

    public static AtomicReference<MaterialDialog> showProgressDialog(View view, int title, String message) {
        Activity activity = getActivityFromView(view);
        AtomicReference<MaterialDialog> reference = new AtomicReference<>();
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                MaterialDialog dialog = new MaterialDialog.Builder(activity)
                        .title(title)
                        .content(message)
                        .progress(false, 100, true)
                        .cancelable(false)
                        .show();
                reference.set(dialog);
            });
        }
        return reference;
    }

    public static void openUrl(Context context, String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        context.startActivity(intent);
    }

    public static List<ApkFilesManifest> findAllApkFileManifest(Context context) {
        ApkFilesManifest apkFilesManifest = com.zane.smapiinstaller.utils.FileUtils.getAssetJson(context, "apk_files_manifest.json", ApkFilesManifest.class);
        ArrayList<ApkFilesManifest> apkFilesManifests = Lists.newArrayList(apkFilesManifest);
        File compatFolder = new File(context.getFilesDir(), "compat");
        if (compatFolder.exists()) {
            for (File directory : compatFolder.listFiles(File::isDirectory)) {
                File manifestFile = new File(directory, "apk_files_manifest.json");
                if (manifestFile.exists()) {
                    ApkFilesManifest manifest = com.zane.smapiinstaller.utils.FileUtils.getFileJson(manifestFile, ApkFilesManifest.class);
                    if (manifest != null) {
                        apkFilesManifests.add(manifest);
                    }
                }
            }
        }
        Collections.sort(apkFilesManifests, (a, b) -> Long.compare(b.getMinBuildCode(), a.getMinBuildCode()));
        return apkFilesManifests;
    }

    public static boolean unpackSmapiFiles(Context context, String apkPath, boolean checkMod) {
        List<ManifestEntry> manifestEntries = com.zane.smapiinstaller.utils.FileUtils.getAssetJson(context, "smapi_files_manifest.json", new TypeReference<List<ManifestEntry>>() { });
        if (manifestEntries == null)
            return false;
        File basePath = new File(Environment.getExternalStorageDirectory() + "/StardewValley/");
        if (!basePath.exists()) {
            if (!basePath.mkdir()) {
                return false;
            }
        }
        File noMedia = new File(basePath, ".nomedia");
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
                    if (!checkMod || !targetFile.exists()) {
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
                    if (!checkMod || !targetFile.exists()) {
                        ZipUtil.unpackEntry(new File(apkPath), entry.getAssetPath(), targetFile);
                    }
                    break;
            }
        }
        return true;
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

}
