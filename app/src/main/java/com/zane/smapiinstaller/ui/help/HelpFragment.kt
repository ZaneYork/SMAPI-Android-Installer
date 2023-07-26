package com.zane.smapiinstaller.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.MobileNavigationDirections;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.databinding.FragmentHelpBinding;
import com.zane.smapiinstaller.entity.HelpItemList;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.UpdatableListManager;
import com.zane.smapiinstaller.utils.FileUtils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * @author Zane
 */
public class HelpFragment extends Fragment {

    private FragmentHelpBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        binding.viewHelpList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        UpdatableListManager<HelpItemList> manager = new UpdatableListManager<>(binding.getRoot(), "help_item_list.json", HelpItemList.class, Constants.HELP_LIST_UPDATE_URL);
        HelpItemAdapter adapter = new HelpItemAdapter(manager.getList().getItems());
        binding.viewHelpList.setAdapter(adapter);
        manager.registerOnChangeListener((list) -> {
            adapter.setHelpItems(list.getItems());
            return false;
        });
        binding.viewHelpList.addItemDecoration(new DividerItemDecoration(binding.viewHelpList.getContext(), DividerItemDecoration.VERTICAL));
        binding.buttonCompat.setOnClickListener(v -> compat());
        binding.buttonNexus.setOnClickListener(v -> nexus());
        binding.buttonLogs.setOnClickListener(v -> showLog());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void compat() {
        CommonLogic.doOnNonNull(this.getContext(), context -> CommonLogic.openUrl(context, "https://smapi.io/mods"));
    }

    private void nexus() {
        CommonLogic.doOnNonNull(this.getContext(), context -> CommonLogic.openUrl(context, "https://www.nexusmods.com/stardewvalley/mods/"));
    }

    private void showLog() {
        CommonLogic.doOnNonNull(this.getView(), view -> {
            NavController controller = Navigation.findNavController(view);
            File logFile = new File(FileUtils.getStadewValleyBasePath(), Constants.LOG_PATH);
            if (logFile.exists()) {
                MobileNavigationDirections.ActionNavAnyToConfigEditFragment action = HelpFragmentDirections.actionNavAnyToConfigEditFragment(logFile.getAbsolutePath());
                action.setEditable(false);
                controller.navigate(action);
            }
        });
    }
}
