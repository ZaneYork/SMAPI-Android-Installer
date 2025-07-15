package com.reandroid.apkeditor.merge;

import static com.reandroid.apkeditor.merge.LogUtil.logMessage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.abdurazaaqmohammed.AntiSplit.main.DeviceSpecsUtil;
import com.abdurazaaqmohammed.AntiSplit.main.MismatchedSplitsException;
import com.abdurazaaqmohammed.AntiSplit.main.SignUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.j256.simplezip.ZipFileInput;
import com.j256.simplezip.format.ZipFileHeader;
import com.reandroid.apk.ApkBundle;
import com.reandroid.apk.ApkModule;
import com.reandroid.apkeditor.common.AndroidManifestHelper;
import com.reandroid.app.AndroidManifest;
import com.reandroid.archive.ArchiveFile;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.starry.FileUtils;
import com.zane.smapiinstaller.MainActivity;
import com.zane.smapiinstaller.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Merger {

    public interface LogListener {
        void onLog(CharSequence log);
        void onLog(int resID);
    }

    private static void extractAndLoad(String inputPath, File cacheDir, Context context, List<String> splits, ApkBundle bundle)
            throws IOException, MismatchedSplitsException, InterruptedException {
        logMessage(inputPath);
        boolean checkSplits = splits != null && !splits.isEmpty();
        File inputFile = new File(inputPath);

        try (InputStream is = FileUtils.getInputStream(inputFile);
             ZipFileInput zis = new ZipFileInput(is)) {
            ZipFileHeader header;
            while ((header = zis.readFileHeader()) != null) {
                String name = header.getFileName();
                if (name.endsWith(".apk")) {
                    if ((checkSplits && splits.contains(name))) {
                        logMessage(MainActivity.instance.getString(R.string.skipping) + name + MainActivity.instance.getString(R.string.unselected));
                    } else {
                        File file = new File(cacheDir, name);
                        if (file.getCanonicalPath().startsWith(cacheDir.getCanonicalPath() + File.separator)) {
                            zis.readFileDataToFile(file);
                            logMessage("Extracted " + name);
                        } else {
                            throw new IOException("Zip entry is outside of the target dir: " + name);
                        }
                    }
                } else {
                    logMessage(MainActivity.instance.getString(R.string.skipping) + name + MainActivity.instance.getString(R.string.not_apk));
                }
            }
            bundle.loadApkDirectory(cacheDir, false, context);
        } catch (MismatchedSplitsException m) {
            throw new RuntimeException(m);
        } catch (Exception e) {
            // Fallback to ArchiveFile if ZipFileInput fails
            ArchiveFile zf = new ArchiveFile(inputFile);
            extractZipFile(zf, checkSplits, splits, cacheDir);
            bundle.loadApkDirectory(cacheDir, false, context);
        }
    }

    private static void extractZipFile(ArchiveFile zf, boolean checkSplits, List<String> splits, File cacheDir) throws IOException {
        for(InputSource archiveEntry : zf.createZipEntryMap().toArray()) {
            String name = archiveEntry.getName();
            if (name.endsWith(".apk")) {
                if ((checkSplits && splits.contains(name))) {
                    logMessage(MainActivity.instance.getString(R.string.skipping) + name + MainActivity.instance.getString(R.string.unselected));
                } else {
                    try (OutputStream os = FileUtils.getOutputStream(new File(cacheDir, name));
                         InputStream is = archiveEntry.openStream()) {
                        FileUtils.copyFile(is, os);
                    }
                }
            } else {
                logMessage(MainActivity.instance.getString(R.string.skipping) + name + MainActivity.instance.getString(R.string.not_apk));
            }
        }
    }

    public static void run(ApkBundle bundle, File cacheDir, String outputPath, Context context, boolean signApk)
            throws IOException, InterruptedException {
        logMessage("Found modules: " + bundle.getApkModuleList().size());
        final boolean[] saveToCacheDir = {false};
        final boolean[] sign = {signApk};

        // 检查pairipcore.so的逻辑保持不变
        for(File split : cacheDir.listFiles()) {
            String splitName = split.getName();
            String arch = null;
            String var = "x86";
            if(splitName.contains(var)) arch = var;
            else if(splitName.contains(var = "x86_64") || splitName.contains("x86-64") || splitName.contains("x64")) arch = var;
            else if(splitName.contains("arm64")) arch = "arm64-v8a";
            else if(splitName.contains("v7a") || splitName.contains("arm7")) arch = "armeabi-v7a";

            if(arch != null) try (ApkModule zf = ApkModule.loadApkFile(split, splitName)) {
                if (zf.containsFile("lib" + File.separator + arch + File.separator + "libpairipcore.so")) {
                    final CountDownLatch latch = new CountDownLatch(1);

                    new MaterialAlertDialogBuilder(context)
                            .setTitle(context.getString(R.string.warning))
                            .setMessage(R.string.pairip_warning)
                            .setPositiveButton("OK", (dialog, which) -> {
                                saveToCacheDir[0] = true;
                                sign[0] = false;
                                latch.countDown();
                            })
                            .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {
                                context.startActivity(new Intent(context, MainActivity.class));
                                if (context instanceof Activity) {
                                    ((Activity)context).finishAffinity();
                                }
                                latch.countDown();
                            })
                            .show();

                    latch.await();
                    break;
                }
            }
        }

        try (ApkModule mergedModule = bundle.mergeModules()) {
            // 清理AndroidManifest的逻辑保持不变
            if (mergedModule.hasAndroidManifest()) {
                AndroidManifestBlock manifest = mergedModule.getAndroidManifest();
                logMessage(MainActivity.instance.getString(R.string.sanitizing_manifest));

                // 移除各种split相关属性
                AndroidManifestHelper.removeAttributeFromManifestById(manifest, 0x0101064e);
                AndroidManifestHelper.removeAttributeFromManifestById(manifest, 0x0101064f);
                AndroidManifestHelper.removeAttributeFromManifestByName(manifest, AndroidManifest.NAME_splitTypes);
                AndroidManifestHelper.removeAttributeFromManifestByName(manifest, AndroidManifest.NAME_requiredSplitTypes);
                AndroidManifestHelper.removeAttributeFromManifestAndApplication(manifest, AndroidManifest.ID_extractNativeLibs);
                AndroidManifestHelper.removeAttributeFromManifestAndApplication(manifest, AndroidManifest.ID_isSplitRequired);

                // 清理split metadata
                ResXmlElement application = manifest.getApplicationElement();
                List<ResXmlElement> splitMetaDataElements = AndroidManifestHelper.listSplitRequired(application);
                for (ResXmlElement meta : splitMetaDataElements) {
                    application.remove(meta);
                }
                manifest.refresh();
            }
            ZipEntryMap zipEntryMap = mergedModule.getZipEntryMap();
            List<InputSource> dexSources = new ArrayList<>();
            for (InputSource inputSource : zipEntryMap.listInputSources()) {
                String name = inputSource.getAlias();
                if (name.startsWith("classes") && name.endsWith(".dex")) {
                    dexSources.add(inputSource);
                }
            }
            if (!dexSources.isEmpty()) {
                // 找到有效的 classes.dex
                InputSource validDex = null;
                for (InputSource dex : dexSources) {
                    if (dex.getAlias().equals("classes.dex")) {
                        validDex = dex;
                        break;
                    }
                }

                // 如果没有找到 classes.dex，使用第一个dex文件作为有效文件
                if (validDex == null) {
                    validDex = dexSources.get(0);
                }

                // 移除所有dex文件
                for (InputSource dex : dexSources) {
                    zipEntryMap.remove(dex);
                }

                // 重新添加有效的dex文件
                zipEntryMap.add(validDex);
                logMessage("Kept only one dex file: " + validDex.getAlias());
            }
            logMessage(MainActivity.instance.getString(R.string.saving));

            File outputFile = new File(outputPath);
            if (sign[0]) {

                File temp = new File(cacheDir, "temp.apk");
                mergedModule.writeApk(temp);
                logMessage(MainActivity.instance.getString(R.string.signing));

                try {
                    SignUtil.signDebugKey(context, temp, outputFile);
                } catch (Exception e) {
                    SignUtil.signPseudoApkSigner(temp, context, outputFile, e);
                }
            } else if (saveToCacheDir[0]) {
                File poopyip = new File(cacheDir, "poopyip.apk");
                mergedModule.writeApk(poopyip);
                FileUtils.copyFile(poopyip, outputFile);
            } else {
                mergedModule.writeApk(outputFile);
            }
        }
    }


}
