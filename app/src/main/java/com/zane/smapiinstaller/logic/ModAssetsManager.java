package com.zane.smapiinstaller.logic;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Queues;
import com.google.gson.reflect.TypeToken;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.ModManifestEntry;

import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.core.util.Consumer;

public class ModAssetsManager {

    private final View root;

    private static final String TAG = "MANAGER";

    public ModAssetsManager(View root) {
        this.root = root;
    }

    public static ModManifestEntry findFirstModIf(Predicate<ModManifestEntry> filter) {
        ConcurrentLinkedQueue<File> files = Queues.newConcurrentLinkedQueue();
        files.add(new File(Environment.getExternalStorageDirectory(), Constants.MOD_PATH));
        do {
            File currentFile = files.poll();
            if(currentFile != null && currentFile.exists()) {
                boolean foundManifest = false;
                for(File file : currentFile.listFiles(File::isFile)) {
                    if(StringUtils.equalsIgnoreCase(file.getName(), "manifest.json")) {
                        ModManifestEntry manifest = CommonLogic.getFileJson(file, new TypeToken<ModManifestEntry>(){}.getType());
                        foundManifest = true;
                        if(manifest != null) {
                            manifest.setAssetPath(file.getParentFile().getAbsolutePath());
                            if(filter.apply(manifest)) {
                                return manifest;
                            }
                        }
                        break;
                    }
                }
                if(!foundManifest) {
                    files.addAll(Lists.newArrayList(currentFile.listFiles(File::isDirectory)));
                }
            }
        } while (!files.isEmpty());
        return null;
    }
    public static List<ModManifestEntry> findAllInstalledMods() {
        ConcurrentLinkedQueue<File> files = Queues.newConcurrentLinkedQueue();
        files.add(new File(Environment.getExternalStorageDirectory(), Constants.MOD_PATH));
        List<ModManifestEntry> mods = new ArrayList<>(30);
        do {
            File currentFile = files.poll();
            if(currentFile != null && currentFile.exists()) {
                boolean foundManifest = false;
                for(File file : currentFile.listFiles(File::isFile)) {
                    if(StringUtils.equalsIgnoreCase(file.getName(), "manifest.json")) {
                        ModManifestEntry manifest = CommonLogic.getFileJson(file, new TypeToken<ModManifestEntry>(){}.getType());
                        foundManifest = true;
                        if(manifest != null) {
                            manifest.setAssetPath(file.getParentFile().getAbsolutePath());
                            mods.add(manifest);
                        }
                        break;
                    }
                }
                if(!foundManifest) {
                    files.addAll(Lists.newArrayList(currentFile.listFiles(File::isDirectory)));
                }
            }
        } while (!files.isEmpty());
        return mods;
    }

    public boolean installDefaultMods() {
        Activity context = CommonLogic.getActivityFromView(root);
        List<ModManifestEntry> modManifestEntries = CommonLogic.getAssetJson(context, "mods_manifest.json", new TypeToken<List<ModManifestEntry>>() {
        }.getType());
        if(modManifestEntries == null)
            return false;
        File modFolder = new File(Environment.getExternalStorageDirectory(), Constants.MOD_PATH);
        ImmutableListMultimap<String, ModManifestEntry> installedModMap = Multimaps.index(findAllInstalledMods(), ModManifestEntry::getUniqueID);
        for (ModManifestEntry mod : modManifestEntries) {
            if(installedModMap.containsKey(mod.getUniqueID()) || installedModMap.containsKey(mod.getUniqueID().replace("ZaneYork.CustomLocalization", "SMAPI.CustomLocalization"))) {
                ImmutableList<ModManifestEntry> installedMods = installedModMap.get(mod.getUniqueID());
                if(installedMods.size() > 1) {
                    CommonLogic.showAlertDialog(root, R.string.error,
                            String.format(context.getString(R.string.duplicate_mod_found),
                                    Joiner.on(",").join(Lists.transform(installedMods, item -> CommonLogic.toPrettyPath(item.getAssetPath())))));
                    return false;
                }
                else if(installedMods.size() == 0) {
                    installedMods = installedModMap.get(mod.getUniqueID().replace("ZaneYork.CustomLocalization", "SMAPI.CustomLocalization"));
                }
                if(installedMods.size() > 0) {
                    try {
                        ZipUtil.unpack(context.getAssets().open(mod.getAssetPath()), new File(installedMods.get(0).getAssetPath()), (name) -> StringUtils.removeStart(name, mod.getName() + "/"));
                    } catch (IOException e) {
                        Log.e(TAG, "Install Mod Error", e);
                    }
                }
            }
            else {
                try {
                    ZipUtil.unpack(context.getAssets().open(mod.getAssetPath()), modFolder);
                } catch (IOException e) {
                    Log.e(TAG, "Install Mod Error", e);
                }
            }
        }
        return true;
    }

    public void checkModEnvironment(Consumer<Boolean> returnCallback) {
        ImmutableListMultimap<String, ModManifestEntry> installedModMap = Multimaps.index(findAllInstalledMods(), ModManifestEntry::getUniqueID);
        ArrayList<String> list = Lists.newArrayList();
        for (String key : installedModMap.keySet()) {
            ImmutableList<ModManifestEntry> installedMods = installedModMap.get(key);
            if(installedMods.size() > 1) {
                list.add(Joiner.on(",").join(Lists.transform(installedMods, item -> CommonLogic.toPrettyPath(item.getAssetPath()))));
            }
        }
        if(list.size() > 0) {
            CommonLogic.showConfirmDialog(root, R.string.error,
                    String.format(root.getContext().getString(R.string.duplicate_mod_found),
                            Joiner.on(";").join(list)), ((dialog, which) -> {
                                if(which == DialogAction.POSITIVE) {
                                    returnCallback.accept(true);
                                }
                                else {
                                    returnCallback.accept(false);
                                }
                    }));
            return;
        }
        returnCallback.accept(true);
    }
}
