package com.zane.smapiinstaller.ui.update;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.databinding.UpdatableModListItemBinding;
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.utils.VersionUtil;

import java.util.List;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ModUpdateCheckResponseDto.UpdateInfo}
 *
 * @author Zane
 */
public class ModUpdateAdapter extends RecyclerView.Adapter<ModUpdateAdapter.ViewHolder> {

    private final ImmutableListMultimap<String, ModManifestEntry> installedModMap;

    private List<ModUpdateCheckResponseDto> updateInfoList;

    public ModUpdateAdapter(List<ModUpdateCheckResponseDto> items) {
        updateInfoList = items;
        installedModMap = Multimaps.index(ModAssetsManager.findAllInstalledMods(), ModManifestEntry::getUniqueID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.updatable_mod_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setUpdateInfo(updateInfoList.get(position));
    }

    @Override
    public int getItemCount() {
        return updateInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ModUpdateCheckResponseDto updateInfo;
        private UpdatableModListItemBinding binding;

        public void setUpdateInfo(ModUpdateCheckResponseDto updateInfo) {
            this.updateInfo = updateInfo;
            String id = updateInfo.getId();
            Optional<ModManifestEntry> mod = installedModMap.get(id).stream().min((a, b) -> VersionUtil.compareVersion(a.getVersion(), b.getVersion()));
            if (mod.isPresent()) {
                ModManifestEntry modManifestEntry = mod.get();
                binding.textViewModName.setText(modManifestEntry.getName());
                CommonLogic.doOnNonNull(CommonLogic.getActivityFromView(binding.textViewModName),
                        activity -> binding.textViewModVersion.setText(
                                activity.getString(R.string.mod_version_update_text, modManifestEntry.getVersion(), updateInfo.getSuggestedUpdate().getVersion())
                        ));
            }
        }

        public ViewHolder(View view) {
            super(view);
            binding = UpdatableModListItemBinding.bind(view);
            binding.buttonUpdateMod.setOnClickListener(v -> onUpdateClick());
        }

        void onUpdateClick() {
            CommonLogic.doOnNonNull(CommonLogic.getActivityFromView(binding.textViewModName), context -> CommonLogic.openUrl(context, updateInfo.getSuggestedUpdate().getUrl()));
        }
    }
}
