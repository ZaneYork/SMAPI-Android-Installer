package com.zane.smapiinstaller.ui.config;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java9.util.function.Predicate;

/**
 * @author Zane
 */
public class ModManifestAdapter extends RecyclerView.Adapter<ModManifestAdapter.ViewHolder> {
    private ConfigViewModel model;
    private List<ModManifestEntry> modList;

    public ModManifestAdapter(ConfigViewModel model, List<ModManifestEntry> modList){
        this.model=model;
        this.modList = modList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_list_item,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModManifestEntry mod = modList.get(position);
        holder.modName.setText(mod.getName());
        holder.modDescription.setText(StringUtils.firstNonBlank(mod.getTranslatedDescription(), mod.getDescription()));
        holder.setModPath(mod.getAssetPath());
    }

    @Override
    public int getItemCount() {
        return modList.size();
    }

    public void setList(List<ModManifestEntry> list) {
        this.modList = list;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private String modPath;
        void setModPath(String modPath) {
            this.modPath = modPath;
            File file = new File(modPath, "config.json");
            if(!file.exists()) {
                configModButton.setVisibility(View.INVISIBLE);
            }
            else {
                configModButton.setVisibility(View.VISIBLE);
            }
            setStrike();
        }
        @BindView(R.id.button_config_mod)
        Button configModButton;
        @BindView(R.id.text_view_mod_name)
        TextView modName;
        @BindView(R.id.text_view_mod_description)
        TextView modDescription;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setStrike() {
            File file = new File(modPath);
            if(StringUtils.startsWith(file.getName(), Constants.HIDDEN_FILE_PREFIX)) {
                modName.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else {
                modName.getPaint().setFlags(modName.getPaint().getFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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

        @OnClick(R.id.button_remove_mod) void removeMod() {
            DialogUtils.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_delete_content, (dialog, which)->{
                if (which == DialogAction.POSITIVE) {
                    File file = new File(modPath);
                    if (file.exists()) {
                        try {
                            FileUtils.forceDelete(file);
                            model.removeAll(entry -> StringUtils.equals(entry.getAssetPath(), modPath));
                            List<Integer> removed = removeAll(entry -> StringUtils.equals(entry.getAssetPath(), modPath));
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
        @OnClick(R.id.button_disable_mod) void disableMod() {
            File file = new File(modPath);
            if(file.exists() && file.isDirectory()) {
                if(StringUtils.startsWith(file.getName(), Constants.HIDDEN_FILE_PREFIX)) {
                    File newFile = new File(file.getParent(), StringUtils.stripStart(file.getName(), "."));
                    moveMod(file, newFile);
                }
                else {
                    DialogUtils.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_disable_mod, (dialog, which)-> {
                        if(which == DialogAction.POSITIVE) {
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
                Integer idx = findFirst(mod -> StringUtils.equalsIgnoreCase(mod.getAssetPath(), modPath));
                if (idx != null) {
                    modList.get(idx).setAssetPath(newFile.getAbsolutePath());
                    notifyItemChanged(idx);
                }
            } catch (IOException e) {
                DialogUtils.showAlertDialog(itemView, R.string.error, e.getLocalizedMessage());
            }
        }

        @OnClick(R.id.button_config_mod) void configMod() {
            File file = new File(modPath, "config.json");
            if(file.exists()) {
                NavController controller = Navigation.findNavController(itemView);
                ConfigFragmentDirections.ActionNavConfigToConfigEditFragment action = ConfigFragmentDirections.actionNavConfigToConfigEditFragment(file.getAbsolutePath());
                controller.navigate(action);
            }
        }
    }
}
