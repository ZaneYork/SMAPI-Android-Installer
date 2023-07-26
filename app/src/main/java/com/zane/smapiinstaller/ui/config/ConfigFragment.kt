package com.zane.smapiinstaller.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.databinding.FragmentConfigBinding
import com.zane.smapiinstaller.utils.DialogUtils.showSingleChoiceDialog
import com.zane.smapiinstaller.utils.function.TextChangedWatcher

/**
 * @author Zane
 */
class ConfigFragment : Fragment() {
    private lateinit var configViewModel: ConfigViewModel
    private lateinit var binding: FragmentConfigBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfigBinding.inflate(inflater, container, false)
        binding.viewModList.layoutManager = LinearLayoutManager(this.context)
        configViewModel = ConfigViewModel(binding.root)
        val modManifestAdapter = ModManifestAdapter(
            configViewModel, ArrayList(
                configViewModel.modList
            )
        )
        binding.viewModList.adapter = modManifestAdapter
        configViewModel.registerOnChangeListener { list ->
            modManifestAdapter.setList(ArrayList(list))
            false
        }
        binding.viewModList.addItemDecoration(
            DividerItemDecoration(
                binding.viewModList.context, DividerItemDecoration.VERTICAL
            )
        )
        binding.buttonSearch.addTextChangedListener(object : TextChangedWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                configViewModel.filter(s)
            }
        })
        binding.buttonSortBy.setOnClickListener { v -> onSortByClick(v) }
        return binding.root
    }

    fun onSortByClick(v: View?) {
        var index = 0
        when (configViewModel.sortBy) {
            "Name asc" -> index = 0
            "Name desc" -> index = 1
            "Date asc" -> index = 2
            "Date desc" -> index = 3
            else -> {}
        }
        showSingleChoiceDialog(
            binding.viewModList, R.string.sort_by, R.array.mod_list_sort_by, index
        ) { _, position ->
            when (position) {
                0 -> configViewModel.switchSortBy("Name asc")
                1 -> configViewModel.switchSortBy("Name desc")
                2 -> configViewModel.switchSortBy("Date asc")
                3 -> configViewModel.switchSortBy("Date desc")
                else -> {}
            }
        }
    }
}