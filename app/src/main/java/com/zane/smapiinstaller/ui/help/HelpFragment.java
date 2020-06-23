package com.zane.smapiinstaller.ui.help;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.MobileNavigationDirections;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.HelpItemList;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.UpdatableListManager;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Zane
 */
public class HelpFragment extends Fragment {

    @BindView(R.id.view_help_list)
    RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_help, container, false);
        ButterKnife.bind(this, root);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        UpdatableListManager<HelpItemList> manager = new UpdatableListManager<>(root, "help_item_list.json", HelpItemList.class, Constants.HELP_LIST_UPDATE_URL);
        HelpItemAdapter adapter = new HelpItemAdapter(manager.getList().getItems());
        recyclerView.setAdapter(adapter);
        manager.registerOnChangeListener((list) -> {
            adapter.setHelpItems(list.getItems());
            return false;
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        return root;
    }
    @OnClick(R.id.button_compat) void compat() {
        CommonLogic.doOnNonNull(this.getContext(), context -> CommonLogic.openUrl(context, "https://smapi.io/mods"));
    }
    @OnClick(R.id.button_nexus) void nexus() {
        CommonLogic.doOnNonNull(this.getContext(), context -> CommonLogic.openUrl(context, "https://www.nexusmods.com/stardewvalley/mods/"));
    }
    @OnClick({R.id.button_logs}) void showLog() {
        CommonLogic.doOnNonNull(this.getView(), view -> {
            NavController controller = Navigation.findNavController(view);
            File logFile = new File(Environment.getExternalStorageDirectory(), Constants.LOG_PATH);
            if(logFile.exists()) {
                MobileNavigationDirections.ActionNavAnyToConfigEditFragment action = HelpFragmentDirections.actionNavAnyToConfigEditFragment(logFile.getAbsolutePath());
                action.setEditable(false);
                controller.navigate(action);
            }
        });
    }
}
