package com.zane.smapiinstaller.logic;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.lmntrx.android.library.livin.missme.ProgressDialog;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.MainApplication;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.constant.ManifestPatchConstants;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.StringUtils;
import com.zane.smapiinstaller.utils.ZipUtils;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;

/**
 * 通用逻辑
 *
 * @author Zane
 */
public class CommonLogic {
    /**
     * 从View获取所属Activity
     *
     * @param view context容器
     * @return Activity
     */
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

    /**
     * 从一个View获取Application
     *
     * @param view 控件
     * @return Application
     */
    public static MainApplication getApplicationFromView(View view) {
        Activity activity = getActivityFromView(view);
        if (null != activity) {
            return (MainApplication) activity.getApplication();
        }
        return null;
    }

    /**
     * 当data非null时执行操作
     *
     * @param data   数据
     * @param action 操作
     * @param <T>    泛型
     */
    public static <T> void doOnNonNull(T data, Consumer<T> action) {
        if (data != null) {
            action.accept(data);
        }
    }

    /**
     * 在UI线程执行操作
     *
     * @param activity activity
     * @param action   操作
     */
    public static void runOnUiThread(Activity activity, Consumer<Activity> action) {
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> action.accept(activity));
        }
    }

    /**
     * 打开指定URL
     *
     * @param context context
     * @param url     目标URL
     */
    public static void openUrl(Context context, String url) {
        try {
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            intent.setAction(Intent.ACTION_VIEW);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    /**
     * 复制文本到剪贴板
     *
     * @param context 上下文
     * @param copyStr 文本
     * @return 是否复制成功
     */
    public static boolean copyToClipboard(Context context, String copyStr) {
        try {
            CommonLogic.doOnNonNull(context.getSystemService(Context.CLIPBOARD_SERVICE), cm -> {
                ClipData mClipData = ClipData.newPlainText("Label", copyStr);
                ((ClipboardManager) cm).setPrimaryClip(mClipData);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 扫描全部兼容包
     *
     * @param context context
     * @return 兼容包列表
     */
    public static List<ApkFilesManifest> findAllApkFileManifest(Context context) {
        ApkFilesManifest apkFilesManifest = FileUtils.getAssetJson(context, "apk_files_manifest.json", ApkFilesManifest.class);
        ArrayList<ApkFilesManifest> apkFilesManifests = Lists.newArrayList(apkFilesManifest);
        File compatFolder = new File(context.getFilesDir(), "compat");
        if (compatFolder.exists()) {
            for (File directory : compatFolder.listFiles(File::isDirectory)) {
                File manifestFile = new File(directory, "apk_files_manifest.json");
                if (manifestFile.exists()) {
                    ApkFilesManifest manifest = FileUtils.getFileJson(manifestFile, ApkFilesManifest.class);
                    if (manifest != null) {
                        apkFilesManifests.add(manifest);
                    }
                }
            }
        }
        Collections.sort(apkFilesManifests, (a, b) -> {
            if (a.getTargetPackageName() != null && b.getTargetPackageName() == null) {
                return -1;
            } else if (b.getTargetPackageName() != null) {
                return Long.compare(b.getMinBuildCode(), a.getMinBuildCode());
            }
            return 1;
        });
        return apkFilesManifests;
    }

    public static String computePackageName(PackageInfo packageInfo) {
        String packageName = packageInfo.packageName;
        if (StringUtils.endsWith(packageInfo.versionName, ManifestPatchConstants.PATTERN_VERSION_AMAZON)) {
            packageName = ManifestPatchConstants.APP_PACKAGE_NAME + ManifestPatchConstants.PATTERN_VERSION_AMAZON;
        }
        return packageName;
    }

    /**
     * 提取SMAPI环境文件到内部存储对应位置
     *
     * @param context     context
     * @param apkPath     安装包路径
     * @param checkMode   是否为校验模式
     * @param packageName 包名
     * @param versionCode 版本号
     * @return 操作是否成功
     */
    public static boolean unpackSmapiFiles(Context context, String apkPath, boolean checkMode, String packageName, long versionCode) {
        checkMusic(packageName, checkMode);
        List<ApkFilesManifest> apkFilesManifests = CommonLogic.findAllApkFileManifest(context);
        filterManifest(apkFilesManifests, packageName, versionCode);
        List<ManifestEntry> manifestEntries = null;
        ApkFilesManifest apkFilesManifest = null;
        if (apkFilesManifests.size() > 0) {
            apkFilesManifest = apkFilesManifests.get(0);
            String basePath = apkFilesManifest.getBasePath();
            if (StringUtils.isNoneBlank(basePath)) {
                manifestEntries = FileUtils.getAssetJson(context, basePath + "smapi_files_manifest.json", new TypeReference<List<ManifestEntry>>() {
                });
            }
        }
        if (manifestEntries == null) {
            manifestEntries = FileUtils.getAssetJson(context, "smapi_files_manifest.json", new TypeReference<List<ManifestEntry>>() {
            });
        }
        if (manifestEntries == null) {
            return false;
        }
        File basePath = new File(FileUtils.getStadewValleyBasePath() + "/StardewValley/");
        if (!basePath.exists()) {
            if (!basePath.mkdir()) {
                return false;
            }
        } else {
            if (!checkMode) {
                File[] oldAssemblies = new File(basePath, "smapi-internal").listFiles((FileFilter) new WildcardFileFilter("*.dll"));
                if (oldAssemblies != null) {
                    for (File file : oldAssemblies) {
                        FileUtils.deleteQuietly(file);
                    }
                }
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
                    unpackFromInstaller(context, checkMode, apkFilesManifest, basePath, entry, targetFile);
                    break;
                case 1:
                    unpackFromApk(apkPath, checkMode, entry, targetFile);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private static void checkMusic(String packageName, boolean checkMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && StringUtils.equals(packageName, Constants.ORIGIN_PACKAGE_NAME_GOOGLE)) {
            File pathFrom = new File(FileUtils.getStadewValleyBasePath(), "Android/obb/" + packageName);
            File pathTo = new File(FileUtils.getStadewValleyBasePath(), "Android/obb/" + Constants.TARGET_PACKAGE_NAME);
            if (pathFrom.exists() && pathFrom.isDirectory()) {
                if (!pathTo.exists()) {
                    pathTo.mkdirs();
                }
                File[] files = pathFrom.listFiles((dir, name) -> name.contains("com.chucklefish.stardewvalley.obb"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            File targetFile = new File(pathTo, file.getName());
                            if (!targetFile.exists() || FileUtils.sizeOf(targetFile) != FileUtils.sizeOf(file)) {
                                FileUtils.copyFile(file, targetFile);
                            }
                        } catch (IOException e) {
                            Crashes.trackError(e);
                        }
                    }
                }
            }
        }
    }

    private static void unpackFromApk(String apkPath, boolean checkMode, ManifestEntry entry, File targetFile) {
        if (!checkMode || !targetFile.exists()) {
            if (entry.isXALZ()) {
                byte[] bytes = ZipUtil.unpackEntry(new File(apkPath), entry.getAssetPath());
                if (entry.isXALZ()) {
                    bytes = ZipUtils.decompressXALZ(bytes);
                }
                try (FileOutputStream outputStream = FileUtils.openOutputStream(targetFile)) {
                    ByteStreams.copy(Channels.newChannel(new ByteArrayInputStream(bytes)), outputStream.getChannel());
                } catch (IOException ignore) {
                }
            } else {
                ZipUtil.unpackEntry(new File(apkPath), entry.getAssetPath(), targetFile);
            }
        }
    }

    private static void unpackFromInstaller(Context context, boolean checkMode, ApkFilesManifest apkFilesManifest, File basePath, ManifestEntry entry, File targetFile) {
        if (entry.isExternal() && apkFilesManifest != null) {
            byte[] bytes = FileUtils.getAssetBytes(context, apkFilesManifest.getBasePath() + entry.getAssetPath());
            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                ByteStreams.copy(Channels.newChannel(new ByteArrayInputStream(bytes)), outputStream.getChannel());
            } catch (IOException ignored) {
            }
        } else {
            if (entry.getTargetPath().endsWith("/") && entry.getAssetPath().contains("*")) {
                String path = StringUtils.substring(entry.getAssetPath(), 0, StringUtils.lastIndexOf(entry.getAssetPath(), "/"));
                String pattern = StringUtils.substringAfterLast(entry.getAssetPath(), "/");
                try {
                    Stream.of(context.getAssets().list(path))
                            .filter(filename -> StringUtils.wildCardMatch(filename, pattern))
                            .forEach(filename -> unpackFile(context, checkMode, path + "/" + filename, new File(basePath, entry.getTargetPath() + filename)));
                } catch (IOException ignored) {
                }
            } else {
                unpackFile(context, checkMode, entry.getAssetPath(), targetFile);
            }
        }
    }

    public static void filterManifest(List<ApkFilesManifest> manifests, String packageName, long versionCode) {
        Iterables.removeIf(manifests, manifest -> {
            if (manifest == null) {
                return true;
            }
            if (versionCode < manifest.getMinBuildCode()) {
                return true;
            }
            if (manifest.getMaxBuildCode() != null) {
                if (versionCode > manifest.getMaxBuildCode()) {
                    return true;
                }
            }
            return manifest.getTargetPackageName() != null && packageName != null && !manifest.getTargetPackageName().contains(packageName);
        });
    }

    private static void unpackFile(Context context, boolean checkMode, String assertPath, File targetFile) {
        if (!checkMode || !targetFile.exists()) {
            try (InputStream inputStream = context.getAssets().open(assertPath)) {
                if (!targetFile.getParentFile().exists()) {
                    if (!targetFile.getParentFile().mkdirs()) {
                        Log.e("COMMON", "Make dirs error");
                        return;
                    }
                }
                try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                    ByteStreams.copy(Channels.newChannel(inputStream), outputStream.getChannel());
                }
            } catch (IOException e) {
                Log.e("COMMON", "Copy Error", e);
            }
        }
    }

    /**
     * 修改AndroidManifest.xml文件
     *
     * @param bytes            AndroidManifest.xml文件字符数组
     * @param attrProcessLogic 处理逻辑
     * @return 修改后的AndroidManifest.xml文件字符数组
     * @throws IOException 异常
     */
    public static byte[] modifyManifest(byte[] bytes, Function<ManifestTagVisitor.AttrArgs, List<ManifestTagVisitor.AttrArgs>> attrProcessLogic, Function<ManifestTagVisitor.ChildArgs, List<ManifestTagVisitor.ChildArgs>> childProcessLogic) throws IOException {
        AxmlReader reader = new AxmlReader(bytes);
        AxmlWriter writer = new AxmlWriter();
        reader.accept(new AxmlVisitor(writer) {
            @Override
            public NodeVisitor child(String ns, String name) {
                NodeVisitor child = super.child(ns, name);
                return new ManifestTagVisitor(child, attrProcessLogic, childProcessLogic);
            }
        });
        return writer.toByteArray();
    }

    public static void showAnimation(ImageView view, int anim, Consumer<Animation> action) {
        Animation animation = AnimationUtils.loadAnimation(getActivityFromView(view), anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                action.accept(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static long getVersionCode(Activity activity) {
        try {
            PackageManager manager = activity.getPackageManager();
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return info.getLongVersionCode();
            }
            return info.versionCode;
        } catch (Exception ignored) {
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 在谷歌商店打开
     *
     * @param activity activity
     */
    public static void openInPlayStore(Activity activity) {
        CommonLogic.doOnNonNull(activity, (context) -> {
            try {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse("market://details?id=com.zane.smapiinstaller"));
                intent.setPackage("com.android.vending");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                CommonLogic.openUrl(activity, "https://play.google.com/store/apps/details?id=com.zane.smapiinstaller");
            }
        });
    }

    public static void showPrivacyPolicy(View view, BiConsumer<MaterialDialog, DialogAction> callback) {
        Context context = view.getContext();
        String policy = FileUtils.getLocaledAssetText(context, "privacy_policy.txt");
        DialogUtils.showConfirmDialog(view, R.string.privacy_policy, policy, R.string.confirm, R.string.cancel, true, callback);
    }

    public static void showProgressDialog(View root, Context context, Consumer<ProgressDialog> dialogConsumer) {
        AtomicReference<ProgressDialog> dialogHolder = DialogUtils.showProgressDialog(root, R.string.install_progress_title, context.getString(R.string.extracting_package));
        ProgressDialog dialog = null;
        try {
            do {
                Thread.sleep(10);
                dialog = dialogHolder.get();
            } while (dialog == null);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                ConfigManager configManager = new ConfigManager();
                if (configManager.getConfig().isInitial()) {
                    configManager.getConfig().setInitial(false);
                    configManager.getConfig().setDisableMonoMod(true);
                    configManager.flushConfig();
                }
            }
            dialogConsumer.accept(dialog);
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            Crashes.trackError(e);
            DialogUtils.showAlertDialog(root, R.string.error, e.getLocalizedMessage());
        } finally {
            DialogUtils.dismissDialog(root, dialog);
        }
    }
}
