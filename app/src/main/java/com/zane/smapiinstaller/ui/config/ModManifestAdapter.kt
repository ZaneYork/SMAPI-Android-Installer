package com.zane.smapiinstaller.ui.config

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.zane.smapiinstaller.MobileNavigationDirections
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.databinding.ModListItemBinding
import com.zane.smapiinstaller.entity.ModManifestEntry
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.DialogUtils.showAlertDialog
import com.zane.smapiinstaller.utils.DialogUtils.showListItemsDialog
import com.zane.smapiinstaller.utils.FileUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException

/**
 * @author Zane
 */
class ModManifestAdapter(
    private val model: ConfigViewModel, private var modList: MutableList<ModManifestEntry>
) : RecyclerView.Adapter<ModManifestAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.mod_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mod = modList[position]
        holder.binding.textViewModName.text = mod.name
        holder.binding.textViewModDescription.text = StringUtils.firstNonBlank(
            mod.translatedDescription, mod.description
        )
        holder.setModInfo(mod)
    }

    override fun getItemCount(): Int {
        return modList.size
    }

    fun setList(list: MutableList<ModManifestEntry>) {
        modList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: ModListItemBinding
        private lateinit var modInfo: ModManifestEntry
        private var configList: MutableList<String> = mutableListOf()
        fun setModInfo(modInfo: ModManifestEntry) {
            this.modInfo = modInfo
            val file = File(modInfo.assetPath, "config.json")
            configList = FileUtils.listAll(
                modInfo.assetPath
            ) { f ->
                (!StringUtils.equals(
                    f.absolutePath, file.absolutePath
                ) && f.name.endsWith(".json") && !f.name.startsWith(".") && !StringUtils.equals(
                    f.name, "manifest.json"
                ))
            }
            if (file.exists()) {
                configList.add(0, file.absolutePath)
            }
            if (configList.size == 0) {
                binding.buttonConfigMod.visibility = View.INVISIBLE
            } else {
                binding.buttonConfigMod.visibility = View.VISIBLE
            }
            setStrike()
        }

        init {
            binding = ModListItemBinding.bind(view)
            binding.buttonRemoveMod.setOnClickListener { removeMod() }
            binding.buttonDisableMod.setOnClickListener { disableMod() }
            binding.buttonConfigMod.setOnClickListener { configMod() }
        }

        private fun setStrike() {
            val file = File(modInfo.assetPath)
            if (StringUtils.startsWith(file.name, Constants.HIDDEN_FILE_PREFIX)) {
                binding.textViewModName.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.textViewModName.paint.flags =
                    binding.textViewModName.paint.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        fun removeAll(predicate: (ModManifestEntry) -> Boolean): List<Int> {
            val deletedId: MutableList<Int> = ArrayList()
            for (i in modList.indices.reversed()) {
                if (predicate.invoke(modList[i])) {
                    modList.removeAt(i)
                    deletedId.add(i)
                }
            }
            return deletedId
        }

        fun removeMod() {
            DialogUtils.showConfirmDialog(
                itemView, R.string.confirm, R.string.confirm_delete_content
            ) { _, which ->
                if (which === DialogAction.POSITIVE) {
                    val file = File(modInfo.assetPath)
                    if (file.exists()) {
                        try {
                            org.zeroturnaround.zip.commons.FileUtils.forceDelete(file)
                            model.removeAll { entry ->
                                StringUtils.equals(
                                    entry.assetPath, modInfo.assetPath
                                )
                            }
                            val removed = removeAll { entry ->
                                StringUtils.equals(
                                    entry.assetPath, modInfo.assetPath
                                )
                            }
                            for (idx in removed) {
                                notifyItemRemoved(idx)
                            }
                        } catch (e: IOException) {
                            showAlertDialog(itemView, R.string.error, e.message)
                        }
                    }
                }
            }
        }

        fun disableMod() {
            val file = File(modInfo.assetPath)
            if (file.exists() && file.isDirectory) {
                if (StringUtils.startsWith(file.name, Constants.HIDDEN_FILE_PREFIX)) {
                    val newFile = File(file.parent, StringUtils.stripStart(file.name, "."))
                    moveMod(file, newFile)
                } else {
                    DialogUtils.showConfirmDialog(
                        itemView, R.string.confirm, R.string.confirm_disable_mod
                    ) { _, which ->
                        if (which === DialogAction.POSITIVE) {
                            val newFile = File(file.parent, "." + file.name)
                            moveMod(file, newFile)
                        }
                    }
                }
            }
        }

        fun findFirst(predicate: (ModManifestEntry) -> Boolean): Int? {
            for (i in modList.indices) {
                if (predicate.invoke(modList[i])) {
                    return i
                }
            }
            return null
        }

        private fun moveMod(file: File, newFile: File) {
            try {
                org.zeroturnaround.zip.commons.FileUtils.moveDirectory(file, newFile)
                val idx = findFirst { mod ->
                    StringUtils.equalsIgnoreCase(
                        mod.assetPath, modInfo.assetPath
                    )
                }
                if (idx != null) {
                    modList[idx].assetPath = newFile.absolutePath
                    notifyItemChanged(idx)
                }
            } catch (e: IOException) {
                showAlertDialog(itemView, R.string.error, e.localizedMessage)
            }
        }

        fun configMod() {
            if (configList.size > 0) {
                if (configList.size > 1) {
                    val selections = configList.map { path ->
                        StringUtils.removeStart(
                            path, modInfo.assetPath
                        )
                    }.toList()
                    showListItemsDialog(
                        itemView, R.string.menu_config_edit, selections
                    ) { _, index ->
                        navigateToConfigEditor(
                            configList[index]
                        )
                    }
                } else {
                    navigateToConfigEditor(configList[0])
                }
            }
        }

        private fun navigateToConfigEditor(path: String) {
            val controller = findNavController(itemView)
            val action: MobileNavigationDirections.ActionNavAnyToConfigEditFragment
            action = ConfigFragmentDirections.actionNavAnyToConfigEditFragment(path)
            if ("VirtualKeyboard" == modInfo.uniqueID && path == File(
                    modInfo.assetPath, "config.json"
                ).absolutePath
            ) {
                showListItemsDialog(
                    itemView, R.string.menu_config_edit, R.array.vk_config_mode
                ) { _, index ->
                    if (index == 0) {
                        action.virtualKeyboardConfigMode = true
                    }
                    controller.navigate(action)
                }
            } else {
                controller.navigate(action)
            }
        }
    }
}