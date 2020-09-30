package com.zane.smapiinstaller.ui.config;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.databinding.FragmentConfigEditBinding;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

/**
 * @author Zane
 */
public class ConfigEditFragment extends Fragment {
    private Boolean editable;
    private String configPath;

    private FragmentConfigEditBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConfigEditBinding.inflate(inflater, container, false);
        CommonLogic.doOnNonNull(this.getArguments(), arguments -> {
            ConfigEditFragmentArgs args = ConfigEditFragmentArgs.fromBundle(arguments);
            editable = args.getEditable();
            if (!editable) {
                binding.editTextConfigEdit.setKeyListener(null);
                binding.buttonConfigSave.setVisibility(View.INVISIBLE);
                binding.buttonConfigCancel.setVisibility(View.INVISIBLE);
                binding.buttonLogParser.setVisibility(View.VISIBLE);
            }
            configPath = args.getConfigPath();
            File file = new File(configPath);
            if (file.exists() && file.length() < Constants.TEXT_FILE_OPEN_SIZE_LIMIT) {
                String fileText = FileUtils.getFileText(file);
                if (fileText != null) {
                    binding.editTextConfigEdit.setText(fileText);
                }
            } else {
                binding.editTextConfigEdit.setText("");
                binding.editTextConfigEdit.setKeyListener(null);
                DialogUtils.showConfirmDialog(binding.getRoot(), R.string.error, this.getString(R.string.text_too_large), R.string.open_with, R.string.cancel, ((dialog, which) -> {
                    if (which == DialogAction.POSITIVE) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            CommonLogic.doOnNonNull(this.getContext(), (context -> {
                                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                                intent.setDataAndType(contentUri, "text/plain");
                            }));
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), "text/plain");
                        }
                        this.startActivity(intent);
                    }
                    onConfigCancel();
                }));
            }
        });
        binding.buttonConfigCancel.setOnClickListener(v -> onConfigCancel());
        binding.buttonConfigSave.setOnClickListener(v -> onConfigSave());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onConfigSave() {
        try {
            JsonUtil.checkJson(binding.editTextConfigEdit.getText().toString());
            FileOutputStream outputStream = new FileOutputStream(configPath);
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
                outputStreamWriter.write(binding.editTextConfigEdit.getText().toString());
                outputStreamWriter.flush();
            }
        } catch (Exception e) {
            DialogUtils.showAlertDialog(getView(), R.string.error, e.getLocalizedMessage());
        }
    }

    private void onConfigCancel() {
        CommonLogic.doOnNonNull(getView(), view -> Navigation.findNavController(view).popBackStack());
    }

    private void onLogParser() {
        CommonLogic.doOnNonNull(getContext(), context -> CommonLogic.openUrl(context, "https://smapi.io/log"));
    }
}
