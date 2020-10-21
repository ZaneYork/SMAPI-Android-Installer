package com.zane.smapiinstaller.ui.config;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.MobileNavigationDirections;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.databinding.ModListItemBinding;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Zane
 */
public class ModManifestAdapter extends RecyclerView.Adapter<ModManifestAdapter.ViewHolder> {
    private ConfigViewModel model;
    private List<ModManifestEntry> modList;

    public ModManifestAdapter(ConfigViewModel model, List<ModManifestEntry> modList) {
        this.model = model;
        this.modList = modList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModManifestEntry mod = modList.get(position);
        holder.binding.textViewModName.setText(mod.getName());
        holder.binding.textViewModDescription.setText(StringUtils.firstNonBlank(mod.getTranslatedDescription(), mod.getDescription()));
        holder.setModInfo(mod);
    }

    @Override
    public int getItemCount() {
        return modList.size();
    }

    public void setList(List<ModManifestEntry> list) {
        this.modList = list;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ModListItemBinding binding;
        private ModManifestEntry modInfo;
        private List<String> configList;

        void setModInfo(ModManifestEntry modInfo) {
            this.modInfo = modInfo;
            File file = new File(modInfo.getAssetPath(), "config.json");
            configList = FileUtils.listAll(modInfo.getAssetPath(), (f) ->
                    !StringUtils.equals(f.getAbsolutePath(), file.getAbsolutePath())
                            && f.getName().endsWith(".json")
                            && !f.getName().startsWith(".")
                            && !StringUtils.equals(f.getName(), "manifest.json")
            );
            if (file.exists()) {
                configList.add(0, file.getAbsolutePath());
            }
            if (configList.size() == 0) {
                binding.buttonConfigMod.setVisibility(View.INVISIBLE);
            } else {
                binding.buttonConfigMod.setVisibility(View.VISIBLE);
            }
            setStrike();
        }

        ViewHolder(@NonNull View view) {
            super(view);
            binding = ModListItemBinding.bind(view);
            binding.buttonRemoveMod.setOnClickListener(v -> removeMod());
            binding.buttonDisableMod.setOnClickListener(v -> disableMod());
            binding.buttonConfigMod.setOnClickListener(v -> configMod());
        }

        private void setStrike() {
            File file = new File(modInfo.getAssetPath());
            if (StringUtils.startsWith(file.getName(), Constants.HIDDEN_FILE_PREFIX)) {
                binding.textViewModName.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                binding.textViewModName.getPaint().setFlags(binding.textViewModName.getPaint().getFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        public List<Integer> removeAll(Predicate<ModManifestEntry> predicate) {
            List<Integer> deletedId = new ArrayList<>();
            for (int i = modList.size() - 1; i >= 0; i--) {
                if (predicate.test(modList.get(i))) {
                    modList.remove(i);
                    deletedId.add(i);
                }
            }
            return deletedId;
        }

        void removeMod() {
            DialogUtils.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_delete_content, (dialog, which) -> {
                if (which == DialogAction.POSITIVE) {
                    File file = new File(modInfo.getAssetPath());
                    if (file.exists()) {
                        try {
                            FileUtils.forceDelete(file);
                            model.removeAll(entry -> StringUtils.equals(entry.getAssetPath(), modInfo.getAssetPath()));
                            List<Integer> removed = removeAll(entry -> StringUtils.equals(entry.getAssetPath(), modInfo.getAssetPath()));
                            for (int idx : removed) {
                                notifyItemRemoved(idx);
                            }
                        } catch (IOException e) {
                            DialogUtils.showAlertDialog(itemView, R.string.error, e.getMessage());
                        }
                    }
                }
            });
        }

        void disableMod() {
            File file = new File(modInfo.getAssetPath());
            if (file.exists() && file.isDirectory()) {
                if (StringUtils.startsWith(file.getName(), Constants.HIDDEN_FILE_PREFIX)) {
                    File newFile = new File(file.getParent(), StringUtils.stripStart(file.getName(), "."));
                    moveMod(file, newFile);
                } else {
                    DialogUtils.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_disable_mod, (dialog, which) -> {
                        if (which == DialogAction.POSITIVE) {
                            File newFile = new File(file.getParent(), "." + file.getName());
                            moveMod(file, newFile);
                        }
                    });
                }
            }
        }

        public Integer findFirst(Predicate<ModManifestEntry> predicate) {
            for (int i = 0; i < modList.size(); i++) {
                if (predicate.test(modList.get(i))) {
                    return i;
                }
            }
            return null;
        }

        private void moveMod(File file, File newFile) {
            try {
                FileUtils.moveDirectory(file, newFile);
                Integer idx = findFirst(mod -> StringUtils.equalsIgnoreCase(mod.getAssetPath(), modInfo.getAssetPath()));
                if (idx != null) {
                    modList.get(idx).setAssetPath(newFile.getAbsolutePath());
                    notifyItemChanged(idx);
                }
            } catch (IOException e) {
                DialogUtils.showAlertDialog(itemView, R.string.error, e.getLocalizedMessage());
            }
        }

        void configMod() {
            if (configList.size() > 0) {
                if (configList.size() > 1) {
                    List<String> selections = configList.stream().map(path -> StringUtils.removeStart(path, modInfo.getAssetPath())).collect(Collectors.toList());
                    DialogUtils.showListItemsDialog(itemView, R.string.menu_config_edit, selections, (materialDialog, index) -> {
                        navigateToConfigEditor(configList.get(index));
                    });
                } else {
                    navigateToConfigEditor(configList.get(0));
                }
            }
        }

        private void navigateToConfigEditor(String path) {
            NavController controller = Navigation.findNavController(itemView);
            MobileNavigationDirections.ActionNavAnyToConfigEditFragment action;
            action = ConfigFragmentDirections.actionNavAnyToConfigEditFragment(path);
            if ("VirtualKeyboard".equals(modInfo.getUniqueID()) && path.equals(new File(modInfo.getAssetPath(), "config.json").getAbsolutePath())) {
                DialogUtils.showListItemsDialog(itemView, R.string.menu_config_edit, R.array.vk_config_mode, ((materialDialog, index) -> {
                    if (index == 0) {
                        action.setVirtualKeyboardConfigMode(true);
                    }
                    controller.navigate(action);
                }));
            } else {
                controller.navigate(action);
            }
        }
    }
}
