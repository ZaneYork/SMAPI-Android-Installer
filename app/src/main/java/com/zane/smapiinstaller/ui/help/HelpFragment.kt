package com.zane.smapiinstaller.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.databinding.FragmentHelpBinding
import com.zane.smapiinstaller.entity.HelpItemList
import com.zane.smapiinstaller.logic.CommonLogic.openUrl
import com.zane.smapiinstaller.logic.UpdatableListManager
import com.zane.smapiinstaller.utils.FileUtils
import java.io.File

/**
 * @author Zane
 */
class HelpFragment : Fragment() {
    private lateinit var binding: FragmentHelpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHelpBinding.inflate(inflater, container, false)
        binding.viewHelpList.layoutManager = LinearLayoutManager(this.context)
        val manager = UpdatableListManager(
            binding.root,
            "help_item_list.json",
            HelpItemList::class.java,
            Constants.HELP_LIST_UPDATE_URL
        )
        val adapter = HelpItemAdapter(manager.list.items)
        binding.viewHelpList.adapter = adapter
        manager.registerOnChangeListener { list ->
            adapter.setHelpItems(list.items)
            false
        }
        binding.viewHelpList.addItemDecoration(
            DividerItemDecoration(
                binding.viewHelpList.context, DividerItemDecoration.VERTICAL
            )
        )
        binding.buttonCompat.setOnClickListener { compat() }
        binding.buttonNexus.setOnClickListener { nexus() }
        binding.buttonLogs.setOnClickListener { showLog() }
        return binding.root
    }

    private fun compat() {
        this.context?.let {
            openUrl(it, "https://smapi.io/mods")
        }
    }

    private fun nexus() {
        this.context?.let {
            openUrl(it, "https://www.nexusmods.com/stardewvalley/mods/")
        }
    }

    private fun showLog() {
        this.view?.let { view: View ->
            val controller = findNavController(view)
            val logFile = File(FileUtils.stadewValleyBasePath, Constants.LOG_PATH)
            if (logFile.exists()) {
                val action =
                    HelpFragmentDirections.actionNavAnyToConfigEditFragment(logFile.absolutePath)
                action.editable = false
                controller.navigate(action)
            }
        }
    }
}