package com.zane.smapiinstaller.ui.config;

import android.os.FileObserver;
import android.view.View;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.ModAssetsManager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

class ConfigViewModel extends ViewModel {

    @NonNull
    private List<ModManifestEntry> modList;

    private RecyclerView view;

    ConfigViewModel(View root) {
        this.modList = ModAssetsManager.findAllInstalledMods();
        Collections.sort(this.modList, (a, b)-> a.getName().compareTo(b.getName()));
    }

    @NonNull
    public List<ModManifestEntry> getModList() {
        return modList;
    }

    public List<Integer> removeAll(Predicate<ModManifestEntry> predicate) {
        List<Integer> deletedId = new ArrayList<>();
        for (int i = modList.size() - 1; i >=0 ; i--) {
            if(predicate.apply(modList.get(i))) {
                modList.remove(i);
                deletedId.add(i);
            }
        }
        return deletedId;
    }

}