package com.abdurazaaqmohammed.AntiSplit.main;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.j256.simplezip.ZipFileInput;
import com.j256.simplezip.format.ZipFileHeader;
import com.reandroid.archive.ArchiveFile;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.starry.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DeviceSpecsUtil {

    private final Context context;
    public final String lang;
    private final String densityType;
    public static ArchiveFile zipFile = null;

    public DeviceSpecsUtil(Context context) {
        this.context = context;
        this.lang = Locale.getDefault().getLanguage();
        this.densityType = getDeviceDpi();
    }

    public List<String> getListOfSplits(Uri splitAPKUri) throws IOException {
        List<String> splits = new ArrayList<>();

        try (InputStream is = FileUtils.getInputStream(splitAPKUri, context);
                ZipFileInput zis = new ZipFileInput(is)) {
            ZipFileHeader header;
            while ((header = zis.readFileHeader()) != null) {
                final String name = header.getFileName();
                if (name.endsWith(".apk")) splits.add(name);
            }
        }

        if(splits.size() < 2) {
            File file = new File(FileUtils.getPath(splitAPKUri, context));
            boolean couldNotRead = !file.canRead();
            try(InputStream is = context.getContentResolver().openInputStream(splitAPKUri)) {
                if(couldNotRead) FileUtils.copyFile(is, file = new File(context.getCacheDir(), file.getName()));
            }
            ZipEntryMap entries = (zipFile = new ArchiveFile(file)).createZipEntryMap();
            // Do not close this ZipFile it could be used later in merger
            for(InputSource inputSource : entries.toArray()) {
                String name = inputSource.getName();
                if (name.endsWith(".apk"))  splits.add(name);
            }
        }

        return splits;
    }

    public static boolean isArch(String thisSplit) {
        return thisSplit.contains("armeabi") || thisSplit.contains("arm64") || thisSplit.contains("x86") || thisSplit.contains("mips");
    }

    public static boolean isBaseApk(String name) {
        return name.equals("base.apk")
                || !name.startsWith("config") && !name.startsWith("split"); // this is base.apk hopefully
    }

    public boolean shouldIncludeSplit(String name) {
        return isBaseApk(name) || shouldIncludeLang(name) || shouldIncludeArch(name) || shouldIncludeDpi(name);
    }

    public boolean shouldIncludeLang(String name) {
        return name.contains(lang);
    }

    public boolean shouldIncludeArch(String name) {
        return name.contains(Build.CPU_ABI) || name.replace('-', '_').contains(Build.CPU_ABI.replace('-', '_'));
    }

    public boolean shouldIncludeDpi(String name) {
        return (name.endsWith(densityType) && !name.replace(densityType, "").endsWith("x")); // ensure that it does not select xxhdpi for xhdpi etc
    }

    public String getDeviceDpi() {
        String densityType;
        if(TextUtils.isEmpty(densityType = context.getSharedPreferences("set", Context.MODE_PRIVATE).getString("deviceDpi", ""))) {
            switch (context.getResources().getDisplayMetrics().densityDpi) {
                case DisplayMetrics.DENSITY_LOW:
                    densityType = "ldpi";
                    break;
                case DisplayMetrics.DENSITY_MEDIUM:
                case DisplayMetrics.DENSITY_140:
                    densityType = "mdpi";
                    break;
                case DisplayMetrics.DENSITY_XHIGH:
                case DisplayMetrics.DENSITY_280:
                case DisplayMetrics.DENSITY_260:
                case DisplayMetrics.DENSITY_300:
                    densityType = "xhdpi";
                    break;
                case DisplayMetrics.DENSITY_340:
                case DisplayMetrics.DENSITY_360:
               // case DisplayMetrics.DENSITY_390:
                case DisplayMetrics.DENSITY_400:
                case DisplayMetrics.DENSITY_420:
                case DisplayMetrics.DENSITY_440:
                case DisplayMetrics.DENSITY_450:
                case DisplayMetrics.DENSITY_XXHIGH:
                    densityType = "xxhdpi";
                    break;
              //  case DisplayMetrics.DENSITY_520:
                case DisplayMetrics.DENSITY_560:
                case DisplayMetrics.DENSITY_600:
                case DisplayMetrics.DENSITY_XXXHIGH:
                    densityType = "xxxhdpi";
                    break;
                case DisplayMetrics.DENSITY_TV:
                    densityType = "tvdpi";
                    break;
                case DisplayMetrics.DENSITY_HIGH:
                case DisplayMetrics.DENSITY_220:
                case DisplayMetrics.DENSITY_200:
                case DisplayMetrics.DENSITY_180:
                default:
                    densityType = "hdpi";
                    break;
            }
            densityType += ".apk";
            context.getSharedPreferences("set", Context.MODE_PRIVATE).edit().putString("deviceDpi", densityType).apply();
        }
        return densityType;
    }
}