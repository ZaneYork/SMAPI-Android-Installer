package com.zane.smapiinstaller.ui.update

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimaps
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.databinding.UpdatableModListItemBinding
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto
import com.zane.smapiinstaller.entity.ModManifestEntry
import com.zane.smapiinstaller.logic.CommonLogic.getActivityFromView
import com.zane.smapiinstaller.logic.CommonLogic.openUrl
import com.zane.smapiinstaller.logic.ModAssetsManager
import com.zane.smapiinstaller.utils.VersionUtil

/**
 * [RecyclerView.Adapter] that can display a [ModUpdateCheckResponseDto.UpdateInfo]
 *
 * @author Zane
 */
class ModUpdateAdapter(private val updateInfoList: List<ModUpdateCheckResponseDto>) :
    RecyclerView.Adapter<ModUpdateAdapter.ViewHolder>() {
    private val installedModMap: ImmutableListMultimap<String, ModManifestEntry>

    init {
        installedModMap =
            Multimaps.index(ModAssetsManager.findAllInstalledMods()) { obj -> obj!!.uniqueID }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.updatable_mod_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.updateInfo = updateInfoList[position]
    }

    override fun getItemCount(): Int {
        return updateInfoList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var updateInfo: ModUpdateCheckResponseDto? = null
            set(value) {
                field = value
                field?.let { updateInfo ->
                    {
                        val id = updateInfo.id
                        val mod = installedModMap[id].minWithOrNull { a, b ->
                            VersionUtil.compareVersion(
                                a.version, b.version
                            )
                        }
                        if (mod != null) {
                            binding.textViewModName.text = mod.name
                            getActivityFromView(binding.textViewModName)?.let {
                                binding.textViewModVersion.text = it.getString(
                                    R.string.mod_version_update_text,
                                    mod.version,
                                    updateInfo.suggestedUpdate.version
                                )
                            }
                        } else {
                            binding.textViewModName.text = updateInfo.id
                            getActivityFromView(binding.textViewModName)?.let { activity ->
                                binding.textViewModVersion.text = activity.getString(
                                    R.string.mod_version_update_text,
                                    updateInfo.suggestedUpdate.version,
                                    updateInfo.suggestedUpdate.version
                                )
                            }
                        }

                    }
                }
            }
        private val binding: UpdatableModListItemBinding

        init {
            binding = UpdatableModListItemBinding.bind(view)
            binding.buttonUpdateMod.setOnClickListener { onUpdateClick() }
        }

        fun onUpdateClick() {
            getActivityFromView(binding.textViewModName)?.let {
                openUrl(it, updateInfo?.suggestedUpdate?.url)
            }
        }
    }
}