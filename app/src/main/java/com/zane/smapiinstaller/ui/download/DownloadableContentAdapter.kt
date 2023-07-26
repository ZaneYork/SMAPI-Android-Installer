package com.zane.smapiinstaller.ui.download

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.constant.DownloadableContentTypeConstants
import com.zane.smapiinstaller.databinding.DownloadContentItemBinding
import com.zane.smapiinstaller.entity.DownloadableContent
import com.zane.smapiinstaller.entity.ModManifestEntry
import com.zane.smapiinstaller.logic.ModAssetsManager.Companion.findFirstModIf
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.DialogUtils.dismissDialog
import com.zane.smapiinstaller.utils.DialogUtils.showAlertDialog
import com.zane.smapiinstaller.utils.DialogUtils.showProgressDialog
import com.zane.smapiinstaller.utils.FileUtils
import org.apache.commons.lang3.StringUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [RecyclerView.Adapter] that can display a [DownloadableContent]
 *
 * @author Zane
 */
class DownloadableContentAdapter(private var downloadableContentList: List<DownloadableContent>) :
    RecyclerView.Adapter<DownloadableContentAdapter.ViewHolder>() {
    fun setDownloadableContentList(downloadableContentList: List<DownloadableContent>) {
        this.downloadableContentList = downloadableContentList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.download_content_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.updateDownloadableContent(downloadableContentList[position])
    }

    override fun getItemCount(): Int {
        return downloadableContentList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: DownloadContentItemBinding
        private val downloading = AtomicBoolean(false)
        lateinit var downloadableContent: DownloadableContent
        fun updateDownloadableContent(dlc: DownloadableContent) {
            this.downloadableContent = dlc
            binding.textItemType.text = dlc.type
            binding.textItemName.text = dlc.name
            binding.textItemDescription.text = dlc.description
            if (StringUtils.isNoneBlank(dlc.assetPath)) {
                val contentFile = File(itemView.context.filesDir, dlc.assetPath)
                if (contentFile.exists()) {
                    val context = itemView.context
                    val file = File(context.cacheDir, dlc.name + ".zip")
                    if (!file.exists() || !StringUtils.equalsIgnoreCase(
                            FileUtils.getFileHash(file), dlc.hash
                        )
                    ) {
                        binding.buttonRemoveContent.visibility = View.VISIBLE
                        binding.buttonDownloadContent.visibility = View.VISIBLE
                        return
                    }
                    binding.buttonRemoveContent.visibility = View.VISIBLE
                    binding.buttonDownloadContent.visibility = View.INVISIBLE
                    return
                }
            }
            binding.buttonRemoveContent.visibility = View.INVISIBLE
            binding.buttonDownloadContent.visibility = View.VISIBLE

        }


        init {
            binding = DownloadContentItemBinding.bind(view)
            binding.buttonRemoveContent.setOnClickListener { removeContent() }
            binding.buttonDownloadContent.setOnClickListener { downloadContent() }
        }

        fun removeContent() {
            if (StringUtils.isNoneBlank(downloadableContent.assetPath)) {
                val contentFile = File(itemView.context.filesDir, downloadableContent.assetPath)
                if (contentFile.exists()) {
                    DialogUtils.showConfirmDialog(
                        itemView, R.string.confirm, R.string.confirm_delete_content
                    ) { _, which ->
                        if (which === DialogAction.POSITIVE) {
                            try {
                                org.zeroturnaround.zip.commons.FileUtils.forceDelete(contentFile)
                                binding.buttonDownloadContent.visibility = View.VISIBLE
                                binding.buttonRemoveContent.visibility = View.INVISIBLE
                            } catch (e: IOException) {
                                showAlertDialog(itemView, R.string.error, e.localizedMessage)
                            }
                        }
                    }
                }
            }
        }

        fun downloadContent() {
            val context = itemView.context
            var modManifestEntry: ModManifestEntry? = null
            if (StringUtils.equals(
                    downloadableContent.type, DownloadableContentTypeConstants.LOCALE
                )
            ) {
                modManifestEntry = findFirstModIf { mod: ModManifestEntry ->
                    StringUtils.equals(
                        mod.uniqueID, "ZaneYork.CustomLocalization"
                    ) || StringUtils.equals(mod.uniqueID, "SMAPI.CustomLocalization")
                }
                if (modManifestEntry == null) {
                    showAlertDialog(
                        itemView, R.string.error, String.format(
                            context.getString(R.string.error_depends_on_mod),
                            context.getString(R.string.locale_pack),
                            "ZaneYork.CustomLocalization"
                        )
                    )
                    return
                }
            }
            val file = File(context.cacheDir, downloadableContent.name + ".zip")
            if (file.exists()) {
                if (!StringUtils.equalsIgnoreCase(
                        FileUtils.getFileHash(file), downloadableContent.hash
                    )
                ) {
                    try {
                        org.zeroturnaround.zip.commons.FileUtils.forceDelete(file)
                    } catch (e: IOException) {
                        Crashes.trackError(e)
                        return
                    }
                } else {
                    unpackLogic(context, file, modManifestEntry)
                    return
                }
            }
            if (downloading.get()) {
                return
            }
            downloading.set(true)
            val finalModManifestEntry = modManifestEntry
            val dialogRef = showProgressDialog(itemView, R.string.progress, "")
            OkGo.get<File>(downloadableContent.url)
                .execute(object : FileCallback(file.parentFile!!.absolutePath, file.name) {
                    override fun onError(response: Response<File>) {
                        super.onError(response)
                        dismissDialog(itemView, dialogRef.get())
                        downloading.set(false)
                        showAlertDialog(itemView, R.string.error, R.string.error_failed_to_download)
                    }

                    override fun downloadProgress(progress: Progress) {
                        super.downloadProgress(progress)
                        val dialog = dialogRef.get()
                        if (dialog != null) {
                            dialog.setMessage(
                                context.getString(
                                    R.string.downloading,
                                    progress.currentSize / 1024,
                                    progress.totalSize / 1024
                                )
                            )
                            dialog.setProgress((progress.currentSize * 100.0 / progress.totalSize).toInt())
                        }
                    }

                    override fun onSuccess(response: Response<File>) {
                        dismissDialog(itemView, dialogRef.get())
                        downloading.set(false)
                        val downloadedFile = response.body()
                        val hash = FileUtils.getFileHash(downloadedFile)
                        if (!StringUtils.equalsIgnoreCase(hash, downloadableContent.hash)) {
                            showAlertDialog(
                                itemView, R.string.error, R.string.error_failed_to_download
                            )
                            return
                        }
                        unpackLogic(context, downloadedFile, finalModManifestEntry)
                    }
                })
        }

        private fun unpackLogic(
            context: Context, downloadedFile: File, finalModManifestEntry: ModManifestEntry?
        ) {
            try {
                if (StringUtils.equals(
                        downloadableContent.type, DownloadableContentTypeConstants.LOCALE
                    )
                ) {
                    if (finalModManifestEntry != null) {
                        ZipUtil.unpack(downloadedFile, File(finalModManifestEntry.assetPath))
                    }
                } else {
                    ZipUtil.unpack(
                        downloadedFile, File(context.filesDir, downloadableContent.assetPath)
                    )
                }
                showAlertDialog(itemView, R.string.info, R.string.download_unpack_success)
                binding.buttonDownloadContent.visibility = View.INVISIBLE
                binding.buttonRemoveContent.visibility = View.VISIBLE
            } catch (e: Exception) {
                showAlertDialog(itemView, R.string.error, e.localizedMessage)
            }
        }
    }
}