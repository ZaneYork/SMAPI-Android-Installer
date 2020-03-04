package com.zane.smapiinstaller.ui.help;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.afollestad.materialdialogs.DialogAction;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.logic.CommonLogic;

import java.io.File;

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
        CommonLogic.showConfirmDialog(this.getView(), R.string.confirm, R.string.test_message, (dialog, which)-> {
            if (which == DialogAction.POSITIVE) {
                if (this.getString(R.string.test_message).contains("860453392")) {
                    CommonLogic.openUrl(this.getContext(), "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + "AAflCLHiWw1haM1obu_f-CpGsETxXc6b");
                }
            }
        });
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
