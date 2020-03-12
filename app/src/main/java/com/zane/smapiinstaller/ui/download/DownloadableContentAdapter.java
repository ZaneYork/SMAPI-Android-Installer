package com.zane.smapiinstaller.ui.download;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.DownloadableContent;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ModAssetsManager;

import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DownloadableContent}
 */
public class DownloadableContentAdapter extends RecyclerView.Adapter<DownloadableContentAdapter.ViewHolder> {

    private List<DownloadableContent> downloadableContentList;

    public void setDownloadableContentList(List<DownloadableContent> downloadableContentList) {
        this.downloadableContentList = downloadableContentList;
        notifyDataSetChanged();
    }

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
        holder.setDownloadableContent(downloadableContentList.get(position));
    }

    @Override
    public int getItemCount() {
        return downloadableContentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_item_type)
        TextView typeTextView;
        @BindView(R.id.text_item_name)
        TextView nameTextView;
        @BindView(R.id.text_item_description)
        TextView descriptionTextView;
        @BindView(R.id.button_remove_content)
        Button buttonRemove;
        @BindView(R.id.button_download_content)
        Button buttonDownload;
        private AtomicBoolean downloading = new AtomicBoolean(false);

        public DownloadableContent downloadableContent;

        public void setDownloadableContent(DownloadableContent downloadableContent) {
            this.downloadableContent = downloadableContent;
            typeTextView.setText(downloadableContent.getType());
            nameTextView.setText(downloadableContent.getName());
            descriptionTextView.setText(downloadableContent.getDescription());
            if (StringUtils.isBlank(downloadableContent.getAssetPath())) {
                buttonRemove.setVisibility(View.INVISIBLE);
                buttonDownload.setVisibility(View.VISIBLE);
            } else {
                File contentFile = new File(itemView.getContext().getFilesDir(), downloadableContent.getAssetPath());
                if (contentFile.exists()) {
                    buttonRemove.setVisibility(View.VISIBLE);
                    buttonDownload.setVisibility(View.INVISIBLE);
                }
            }
        }

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.button_remove_content)
        void removeContent() {
            if (StringUtils.isNoneBlank(downloadableContent.getAssetPath())) {
                File contentFile = new File(itemView.getContext().getFilesDir(), downloadableContent.getAssetPath());
                if (contentFile.exists()) {
                    CommonLogic.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_delete_content, (dialog, which) -> {
                        if (which == DialogAction.POSITIVE) {
                            try {
                                FileUtils.forceDelete(contentFile);
                            } catch (IOException e) {
                                CommonLogic.showAlertDialog(itemView, R.string.error, e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        }

        @OnClick(R.id.button_download_content)
        void downloadContent() {
            Context context = itemView.getContext();
            ModManifestEntry modManifestEntry = null;
            if (StringUtils.equals(downloadableContent.getType(), "LOCALE")) {
                modManifestEntry = ModAssetsManager.findFirstModIf(mod -> StringUtils.equals(mod.getUniqueID(), "ZaneYork.CustomLocalization") || StringUtils.equals(mod.getUniqueID(), "SMAPI.CustomLocalization"));
                if (modManifestEntry == null) {
                    CommonLogic.showAlertDialog(itemView, R.string.error, String.format(context.getString(R.string.error_depends_on_mod), context.getString(R.string.locale_pack), "ZaneYork.CustomLocalization"));
                    return;
                }
            }
            File file = new File(context.getCacheDir(), downloadableContent.getName() + ".zip");
            if (file.exists()) {
                if (!StringUtils.equalsIgnoreCase(com.zane.smapiinstaller.utils.FileUtils.getFileHash(file), downloadableContent.getHash())) {
                    file.delete();
                } else {
                    unpackLogic(context, file, modManifestEntry);
                    return;
                }
            }
            if (downloading.get())
                return;
            downloading.set(true);
            ModManifestEntry finalModManifestEntry = modManifestEntry;
            AtomicReference<MaterialDialog> dialogRef = CommonLogic.showProgressDialog(itemView, R.string.progress, "");
            OkGo.<File>get(downloadableContent.getUrl()).execute(new FileCallback(file.getParentFile().getAbsolutePath(), file.getName()) {
                @Override
                public void onError(Response<File> response) {
                    super.onError(response);
                    MaterialDialog dialog = dialogRef.get();
                    if (dialog != null && !dialog.isCancelled()) {
                        dialog.dismiss();
                    }
                    downloading.set(false);
                    CommonLogic.showAlertDialog(itemView, R.string.error, R.string.error_failed_to_download);
                }

                @Override
                public void downloadProgress(Progress progress) {
                    super.downloadProgress(progress);
                    MaterialDialog dialog = dialogRef.get();
                    if (dialog != null && !dialog.isCancelled()) {
                        dialog.setContent(R.string.downloading, progress.currentSize / 1024, progress.totalSize / 1024);
                        dialog.setProgress((int) (progress.currentSize * 100.0 / progress.totalSize));
                    }
                }

                @Override
                public void onSuccess(Response<File> response) {
                    MaterialDialog dialog = dialogRef.get();
                    if (dialog != null && !dialog.isCancelled()) {
                        dialog.dismiss();
                    }
                    downloading.set(false);
                    File downloadedFile = response.body();
                    String hash = com.zane.smapiinstaller.utils.FileUtils.getFileHash(downloadedFile);
                    if (!StringUtils.equalsIgnoreCase(hash, downloadableContent.getHash())) {
                        CommonLogic.showAlertDialog(itemView, R.string.error, R.string.error_failed_to_download);
                        return;
                    }
                    unpackLogic(context, downloadedFile, finalModManifestEntry);
                }
            });
        }

        private void unpackLogic(Context context, File downloadedFile, ModManifestEntry finalModManifestEntry) {
            if (StringUtils.equals(downloadableContent.getType(), "LOCALE")) {
                if (finalModManifestEntry != null) {
                    ZipUtil.unpack(downloadedFile, new File(finalModManifestEntry.getAssetPath()));
                }
            } else {
                ZipUtil.unpack(downloadedFile, new File(context.getFilesDir(), downloadableContent.getAssetPath()));
            }
            CommonLogic.showAlertDialog(itemView, R.string.info, R.string.download_unpack_success);
        }
    }
}
