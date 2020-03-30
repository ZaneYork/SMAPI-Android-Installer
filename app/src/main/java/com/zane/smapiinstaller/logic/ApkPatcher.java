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

import com.android.apksig.ApkSigner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.ManifestPatchConstants;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.ZipUtils;

import net.fornwall.apksigner.KeyStoreFileManager;

import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import java9.util.function.Consumer;
import java9.util.stream.Collectors;

import java.util.zip.Deflater;

import androidx.core.content.FileProvider;
import java9.util.stream.StreamSupport;
import pxb.android.axml.NodeVisitor;

/**
 * @author Zane
 */
public class ApkPatcher {

    private static final String PASSWORD = "android";

    private final Context context;

    private static final String TAG = "PATCHER";

    private final AtomicReference<String> errorMessage = new AtomicReference<>();

    private final AtomicInteger switchAction = new AtomicInteger();

    private final List<Consumer<Integer>> progressListener = new ArrayList<>();

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    public ApkPatcher(Context context) {
        this.context = context;
    }

    /**
     * 依次扫描package_names.json文件对应的包名，抽取找到的第一个游戏APK到SMAPI Installer路径
     *
     * @return 抽取后的APK文件路径，如果抽取失败返回null
     */
    public String extract() {
        emitProgress(0);
        PackageManager packageManager = context.getPackageManager();
        List<String> packageNames = FileUtils.getAssetJson(context, "package_names.json", new TypeReference<List<String>>() {
        });
        if (packageNames == null) {
            errorMessage.set(context.getString(R.string.error_game_not_found));
            return null;
        }
        emitProgress(1);
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
                    emitProgress(5);
                    return distFile.getAbsolutePath();
                }
            } catch (PackageManager.NameNotFoundException | IOException e) {
                Log.e(TAG, "Extract error", e);
            }
        }
        errorMessage.set(context.getString(R.string.error_game_not_found));
        return null;
    }

    /**
     * 将指定APK文件重新打包，添加SMAPI，修改AndroidManifest.xml，同时验证版本是否正确
     *
     * @param apkPath APK文件路径
     * @return 是否成功打包
     */
    public boolean patch(String apkPath) {
        if (apkPath == null) {
            return false;
        }
        File file = new File(apkPath);
        if (!file.exists()) {
            return false;
        }
        try {
            byte[] manifest = ZipUtil.unpackEntry(file, "AndroidManifest.xml");
            emitProgress(9);
            List<ApkFilesManifest> apkFilesManifests = CommonLogic.findAllApkFileManifest(context);
            byte[] modifiedManifest = modifyManifest(manifest, apkFilesManifests);
            if (apkFilesManifests.size() == 0) {
                errorMessage.set(context.getString(R.string.error_no_supported_game_version));
                switchAction.set(R.string.menu_download);
                return false;
            }
            if (modifiedManifest == null) {
                errorMessage.set(context.getString(R.string.failed_to_process_manifest));
                return false;
            }
            ApkFilesManifest apkFilesManifest = apkFilesManifests.get(0);
            List<ManifestEntry> manifestEntries = apkFilesManifest.getManifestEntries();
            List<ZipUtils.ZipEntrySource> entries = StreamSupport.stream(manifestEntries).map(entry -> {
                if (entry.isExternal()) {
                    return new ZipUtils.ZipEntrySource(entry.getTargetPath(), FileUtils.getAssetBytes(context, apkFilesManifest.getBasePath() + entry.getAssetPath()), entry.getCompression());
                } else {
                    return new ZipUtils.ZipEntrySource(entry.getTargetPath(), FileUtils.getAssetBytes(context, entry.getAssetPath()), entry.getCompression());
                }
            }).collect(Collectors.toList());
            entries.add(new ZipUtils.ZipEntrySource("AndroidManifest.xml", modifiedManifest, Deflater.DEFLATED));
            emitProgress(10);
            String patchedFilename = apkPath + ".patched";
            File patchedFile = new File(patchedFilename);
            int baseProgress = 10;
            stopwatch.reset();
            stopwatch.start();
            ZipUtils.addOrReplaceEntries(apkPath, entries, patchedFilename,
                    (progress) -> emitProgress((int) (baseProgress + (progress / 100.0) * 35)));
            stopwatch.stop();
            emitProgress(45);
            FileUtils.forceDelete(file);
            FileUtils.moveFile(patchedFile, file);
            emitProgress(46);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Patch error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * 扫描全部兼容包，寻找匹配的版本，修改AndroidManifest.xml文件
     *
     * @param bytes     AndroidManifest.xml的字节数组
     * @param manifests 兼容包列表
     * @return 修改后的AndroidManifest.xml的字节数组
     */
    private byte[] modifyManifest(byte[] bytes, List<ApkFilesManifest> manifests) {
        AtomicReference<String> packageName = new AtomicReference<>();
        AtomicLong versionCode = new AtomicLong();
        Predicate<ManifestTagVisitor.AttrArgs> processLogic = (attr) -> {
            if(attr == null) {
                return true;
            }
            if (attr.type == NodeVisitor.TYPE_STRING) {
                String strObj = (String) attr.obj;
                switch (attr.name) {
                    case "package":
                        if (packageName.get() == null) {
                            packageName.set(strObj);
                            attr.obj = strObj.replace(ManifestPatchConstants.APP_PACKAGE_NAME, Constants.TARGET_PACKAGE_NAME);
                        }
                        break;
                    case "label":
                        if (strObj.contains(ManifestPatchConstants.APP_NAME)) {
                            attr.obj = context.getString(R.string.smapi_game_name);
                        }
                        break;
                    case "authorities":
                        if (strObj.contains(packageName.get())) {
                            attr.obj = strObj.replace(packageName.get(), Constants.TARGET_PACKAGE_NAME);
                        } else if (strObj.contains(ManifestPatchConstants.APP_PACKAGE_NAME)) {
                            attr.obj = strObj.replace(ManifestPatchConstants.APP_PACKAGE_NAME, Constants.TARGET_PACKAGE_NAME);
                        }
                    case "name":
                        if (strObj.contains(ManifestPatchConstants.PATTERN_MAIN_ACTIVITY)) {
                            attr.obj = strObj.replaceFirst("\\w+\\.MainActivity", "md5723872fa9a204f7f942686e9ed9d0b7d.SMainActivity");
                        }
                        break;
                    default:
                        break;
                }
            } else if (attr.type == NodeVisitor.TYPE_FIRST_INT) {
                if (StringUtils.equals(attr.name, ManifestPatchConstants.PATTERN_VERSION_CODE)) {
                    versionCode.set((int) attr.obj);
                }
            }
            return true;
        };
        try {
            byte[] modifyManifest = CommonLogic.modifyManifest(bytes, processLogic);
            Iterables.removeIf(manifests, manifest -> {
                if(manifest == null) {
                    return true;
                }
                if (versionCode.get() < manifest.getMinBuildCode()) {
                    return true;
                }
                if (manifest.getMaxBuildCode() != null) {
                    if (versionCode.get() > manifest.getMaxBuildCode()) {
                        return true;
                    }
                }
                if (manifest.getTargetPackageName() != null && packageName.get() != null && !manifest.getTargetPackageName().contains(packageName.get())) {
                    return true;
                }
                return false;
            });
            return modifyManifest;
        } catch (Exception e) {
            errorMessage.set(e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * 重新签名安装包
     *
     * @param apkPath APK文件路径
     * @return 签名后的安装包路径
     */
    public String sign(String apkPath) {
        try {
            File externalFilesDir = Environment.getExternalStorageDirectory();
            emitProgress(47);
            if (externalFilesDir != null) {
                String signApkPath = externalFilesDir.getAbsolutePath() + "/SMAPI Installer/base_signed.apk";
                KeyStore ks = new KeyStoreFileManager.JksKeyStore();
                try (InputStream fis = context.getAssets().open("debug.keystore.dat")) {
                    ks.load(fis, PASSWORD.toCharArray());
                }
                String alias = ks.aliases().nextElement();
                X509Certificate publicKey = (X509Certificate) ks.getCertificate(alias);
                PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "android".toCharArray());
                ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder("debug", privateKey, Collections.singletonList(publicKey)).build();
                emitProgress(49);
                ApkSigner signer = new ApkSigner.Builder(Collections.singletonList(signerConfig))
                        .setInputApk(new File(apkPath))
                        .setOutputApk(new File(signApkPath))
                        .setV1SigningEnabled(true)
                        .setV2SigningEnabled(true).build();
                long zipOpElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                stopwatch.reset();
                Thread thread = new Thread(() -> {
                    stopwatch.start();
                    while (true) {
                        try {
                            Thread.sleep(20);
                            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                            double progress = elapsed * 0.98 / zipOpElapsed;
                            if (progress < 1.0) {
                                emitProgress((int) (49 + 40 * progress));
                            }
                        } catch (InterruptedException ignored) {
                            return;
                        }
                    }
                });
                thread.start();
                signer.sign();
                if (thread.isAlive() && !thread.isInterrupted()) {
                    thread.interrupt();
                }
                FileUtils.forceDelete(new File(apkPath));
                emitProgress(90);
                return signApkPath;
            }
        } catch (Exception e) {
            Log.e(TAG, "Sign error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 对指定安装包发起安装
     *
     * @param apkPath 安装包路径
     */
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * 获取报错内容
     *
     * @return 报错内容
     */
    public AtomicReference<String> getErrorMessage() {
        return errorMessage;
    }

    public AtomicInteger getSwitchAction() {
        return switchAction;
    }

    private void emitProgress(int progress) {
        for (Consumer<Integer> consumer : progressListener) {
            consumer.accept(progress);
        }
    }

    public void registerProgressListener(Consumer<Integer> listener) {
        progressListener.add(listener);
    }
}
