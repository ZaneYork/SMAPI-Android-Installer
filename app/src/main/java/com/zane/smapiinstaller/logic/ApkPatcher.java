package com.zane.smapiinstaller.logic;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.apksig.ApkSigner;
import com.android.apksig.ApkVerifier;
import com.android.apksig.DefaultApkSignerEngine;
import com.android.apksig.util.DataSources;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.io.ByteStreams;
import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.MainActivity;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.constant.ManifestPatchConstants;
import com.zane.smapiinstaller.dto.Tuple2;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.PackageInstallUtil;
import com.zane.smapiinstaller.utils.StringUtils;
import com.zane.smapiinstaller.utils.ZipUtils;

import net.fornwall.apksigner.KeyStoreFileManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import pxb.android.axml.NodeVisitor;

/**
 * @author Zane
 */
public class ApkPatcher {

    private static final String PASSWORD = "android";

    private final Context context;

    private static final String TAG = "PATCHER";

    private final AtomicReference<String> errorMessage = new AtomicReference<>();

    private final AtomicReference<String> gamePackageName = new AtomicReference<>();

    private final AtomicLong gameVersionCode = new AtomicLong();

    private final AtomicInteger switchAction = new AtomicInteger();

    private Tuple2<byte[], Set<String>> originSignInfo = null;

    private final List<Consumer<Integer>> progressListener = new ArrayList<>();

    private int lastProgress = -1;

    private final Stopwatch stopwatch = Stopwatch.createUnstarted(new Ticker() {
        @Override
        public long read() {
            return android.os.SystemClock.elapsedRealtimeNanos();
        }
    });

    public ApkPatcher(Context context) {
        this.lastProgress = -1;
        this.context = context;
    }

    /**
     * 依次扫描package_names.json文件对应的包名，抽取找到的第一个游戏APK到SMAPI Installer路径
     *
     * @param advancedStage 0: 初始化，1: 高级安装，-1: 普通安装
     * @return 抽取后的APK文件路径，如果抽取失败返回null
     */
    public Tuple2<String, String[]> extract(int advancedStage) {
        emitProgress(0);
        PackageManager packageManager = context.getPackageManager();
        List<String> packageNames = FileUtils.getAssetJson(context, "package_names.json", new TypeReference<List<String>>() {
        });
        if (packageNames == null) {
            errorMessage.set(context.getString(R.string.error_game_not_found));
            return null;
        }
        for (String packageName : packageNames) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                String sourceDir = packageInfo.applicationInfo.publicSourceDir;
                gamePackageName.set(CommonLogic.computePackageName(packageInfo));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    gameVersionCode.set(packageInfo.getLongVersionCode());
                } else {
                    gameVersionCode.set(packageInfo.versionCode);
                }
                File apkFile = new File(sourceDir);

                String stadewValleyBasePath = FileUtils.getStadewValleyBasePath();
                if (advancedStage == 0) {
                    AtomicInteger count = new AtomicInteger();
                    ZipUtil.unpack(apkFile, new File(stadewValleyBasePath + "/StardewValley/"), name -> {
                        if (name.startsWith("assets/")) {
                            int progress = count.incrementAndGet();
                            if (progress % 30 == 0) {
                                emitProgress(progress / 30);
                            }
                            return name.replaceFirst("assets/", "");
                        }
                        return null;
                    });
                    return new Tuple2<>(apkFile.getAbsolutePath(), null);
                } else if (advancedStage == 1) {
                    File contentFolder = new File(stadewValleyBasePath + "/StardewValley/Content");
                    if (contentFolder.exists()) {
                        if (!contentFolder.isDirectory()) {
                            errorMessage.set(context.getString(R.string.error_directory_exists_with_same_filename, contentFolder.getAbsolutePath()));
                            return null;
                        }
                    } else {
                        extract(0);
                    }
                    return new Tuple2<>(apkFile.getAbsolutePath(), null);
                }
                emitProgress(5);
                return new Tuple2<>(apkFile.getAbsolutePath(), packageInfo.applicationInfo.splitSourceDirs);
            } catch (PackageManager.NameNotFoundException ignored) {
                return null;
            }
        }
        errorMessage.set(context.getString(R.string.error_game_not_found));
        return null;
    }

    /**
     * 将指定APK文件重新打包，添加SMAPI，修改AndroidManifest.xml，同时验证版本是否正确
     *
     * @param apkPath    APK文件路径
     * @param second
     * @param targetFile 目标文件
     * @param isAdvanced 是否高级模式
     * @return 是否成功打包
     */
    public boolean patch(String apkPath, String[] resourcePacks, File targetFile, boolean isAdvanced, boolean isResourcePack) {
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
            errorMessage.set(null);
            List<ZipUtils.ZipEntrySource> entries = isResourcePack ? new ArrayList<>() : manifestEntries.stream()
                    .map(entry -> processFileEntry(file, apkFilesManifest, entry, isAdvanced))
                    .filter(Objects::nonNull).flatMap(Stream::of).distinct().collect(Collectors.toList());
            entries.add(new ZipUtils.ZipEntrySource("AndroidManifest.xml", modifiedManifest, Deflater.DEFLATED));
            emitProgress(10);
            int baseProgress = 10;
            stopwatch.reset();
            stopwatch.start();
            originSignInfo = ZipUtils.addOrReplaceEntries(apkPath, resourcePacks, entries, targetFile.getAbsolutePath(),
                    (entryName) -> entryName.startsWith("assemblies/assemblies.") || (isAdvanced && entryName.startsWith("assets/Content")),
                    (progress) -> emitProgress((int) (baseProgress + (progress / 100.0) * 35)));
            stopwatch.stop();
            emitProgress(46);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Patch error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
        return false;
    }

    @Nullable
    private ZipUtils.ZipEntrySource[] processFileEntry(File apkFile, ApkFilesManifest apkFilesManifest, ManifestEntry entry, boolean isAdvanced) {
        if (entry.isAdvanced() && !isAdvanced) {
            return null;
        }
        if (entry.getTargetPath().endsWith(Constants.FILE_SEPARATOR)) {
            if (entry.isXABA()) {
                byte[] manifestBytes = ZipUtil.unpackEntry(apkFile, entry.getAssetPath() + ".manifest");
                byte[] xabaBytes = ZipUtil.unpackEntry(apkFile, entry.getAssetPath() + ".blob");
                Map<String, byte[]> unpackedAssemblies = ZipUtils.unpackXABA(manifestBytes, xabaBytes);
                ArrayList<ZipUtils.ZipEntrySource> list = new ArrayList<>();
                unpackedAssemblies.forEach((filename, bytes) -> {
                    list.add(new ZipUtils.ZipEntrySource(entry.getTargetPath() + filename, bytes, entry.getCompression()));
                });
                return list.toArray(new ZipUtils.ZipEntrySource[0]);
            } else if (entry.getAssetPath().contains("*")) {
                String path = StringUtils.substringBeforeLast(entry.getAssetPath(), Constants.FILE_SEPARATOR);
                String pattern = StringUtils.substringAfterLast(entry.getAssetPath(), Constants.FILE_SEPARATOR);
                try {
                    if (entry.getOrigin() == 1) {
                        ArrayList<ZipUtils.ZipEntrySource> list = new ArrayList<>();
                        ZipUtil.iterate(apkFile, (in, zipEntry) -> {
                            String entryPath = StringUtils.substringBeforeLast(zipEntry.getName(), Constants.FILE_SEPARATOR);
                            String filename = StringUtils.substringAfterLast(zipEntry.getName(), Constants.FILE_SEPARATOR);
                            if (entryPath.equals(path) && StringUtils.wildCardMatch(filename, pattern)) {
                                byte[] bytes = ByteStreams.toByteArray(in);
                                ZipUtils.ZipEntrySource source;
                                if (entry.isXALZ()) {
                                    source = new ZipUtils.ZipEntrySource(entry.getTargetPath() + filename, entry.getCompression(), () -> new ByteArrayInputStream(ZipUtils.decompressXALZ(bytes)));
                                } else {
                                    source = new ZipUtils.ZipEntrySource(entry.getTargetPath() + filename, bytes, entry.getCompression());
                                }
                                list.add(source);
                            }
                        });
                        return list.toArray(new ZipUtils.ZipEntrySource[0]);
                    } else {
                        return Stream.of(context.getAssets().list(path))
                                .filter(filename -> StringUtils.wildCardMatch(filename, pattern))
                                .map(filename -> new ZipUtils.ZipEntrySource(entry.getTargetPath() + filename, entry.getCompression(), () -> {
                                    try {
                                        return FileUtils.getLocalAsset(context, path + Constants.FILE_SEPARATOR + filename);
                                    } catch (IOException ignored) {
                                    }
                                    return null;
                                }))
                                .toArray(ZipUtils.ZipEntrySource[]::new);
                    }
                } catch (IOException ignored) {
                }
            }
            return null;
        }
        ZipUtils.ZipEntrySource source;
        if (entry.getOrigin() == 1) {
            byte[] unpackEntryBytes = ZipUtil.unpackEntry(apkFile, entry.getAssetPath());
            if (entry.isXALZ()) {
                source = new ZipUtils.ZipEntrySource(entry.getTargetPath(), entry.getCompression(), () -> new ByteArrayInputStream(ZipUtils.decompressXALZ(unpackEntryBytes)));
            } else {
                source = new ZipUtils.ZipEntrySource(entry.getTargetPath(), unpackEntryBytes, entry.getCompression());
            }
        } else {
            source = new ZipUtils.ZipEntrySource(entry.getTargetPath(), entry.getCompression(), () -> {
                InputStream inputStream = null;
                try {
                    if (entry.isExternal()) {
                        inputStream = FileUtils.getLocalAsset(context, apkFilesManifest.getBasePath() + entry.getAssetPath());
                    } else {
                        inputStream = FileUtils.getLocalAsset(context, entry.getAssetPath());
                    }
                } catch (IOException ignored) {
                }
                if (StringUtils.isNoneBlank(entry.getPatchCrc())) {
                    throw new NotImplementedException("bs patch mode is not supported anymore.");
                }
                return inputStream;
            });
        }
        return new ZipUtils.ZipEntrySource[]{source};
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
        AtomicReference<String> versionName = new AtomicReference<>();
        AtomicLong versionCode = new AtomicLong();
        Function<ManifestTagVisitor.AttrArgs, List<ManifestTagVisitor.AttrArgs>> attrProcessLogic = (attr) -> {
            if (attr == null) {
                return null;
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
                    case ManifestPatchConstants.PATTERN_VERSION_NAME:
                        if (versionName.get() == null) {
                            versionName.set((String) attr.obj);
                        }
                        break;
                    case "label":
                        if (strObj.contains(ManifestPatchConstants.APP_NAME)) {
                            if (StringUtils.isBlank(Constants.PATCHED_APP_NAME)) {
                                attr.obj = context.getString(R.string.smapi_game_name);
                            } else {
                                attr.obj = Constants.PATCHED_APP_NAME;
                            }
//                            return Collections.singletonList(new ManifestTagVisitor.AttrArgs(attr.ns, "requestLegacyExternalStorage", -1, NodeVisitor.TYPE_INT_BOOLEAN, true));
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
                            if (versionCode.get() > Constants.MONO_10_VERSION_CODE) {
                                attr.obj = strObj.replaceFirst("\\w+\\.MainActivity", "crc648e5438a58262f792.SMainActivity");
                            } else {
                                attr.obj = strObj.replaceFirst("\\w+\\.MainActivity", "md5723872fa9a204f7f942686e9ed9d0b7d.SMainActivity");
                            }
                        }
                        break;
                    default:
                        break;
                }
            } else if (attr.type == NodeVisitor.TYPE_FIRST_INT) {
                if (StringUtils.equals(attr.name, ManifestPatchConstants.PATTERN_VERSION_CODE)) {
                    versionCode.set((int) attr.obj);
                }
            } else if (attr.type == NodeVisitor.TYPE_INT_BOOLEAN) {
                if (StringUtils.equals(attr.name, ManifestPatchConstants.PATTERN_EXTRACT_NATIVE_LIBS)) {
                    attr.obj = true;
                }
            }
            return null;
        };
        AtomicReference<Boolean> permissionAppended = new AtomicReference<>(true);
        Function<ManifestTagVisitor.ChildArgs, List<ManifestTagVisitor.ChildArgs>> childProcessLogic = (child -> {
            if (!permissionAppended.get() && StringUtils.equals(child.name, "uses-permission")) {
                permissionAppended.set(true);
                return Collections.singletonList(
                        new ManifestTagVisitor.ChildArgs(child.ns, child.name, Collections.singletonList(
                                new ManifestTagVisitor.AttrArgs(
                                        "http://schemas.android.com/apk/res/android", "name", -1,
                                        NodeVisitor.TYPE_STRING, "android.permission.MANAGE_EXTERNAL_STORAGE")))
                );
            }
            return null;
        });
        try {
            byte[] modifyManifest = CommonLogic.modifyManifest(bytes, attrProcessLogic, childProcessLogic);
            if (StringUtils.endsWith(versionName.get(), ManifestPatchConstants.PATTERN_VERSION_AMAZON)) {
                packageName.set(ManifestPatchConstants.APP_PACKAGE_NAME + ManifestPatchConstants.PATTERN_VERSION_AMAZON);
            }
            CommonLogic.filterManifest(manifests, packageName.get(), versionCode.get());
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
            String stadewValleyBasePath = FileUtils.getStadewValleyBasePath();
            emitProgress(47);
            String signApkPath = stadewValleyBasePath + "/SMAPI Installer/" + FilenameUtils.getBaseName(apkPath) + "_signed.apk";
            KeyStore ks = new KeyStoreFileManager.JksKeyStore();
            try (InputStream fis = context.getAssets().open("debug.keystore.dat")) {
                ks.load(fis, PASSWORD.toCharArray());
            }
            String alias = ks.aliases().nextElement();
            X509Certificate publicKey = (X509Certificate) ks.getCertificate(alias);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "android".toCharArray());
            emitProgress(49);
            File outputFile = new File(signApkPath);
            List<DefaultApkSignerEngine.SignerConfig> engineSignerConfigs = Collections.singletonList(
                    new DefaultApkSignerEngine.SignerConfig.Builder(
                            "debug",
                            privateKey,
                            Collections.singletonList(publicKey))
                            .build());
            DefaultApkSignerEngine signerEngine = new DefaultApkSignerEngine.Builder(engineSignerConfigs, 19)
                    .setV1SigningEnabled(true)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(false)
                    .build();
            if (originSignInfo != null && originSignInfo.getFirst() != null) {
                signerEngine.initWith(originSignInfo.getFirst(), originSignInfo.getSecond());
            }
            long zipOpElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.reset();
            Thread thread = new Thread(() -> {
                stopwatch.start();
                while (true) {
                    try {
                        Thread.sleep(200);
                        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                        double progress = elapsed * 0.98 / zipOpElapsed;
                        if (progress < 1.0) {
                            emitProgress((int) (49 + 45 * progress));
                        }
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            });
            thread.start();
            try (RandomAccessFile inputApkFile = new RandomAccessFile(apkPath, "r")) {
                ApkSigner signer = new ApkSigner.Builder(signerEngine)
                        .setInputApk(DataSources.asDataSource(inputApkFile, 0, inputApkFile.length()))
                        .setOutputApk(outputFile)
                        .build();
                signer.sign();
            }
            FileUtils.forceDelete(new File(apkPath));
            ApkVerifier.Result result = new ApkVerifier.Builder(outputFile).build().verify();
            if (thread.isAlive() && !thread.isInterrupted()) {
                thread.interrupt();
            }
            if (result.containsErrors() && result.getErrors().size() > 0) {
                errorMessage.set(result.getErrors().stream().map(ApkVerifier.IssueWithParams::toString).collect(Collectors.joining(",")));
                return null;
            }
            emitProgress(95);
            return signApkPath;
        } catch (Exception e) {
            Log.e(TAG, "Sign error", e);
            errorMessage.set(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 对指定安装包发起安装
     *
     * @param apkPath             安装包路径
     */
    public void install(String apkPath) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            boolean haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {
                DialogUtils.showConfirmDialog(MainActivity.instance, R.string.confirm, R.string.request_unknown_source_permission, ((dialog, dialogAction) -> {
                    if (dialogAction == DialogAction.POSITIVE) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        ActivityResultHandler.registerListener(ActivityResultHandler.REQUEST_CODE_APP_INSTALL, (resultCode, data) -> this.install(apkPath));
                        MainActivity.instance.startActivityForResult(intent, ActivityResultHandler.REQUEST_CODE_APP_INSTALL);
                    }
                }));
                return;
            }
        }

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

    public String getGamePackageName() {
        return gamePackageName.get();
    }

    public long getGameVersionCode() {
        return gameVersionCode.get();
    }

    public AtomicInteger getSwitchAction() {
        return switchAction;
    }

    private void emitProgress(int progress) {
        if (lastProgress < progress) {
            lastProgress = progress;
            for (Consumer<Integer> consumer : progressListener) {
                consumer.accept(progress);
            }
        }
    }

    public void registerProgressListener(Consumer<Integer> listener) {
        progressListener.add(listener);
    }
}
