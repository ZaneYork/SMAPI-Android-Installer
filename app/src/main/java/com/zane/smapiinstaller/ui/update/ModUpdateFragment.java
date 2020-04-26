package com.zane.smapiinstaller.ui.update;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.utils.JsonUtil;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A fragment representing a list of Items.
 * @author Zane
 */
public class ModUpdateFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ModUpdateFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mod_update_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            CommonLogic.doOnNonNull(this.getArguments(), arguments -> {
                String updateInfoListJson = ModUpdateFragmentArgs.fromBundle(arguments).getUpdateInfoListJson();
                List<ModUpdateCheckResponseDto> updateInfos = JsonUtil.fromJson(updateInfoListJson, new TypeReference<List<ModUpdateCheckResponseDto>>() {
                });
                ModUpdateAdapter adapter = new ModUpdateAdapter(updateInfos);
                recyclerView.setAdapter(adapter);
            });
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        }
        return view;
    }
}
