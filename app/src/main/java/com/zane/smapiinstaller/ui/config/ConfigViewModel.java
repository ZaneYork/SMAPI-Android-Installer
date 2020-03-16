package com.zane.smapiinstaller.ui.config;

import android.view.View;

import com.google.common.base.Predicate;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.ModAssetsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

class ConfigViewModel extends ViewModel {

    @NonNull
    private List<ModManifestEntry> modList;

    private RecyclerView view;

    ConfigViewModel(View root) {
        this.modList = ModAssetsManager.findAllInstalledMods();
        Collections.sort(this.modList, (a, b)-> {
            if(a.getContentPackFor() != null &&  b.getContentPackFor() == null) {
                return 1;
            }
            else if(b.getContentPackFor() != null) {
                return -1;
            }
            return a.getName().compareTo(b.getName());
        });
    }

    @NonNull
    public List<ModManifestEntry> getModList() {
        return modList;
    }

    public Integer findFirst(Predicate<ModManifestEntry> predicate) {
        for (int i = 0; i < modList.size(); i++) {
            if(predicate.apply(modList.get(i))) {
                return i;
            }
        }
        return null;
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