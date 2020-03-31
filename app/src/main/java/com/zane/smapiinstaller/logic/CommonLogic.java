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
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.zane.smapiinstaller.MainApplication;
import com.zane.smapiinstaller.entity.ApkFilesManifest;
import com.zane.smapiinstaller.entity.ManifestEntry;
import com.zane.smapiinstaller.utils.FileUtils;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java9.util.function.Consumer;
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

    /**
     * 提取SMAPI环境文件到内部存储对应位置
     *
     * @param context   context
     * @param apkPath   安装包路径
     * @param checkMode 是否为校验模式
     * @return 操作是否成功
     */
    public static boolean unpackSmapiFiles(Context context, String apkPath, boolean checkMode) {
        List<ManifestEntry> manifestEntries = FileUtils.getAssetJson(context, "smapi_files_manifest.json", new TypeReference<List<ManifestEntry>>() {
        });
        if (manifestEntries == null) {
            return false;
        }
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
                    if (!checkMode || !targetFile.exists()) {
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
                    if (!checkMode || !targetFile.exists()) {
                        ZipUtil.unpackEntry(new File(apkPath), entry.getAssetPath(), targetFile);
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * 修改AndroidManifest.xml文件
     *
     * @param bytes        AndroidManifest.xml文件字符数组
     * @param processLogic 处理逻辑
     * @return 修改后的AndroidManifest.xml文件字符数组
     * @throws IOException 异常
     */
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
            String version = info.versionName;
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
                intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
                intent.setPackage("com.android.vending");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                CommonLogic.openUrl(activity, "https://play.google.com/store/apps/details?id=" + context.getPackageName());
            }
        });
    }
}
