package com.zane.smapiinstaller.ui.update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.core.type.TypeReference
import com.zane.smapiinstaller.databinding.FragmentModUpdateListBinding
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto
import com.zane.smapiinstaller.utils.JsonUtil

/**
 * A fragment representing a list of Items.
 * @author Zane
 */
class ModUpdateFragment : Fragment() {
    private lateinit var binding: FragmentModUpdateListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentModUpdateListBinding.inflate(inflater, container, false)
        // Set the adapter
        val context = binding.root.context
        val recyclerView = binding.root
        recyclerView.layoutManager = LinearLayoutManager(context)
        this.arguments?.let { arguments ->
            val updateInfoListJson = ModUpdateFragmentArgs.fromBundle(
                arguments
            ).updateInfoListJson
            val updateInfos: List<ModUpdateCheckResponseDto> = JsonUtil.fromJson(updateInfoListJson,
                object : TypeReference<List<ModUpdateCheckResponseDto>>() {})
            val adapter = ModUpdateAdapter(updateInfos)
            recyclerView.adapter = adapter
        }
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context, DividerItemDecoration.VERTICAL
            )
        )
        return binding.root
    }
}