package com.zane.smapiinstaller.ui.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JSONUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
            String fileText = FileUtils.getFileText(new File(configPath));
            if(fileText != null) {
                editText.setText(fileText);
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
