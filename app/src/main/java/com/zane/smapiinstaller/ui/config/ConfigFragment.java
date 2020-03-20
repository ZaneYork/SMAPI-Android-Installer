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
import butterknife.OnClick;
import butterknife.OnTextChanged;

import com.zane.smapiinstaller.R;

public class ConfigFragment extends Fragment {

    @BindView(R.id.view_mod_list)
    RecyclerView recyclerView;
    private ConfigViewModel configViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config, container, false);
        ButterKnife.bind(this, root);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        configViewModel = new ConfigViewModel(root);
        ModManifestAdapter modManifestAdapter = new ModManifestAdapter(configViewModel, configViewModel.getModList());
        recyclerView.setAdapter(modManifestAdapter);
        configViewModel.registerListChangeListener((list) -> {
            modManifestAdapter.setList(list);
            return true;
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        return root;
    }

    @OnTextChanged(R.id.button_search) void onSearchMod(CharSequence text){
        configViewModel.filter(text);
    }
}
