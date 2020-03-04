package com.zane.smapiinstaller.ui.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.zane.smapiinstaller.R;

public class ConfigFragment extends Fragment {

    private ConfigViewModel configViewModel;

    @BindView(R.id.view_mod_list)
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config, container, false);
        ButterKnife.bind(this, root);
        configViewModel = new ConfigViewModel(root);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(new ModManifestAdapter(configViewModel.getModList().getValue()));
        recyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        configViewModel.getModList().observe(getViewLifecycleOwner(), modList -> {
            recyclerView.getAdapter().notifyDataSetChanged();
        });
        return root;
    }
}
