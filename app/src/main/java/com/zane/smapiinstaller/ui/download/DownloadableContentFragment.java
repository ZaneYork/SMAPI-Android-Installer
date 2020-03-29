package com.zane.smapiinstaller.ui.download;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.DownloadableContentList;
import com.zane.smapiinstaller.logic.UpdatableListManager;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A fragment representing a list of Items.
 * @author Zane
 */
public class DownloadableContentFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DownloadableContentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_content_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            UpdatableListManager<DownloadableContentList> manager = new UpdatableListManager<>(view, "downloadable_content_list.json", DownloadableContentList.class, Constants.DLC_LIST_UPDATE_URL);
            DownloadableContentAdapter adapter = new DownloadableContentAdapter(manager.getList().getContents());
            recyclerView.setAdapter(adapter);
            manager.registerOnChangeListener((list) -> {
                adapter.setDownloadableContentList(list.getContents());
                return true;
            });
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        }
        return view;
    }
}
