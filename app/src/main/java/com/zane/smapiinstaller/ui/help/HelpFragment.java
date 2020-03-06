package com.zane.smapiinstaller.ui.help;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.logic.CommonLogic;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HelpFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_help, container, false);
        ButterKnife.bind(this, root);
        return root;
    }
    @OnClick(R.id.button_compat) void compat() {
        CommonLogic.openUrl(this.getContext(), "https://smapi.io/mods");
    }
    @OnClick(R.id.button_nexus) void nexus() {
        CommonLogic.openUrl(this.getContext(), "https://www.nexusmods.com/stardewvalley/mods/");
    }
    @OnClick(R.id.button_release) void release() {
        CommonLogic.openUrl(this.getContext(), "https://github.com/ZaneYork/SMAPI-Android-Installer/releases");
    }
    @OnClick({R.id.button_logs}) void showLog() {
        NavController controller = Navigation.findNavController(this.getView());
        File logFile = new File(Environment.getExternalStorageDirectory(), Constants.LOG_PATH);
        if(logFile.exists()) {
            HelpFragmentDirections.ActionNavHelpToConfigEditFragment action = HelpFragmentDirections.actionNavHelpToConfigEditFragment(logFile.getAbsolutePath());
            action.setEditable(false);
            controller.navigate(action);
        }
    }
}
