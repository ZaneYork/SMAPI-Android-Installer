package com.zane.smapiinstaller.ui.update;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ModUpdateCheckResponseDto.UpdateInfo}
 * @author Zane
 */
public class ModUpdateAdapter extends RecyclerView.Adapter<ModUpdateAdapter.ViewHolder> {

    private List<ModUpdateCheckResponseDto.UpdateInfo> updateInfoList;

    public void setUpdateInfoList(List<ModUpdateCheckResponseDto.UpdateInfo> updateInfoList) {
        this.updateInfoList = updateInfoList;
        notifyDataSetChanged();
    }

    public ModUpdateAdapter(List<ModUpdateCheckResponseDto.UpdateInfo> items) {
        updateInfoList = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_content_item, parent, false);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ModUpdateCheckResponseDto.UpdateInfo updateInfo;

        public void setUpdateInfo(ModUpdateCheckResponseDto.UpdateInfo updateInfo) {
            this.updateInfo = updateInfo;
        }

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }
}
