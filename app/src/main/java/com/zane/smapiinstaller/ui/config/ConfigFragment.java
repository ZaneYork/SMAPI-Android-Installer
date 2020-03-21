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

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Lists;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.utils.DialogUtils;

import java.util.ArrayList;

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
        ModManifestAdapter modManifestAdapter = new ModManifestAdapter(configViewModel, new ArrayList<>(configViewModel.getModList()));
        recyclerView.setAdapter(modManifestAdapter);
        configViewModel.registerListChangeListener((list) -> {
            modManifestAdapter.setList(new ArrayList<>(list));
            return true;
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        return root;
    }

    @OnTextChanged(R.id.button_search)
    void onSearchMod(CharSequence text) {
        configViewModel.filter(text);
    }

    @OnClick(R.id.button_sort_by)
    void onSortByClick() {
        int index;
        switch (configViewModel.getSortBy()){
            case "Name asc":
                index = 0;
                break;
            case "Name desc":
                index = 1;
                break;
            case "Date asc":
                index = 2;
                break;
            case "Date desc":
                index = 3;
                break;
            default:
                index = 0;
        }
        DialogUtils.setCurrentDialog(new MaterialDialog.Builder(this.getContext()).title(R.string.sort_by).items(R.array.mod_list_sort_by).itemsCallbackSingleChoice(index, (dialog, itemView, position, text) -> {
            switch (position) {
                case 0:
                    configViewModel.switchSortBy("Name asc");
                    break;
                case 1:
                    configViewModel.switchSortBy("Name desc");
                    break;
                case 2:
                    configViewModel.switchSortBy("Date asc");
                    break;
                case 3:
                    configViewModel.switchSortBy("Date desc");
                    break;
                default:
                    return false;
            }
            return true;
        }).show());
    }
}
