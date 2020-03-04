package com.zane.smapiinstaller.ui.config;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.CommonLogic;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ModManifestAdapter extends RecyclerView.Adapter<ModManifestAdapter.ViewHolder> {
    private List<ModManifestEntry> modList;

    public ModManifestAdapter(List<ModManifestEntry> modList){
        this.modList=modList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_list_item,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModManifestEntry mod = modList.get(position);
        holder.modName.setText(mod.getName());
        holder.modDescription.setText(mod.getDescription());
        holder.setModPath(mod.getAssetPath());
    }

    @Override
    public int getItemCount() {
        return modList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
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
        @OnClick(R.id.button_remove_mod) void removeMod() {
            CommonLogic.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_delete_mod, (dialog, which)->{
                if (which == DialogAction.POSITIVE) {
                    File file = new File(modPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
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
