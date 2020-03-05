package com.zane.smapiinstaller.ui.download;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.DownloadableContent;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DownloadableContent}
 */
public class DownloadableContentAdapter extends RecyclerView.Adapter<DownloadableContentAdapter.ViewHolder> {

    private final List<DownloadableContent> downloadableContentList;

    public DownloadableContentAdapter(List<DownloadableContent> items) {
        downloadableContentList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_content_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.downloadableContent = downloadableContentList.get(position);
        holder.typeTextView.setText(holder.downloadableContent.getType());
        holder.nameTextView.setText(holder.downloadableContent.getName());
        holder.descriptionTextView.setText(holder.downloadableContent.getDescription());
    }

    @Override
    public int getItemCount() {
        return downloadableContentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_item_type)
        TextView typeTextView;
        @BindView(R.id.text_item_name)
        TextView nameTextView;
        @BindView(R.id.text_item_description)
        TextView descriptionTextView;
        public DownloadableContent downloadableContent;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
        @OnClick(R.id.button_remove_content) void removeContent() {

        }
        @OnClick(R.id.button_download_content) void downloadContent() {

        }
    }
}
