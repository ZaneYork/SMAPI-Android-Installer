package com.zane.smapiinstaller.ui.config;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JSONUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Zane
 */
public class ConfigEditFragment extends Fragment {
    @BindView(R.id.edit_text_config_edit)
    EditText editText;
    private Boolean editable;
    private String configPath;
    @BindView(R.id.button_config_save)
    Button buttonConfigSave;
    @BindView(R.id.button_config_cancel)
    Button buttonConfigCancel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config_edit, container, false);
        ButterKnife.bind(this, root);
        editable = this.getArguments().getBoolean("editable");
        if(!editable) {
            editText.setKeyListener(null);
            buttonConfigSave.setVisibility(View.INVISIBLE);
            buttonConfigCancel.setVisibility(View.INVISIBLE);
        }
        configPath = this.getArguments().getString("configPath");
        if(configPath != null) {
            File file = new File(configPath);
            if(file.exists() && file.length() < Constants.TEXT_FILE_OPEN_SIZE_LIMIT) {
                String fileText = FileUtils.getFileText(file);
                if (fileText != null) {
                    editText.setText(fileText);
                }
            }
            else {
                editText.setText("");
                editText.setKeyListener(null);
                DialogUtils.showConfirmDialog(root, R.string.error, this.getString(R.string.text_too_large), R.string.open_with, R.string.cancel, ((dialog, which) -> {
                    if(which == DialogAction.POSITIVE) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(this.getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                            intent.setDataAndType(contentUri, "text/plain");
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), "text/plain");
                        }
                        this.startActivity(intent);
                    }
                    onConfigCancel();
                }));
            }
        }
        return root;
    }
    @OnClick(R.id.button_config_save) void onConfigSave() {
        try {
            JSONUtil.checkJson(editText.getText().toString());
            FileOutputStream outputStream = new FileOutputStream(configPath);
            try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)){
                outputStreamWriter.write(editText.getText().toString());
                outputStreamWriter.flush();
            }
        }
        catch (Exception e) {
            DialogUtils.showAlertDialog(getView(), R.string.error, e.getLocalizedMessage());
        }
    }

    @OnClick(R.id.button_config_cancel) void onConfigCancel() {
        Navigation.findNavController(getView()).popBackStack();
    }
}
