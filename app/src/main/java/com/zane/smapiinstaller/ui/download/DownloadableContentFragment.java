package com.zane.smapiinstaller.ui.download;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.entity.DownloadableContentList;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.DownloadabeContentManager;

/**
 * A fragment representing a list of Items.
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
            DownloadabeContentManager manager = new DownloadabeContentManager(view);
            recyclerView.setAdapter(new DownloadableContentAdapter(manager.getDownloadableContentList().getContents()));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        }
        return view;
    }
}
