package com.zane.smapiinstaller.logic;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;
import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;
import com.zane.smapiinstaller.utils.FileUtils;

import net.fornwall.apksigner.KeyStoreFileManager;
import net.fornwall.apksigner.ZipSigner;

import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;

import androidx.core.content.FileProvider;
import pxb.android.axml.NodeVisitor;

public class ApkPatcher {

    private static final String PASSWORD = "android";

    private final Context context;

    private static final String TAG = "PATCHER";

    private AtomicReference<String> errorMessage = new AtomicReference<>();

    public ApkPatcher(Context context) {
        this.context = context;
    }

    public String extract() {
        PackageManager packageManager = context.getPackageManager();
        List<String> packageNames = FileUtils.getAssetJson(context, "package_names.json", new TypeToken<List<String>>() {
        }.getType());
        if (packageNames == null) {
            errorMessage.set(context.getString(R.string.error_game_not_found));
            return null;
        }
        for (String packageName : packageNames) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                String sourceDir = packageInfo.applicationInfo.publicSourceDir;
                File apkFile = new File(sourceDir);

                File externalFilesDir = Environment.getExternalStorageDirectory();
                if (externalFilesDir != null) {
                    File dest = new File(externalFilesDir.getAbsolutePath() + "/SMAPI Installer/");
                    if (!dest.exists()) {
                        if (!dest.mkdir()) {
                            errorMessage.set(String.format(context.getString(R.string.error_failed_to_create_file), dest.getAbsolutePath()));
                            return null;
                        }
                    }
                    File distFile = new File(dest, apkFile.getName());
                    Files.copy(apkFile, distFile);
                    return distFile.getAbsolutePath();
                }
            } catch (PackageManager.NameNotFoundException | IOException e) {
                Log.e(TAG, "Extract error", e);
                errorMessage.set(e.getLocalizedMessage());
            }
        }
        return null;
    }

    public boolean patch(String apkPath) {
        if (apkPath == null)
            return false;
        File file = new File(apkPath);
        if (!file.exists())
            return false;
        try {
            List<ZipEntrySource> zipEntrySourceList = new ArrayList<>();
            byte[] manifest = ZipUtil.unpackEntry(file, "AndroidManifest.xml");
            List<ApkFilesManifest> apkFilesManifests = CommonLogic.findAllApkFileManifest(context);
            byte[] modifiedManifest = modifyManifest(manifest, apkFilesManifests);
            if(apkFilesManifests.size() == 0) {
                errorMessage.set(context.getString(R.string.error_no_supported_game_version));
                return false;
            }
            if(modifiedManifest == null) {
                errorMessage.set(context.getString(R.string.failed_to_process_manifest));
                return false;
            }
            zipEntrySourceList.add(new ByteSource("AndroidManifest.xml", modifiedManifest, Deflater.DEFLATED));
            ApkFilesManifest apkFilesManifest = apkFilesManifests.get(0);
            List<ManifestEntry> manifestEntries = apkFilesManifest.getManifestEntries();
            for (ManifestEntry entry : manifestEntries) {
                if(entry.isExternal()) {
                    zipEntrySourceList.add(new ByteSource(entry.getTargetPath(), FileUtils.getAssetBytes(context, apkFilesManifest.getBasePath() + entry.getAssetPath()), entry.getCompression()));
                }
                else {
                    zipEntrySourceList.add(new ByteSource(entry.getTargetPath(), FileUtils.getAssetBytes(context, entry.getAssetPath()), entry.getCompression()));
                }
            }
            ZipUtil.addOrReplaceEntries(file, zipEntrySourceList.toArray(new ZipEntrySource[0]));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Patch error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
        return false;
    }

    private byte[] modifyManifest(byte[] bytes, List<ApkFilesManifest> manifests) {
        AtomicReference<String> packageName = new AtomicReference<>();
        Predicate<ManifestTagVisitor.AttrArgs> processLogic = (attr) -> {
            if (attr.type == NodeVisitor.TYPE_STRING) {
                String strObj = (String) attr.obj;
                switch (attr.name) {
                    case "package":
                        if (packageName.get() == null) {
                            packageName.set(strObj);
                            attr.obj = Constants.TARGET_PACKAGE_NAME;
                        }
                        break;
                    case "label":
                        if (strObj.contains("Stardew Valley")) {
                            attr.obj = context.getString(R.string.smapi_game_name);
                        }
                        break;
                    case "authorities":
                        if (strObj.contains(packageName.get())) {
                            attr.obj = strObj.replace(packageName.get(), Constants.TARGET_PACKAGE_NAME);
                        }
                    case "name":
                        if (strObj.contains(".MainActivity")) {
                            attr.obj = strObj.replaceFirst("\\w+\\.MainActivity", "md5723872fa9a204f7f942686e9ed9d0b7d.SMainActivity");
                        }
                        break;
                }
            }
            else if(attr.type == NodeVisitor.TYPE_FIRST_INT) {
                if(StringUtils.equals(attr.name, "versionCode")){
                    long versionCode = (int) attr.obj;
                    Iterables.removeIf(manifests, manifest -> {
                        if (versionCode < manifest.getMinBuildCode()) {
                            return true;
                        }
                        if (manifest.getMaxBuildCode() != null) {
                            if (versionCode > manifest.getMaxBuildCode()) {
                                return true;
                            }
                        }
                        return false;
                    });
                }
            }
            return true;
        };
        try {
            return CommonLogic.modifyManifest(bytes, processLogic);
        }catch (Exception e) {
            errorMessage.set(e.getLocalizedMessage());
            return null;
        }
    }

    public String sign(String apkPath) {
        try {
            File externalFilesDir = Environment.getExternalStorageDirectory();
            if (externalFilesDir != null) {
                String signApkPath = externalFilesDir.getAbsolutePath() + "/SMAPI Installer/base_signed.apk";
                KeyStore ks = new KeyStoreFileManager.JksKeyStore();
                try (InputStream fis = context.getAssets().open("debug.keystore")) {
                    ks.load(fis, PASSWORD.toCharArray());
                }
                String alias = ks.aliases().nextElement();
                X509Certificate publicKey = (X509Certificate) ks.getCertificate(alias);
                PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "android".toCharArray());
                ZipSigner.signZip(publicKey, privateKey, "SHA1withRSA", apkPath, signApkPath);
                new File(apkPath).delete();
                return signApkPath;
            }
        } catch (Exception e) {
            Log.e(TAG, "Sign error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
        return null;
    }

    public void install(String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Install error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
    }

    /**
     * Gets the URI from a file
     *
     * @param file = The file to try and get the URI from
     * @return The URI for the file
     */
    private Uri fromFile(File file) {
        //Android versions greater than Nougat use FileProvider, others use the URI.fromFile.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        else
            return Uri.fromFile(file);
    }

    public AtomicReference<String> getErrorMessage() {
        return errorMessage;
    }
}
