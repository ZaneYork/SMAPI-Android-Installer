package com.zane.smapiinstaller.ui.config;

import android.view.View;

import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.ModAssetsManager;

import java.util.Collections;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class ConfigViewModel extends ViewModel {

    private MutableLiveData<List<ModManifestEntry>> modList;

    public ConfigViewModel(View root) {
        ModAssetsManager manager = new ModAssetsManager(root);
        this.modList = new MutableLiveData<>();
        List<ModManifestEntry> entryList = manager.findAllInstalledMods();
        Collections.sort(entryList, (a, b)-> a.getName().compareTo(b.getName()));
        this.modList.setValue(entryList);
    }

    public MutableLiveData<List<ModManifestEntry>> getModList() {
        return modList;
    }
}