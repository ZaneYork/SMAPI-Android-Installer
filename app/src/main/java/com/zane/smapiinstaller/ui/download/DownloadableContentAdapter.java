package com.zane.smapiinstaller.ui.download;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lmntrx.android.library.livin.missme.ProgressDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.constant.DownloadableContentTypeConstants;
import com.zane.smapiinstaller.databinding.DownloadContentItemBinding;
import com.zane.smapiinstaller.entity.DownloadableContent;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DownloadableContent}
 *
 * @author Zane
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

    @NonNull
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
        private DownloadContentItemBinding binding;

        private final AtomicBoolean downloading = new AtomicBoolean(false);

        public DownloadableContent downloadableContent;

        public void setDownloadableContent(DownloadableContent downloadableContent) {
            this.downloadableContent = downloadableContent;
            binding.textItemType.setText(downloadableContent.getType());
            binding.textItemName.setText(downloadableContent.getName());
            binding.textItemDescription.setText(downloadableContent.getDescription());
            if (StringUtils.isNoneBlank(downloadableContent.getAssetPath())) {
                File contentFile = new File(itemView.getContext().getFilesDir(), downloadableContent.getAssetPath());
                if (contentFile.exists()) {
                    Context context = itemView.getContext();
                    File file = new File(context.getCacheDir(), downloadableContent.getName() + ".zip");
                    if (!file.exists() || !StringUtils.equalsIgnoreCase(FileUtils.getFileHash(file), downloadableContent.getHash())) {
                        binding.buttonRemoveContent.setVisibility(View.VISIBLE);
                        binding.buttonDownloadContent.setVisibility(View.VISIBLE);
                        return;
                    }
                    binding.buttonRemoveContent.setVisibility(View.VISIBLE);
                    binding.buttonDownloadContent.setVisibility(View.INVISIBLE);
                    return;
                }
            }
            binding.buttonRemoveContent.setVisibility(View.INVISIBLE);
            binding.buttonDownloadContent.setVisibility(View.VISIBLE);
        }

        public ViewHolder(View view) {
            super(view);
            binding = DownloadContentItemBinding.bind(view);
            binding.buttonRemoveContent.setOnClickListener(v -> removeContent());
            binding.buttonDownloadContent.setOnClickListener(v -> downloadContent());
        }

        void removeContent() {
            if (StringUtils.isNoneBlank(downloadableContent.getAssetPath())) {
                File contentFile = new File(itemView.getContext().getFilesDir(), downloadableContent.getAssetPath());
                if (contentFile.exists()) {
                    DialogUtils.showConfirmDialog(itemView, R.string.confirm, R.string.confirm_delete_content, (dialog, which) -> {
                        if (which == DialogAction.POSITIVE) {
                            try {
                                FileUtils.forceDelete(contentFile);
                                binding.buttonDownloadContent.setVisibility(View.VISIBLE);
                                binding.buttonRemoveContent.setVisibility(View.INVISIBLE);
                            } catch (IOException e) {
                                DialogUtils.showAlertDialog(itemView, R.string.error, e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        }

        void downloadContent() {
            Context context = itemView.getContext();
            ModManifestEntry modManifestEntry = null;
            if (StringUtils.equals(downloadableContent.getType(), DownloadableContentTypeConstants.LOCALE)) {
                modManifestEntry = ModAssetsManager.findFirstModIf(mod -> StringUtils.equals(mod.getUniqueID(), "ZaneYork.CustomLocalization") || StringUtils.equals(mod.getUniqueID(), "SMAPI.CustomLocalization"));
                if (modManifestEntry == null) {
                    DialogUtils.showAlertDialog(itemView, R.string.error, String.format(context.getString(R.string.error_depends_on_mod), context.getString(R.string.locale_pack), "ZaneYork.CustomLocalization"));
                    return;
                }
            }
            File file = new File(context.getCacheDir(), downloadableContent.getName() + ".zip");
            if (file.exists()) {
                if (!StringUtils.equalsIgnoreCase(FileUtils.getFileHash(file), downloadableContent.getHash())) {
                    try {
                        FileUtils.forceDelete(file);
                    } catch (IOException e) {
                        Crashes.trackError(e);
                        return;
                    }
                } else {
                    unpackLogic(context, file, modManifestEntry);
                    return;
                }
            }
            if (downloading.get()) {
                return;
            }
            downloading.set(true);
            ModManifestEntry finalModManifestEntry = modManifestEntry;
            AtomicReference<ProgressDialog> dialogRef = DialogUtils.showProgressDialog(itemView, R.string.progress, "");
            OkGo.<File>get(downloadableContent.getUrl()).execute(new FileCallback(file.getParentFile().getAbsolutePath(), file.getName()) {
                @Override
                public void onError(Response<File> response) {
                    super.onError(response);
                    DialogUtils.dismissDialog(itemView, dialogRef.get());
                    downloading.set(false);
                    DialogUtils.showAlertDialog(itemView, R.string.error, R.string.error_failed_to_download);
                }

                @Override
                public void downloadProgress(Progress progress) {
                    super.downloadProgress(progress);
                    ProgressDialog dialog = dialogRef.get();
                    if (dialog != null) {
                        dialog.setMessage(context.getString(R.string.downloading, progress.currentSize / 1024, progress.totalSize / 1024));
                        dialog.setProgress((int) (progress.currentSize * 100.0 / progress.totalSize));
                    }
                }

                @Override
                public void onSuccess(Response<File> response) {
                    DialogUtils.dismissDialog(itemView, dialogRef.get());
                    downloading.set(false);
                    File downloadedFile = response.body();
                    String hash = com.zane.smapiinstaller.utils.FileUtils.getFileHash(downloadedFile);
                    if (!StringUtils.equalsIgnoreCase(hash, downloadableContent.getHash())) {
                        DialogUtils.showAlertDialog(itemView, R.string.error, R.string.error_failed_to_download);
                        return;
                    }
                    unpackLogic(context, downloadedFile, finalModManifestEntry);
                }
            });
        }

        private void unpackLogic(Context context, File downloadedFile, ModManifestEntry finalModManifestEntry) {
            try {
                if (StringUtils.equals(downloadableContent.getType(), DownloadableContentTypeConstants.LOCALE)) {
                    if (finalModManifestEntry != null) {
                        ZipUtil.unpack(downloadedFile, new File(finalModManifestEntry.getAssetPath()));
                    }
                } else {
                    ZipUtil.unpack(downloadedFile, new File(context.getFilesDir(), downloadableContent.getAssetPath()));
                }
                DialogUtils.showAlertDialog(itemView, R.string.info, R.string.download_unpack_success);
                binding.buttonDownloadContent.setVisibility(View.INVISIBLE);
                binding.buttonRemoveContent.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                DialogUtils.showAlertDialog(itemView, R.string.error, e.getLocalizedMessage());
            }
        }
    }
}
