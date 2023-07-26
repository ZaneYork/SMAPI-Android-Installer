package com.zane.smapiinstaller.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.entity.DownloadableContentList
import com.zane.smapiinstaller.logic.UpdatableListManager

/**
 * A fragment representing a list of Items.
 * @author Zane
 */
class DownloadableContentFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_download_content_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            val recyclerView = view
            recyclerView.layoutManager = LinearLayoutManager(context)
            val manager = UpdatableListManager(
                view,
                "downloadable_content_list.json",
                DownloadableContentList::class.java,
                Constants.DLC_LIST_UPDATE_URL
            )
            val adapter = DownloadableContentAdapter(manager.list.contents)
            recyclerView.adapter = adapter
            manager.registerOnChangeListener { list ->
                adapter.setDownloadableContentList(list.contents)
                true
            }
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context, DividerItemDecoration.VERTICAL
                )
            )
        }
        return view
    }
}