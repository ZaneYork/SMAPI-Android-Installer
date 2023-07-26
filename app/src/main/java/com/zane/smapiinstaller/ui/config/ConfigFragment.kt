package com.zane.smapiinstaller.ui.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.databinding.FragmentConfigBinding;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.function.TextChangedWatcher;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * @author Zane
 */
public class ConfigFragment extends Fragment {

    private ConfigViewModel configViewModel;

    private FragmentConfigBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConfigBinding.inflate(inflater, container, false);
        binding.viewModList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        configViewModel = new ConfigViewModel(binding.getRoot());
        ModManifestAdapter modManifestAdapter = new ModManifestAdapter(configViewModel, new ArrayList<>(configViewModel.getModList()));
        binding.viewModList.setAdapter(modManifestAdapter);
        configViewModel.registerOnChangeListener((list) -> {
            modManifestAdapter.setList(new ArrayList<>(list));
            return false;
        });
        binding.viewModList.addItemDecoration(new DividerItemDecoration(binding.viewModList.getContext(), DividerItemDecoration.VERTICAL));
        binding.buttonSearch.addTextChangedListener(new TextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                configViewModel.filter(s);
            }
        });
        binding.buttonSortBy.setOnClickListener(this::onSortByClick);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onSortByClick(View v) {
        int index = 0;
        switch (configViewModel.getSortBy()) {
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
        }
        DialogUtils.showSingleChoiceDialog(binding.viewModList, R.string.sort_by, R.array.mod_list_sort_by, index, (dialog, position) -> {
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
            }
        });
    }
}
