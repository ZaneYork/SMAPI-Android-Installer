package com.zane.smapiinstaller.ui.config;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.hjq.language.LanguagesManager;
import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.databinding.FragmentConfigEditBinding;
import com.zane.smapiinstaller.dto.JsonEditorObject;
import com.zane.smapiinstaller.dto.KeyboardEditorObject;
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
    private Boolean virtualKeyboardConfigMode;
    private String configPath;

    private FragmentConfigEditBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConfigEditBinding.inflate(inflater, container, false);
        initView();
        binding.buttonConfigCancel.setOnClickListener(v -> onConfigCancel());
        binding.buttonConfigSave.setOnClickListener(v -> onConfigSave());
        binding.buttonLogParser.setOnClickListener(v -> onLogParser());
        return binding.getRoot();
    }

    private void initView() {
        CommonLogic.doOnNonNull(this.getArguments(), arguments -> {
            ConfigEditFragmentArgs args = ConfigEditFragmentArgs.fromBundle(arguments);
            editable = args.getEditable();
            virtualKeyboardConfigMode = args.getVirtualKeyboardConfigMode();
            if (!editable) {
                binding.buttonConfigSave.setVisibility(View.INVISIBLE);
                binding.buttonConfigCancel.setVisibility(View.INVISIBLE);
                binding.buttonLogParser.setVisibility(View.VISIBLE);
            }
            binding.editTextConfigWebview.getSettings().setJavaScriptEnabled(true);
            binding.editTextConfigWebview.setWebChromeClient(new WebChromeClient());
            binding.editTextConfigWebview.setWebViewClient(new WebViewClient());
            configPath = args.getConfigPath();
            File file = new File(configPath);
            if (file.exists() && file.length() < Constants.TEXT_FILE_OPEN_SIZE_LIMIT) {
                String fileText = FileUtils.getFileText(file);
                if (fileText != null) {
                    binding.scrollView.post(() -> {
                        CommonLogic.doOnNonNull(this.getContext(), (context -> {
                            String lang = LanguagesManager.getAppLanguage(context).getLanguage();
                            switch (lang) {
                                case "zh":
                                    lang = "zh-CN";
                                    break;
                                case "fr":
                                    lang = "fr-FR";
                                    break;
                                case "pt":
                                    lang = "pt-BR";
                                    break;
                                default:
                                    break;
                            }
                            String assetText;
                            String baseUrl;
                            if(!virtualKeyboardConfigMode) {
                                int height = (int) (binding.scrollView.getMeasuredHeight() / context.getResources().getDisplayMetrics().density * 0.95);
                                JsonEditorObject webObject;
                                if (editable) {
                                    try {
                                        JsonUtil.checkJson(fileText);
                                        String jsonText = JsonUtil.toJson(JsonUtil.fromJson(fileText, Object.class));
                                        webObject = new JsonEditorObject(jsonText, "tree", lang, true, height, this::configSave);
                                        binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject");
                                    } catch (Exception e) {
                                        DialogUtils.showAlertDialog(getView(), R.string.error, e.getLocalizedMessage());
                                        return;
                                    }
                                } else {
                                    webObject = new JsonEditorObject(fileText, "text-plain", lang, false, height, null);
                                    binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject");
                                }
                                baseUrl = "file:///android_asset/jsoneditor/";
                                assetText = FileUtils.getAssetText(context, "jsoneditor/editor.html");
                                Activity activity = CommonLogic.getActivityFromView(binding.editTextConfigWebview);
                                if(activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                }
                            }
                            else {
                                int height = context.getResources().getDisplayMetrics().heightPixels;
                                int width = context.getResources().getDisplayMetrics().widthPixels;
                                boolean landscape = true;
                                if(height > width) {
                                    height ^= width; width ^= height; height ^= width;
                                    landscape = false;
                                }
                                int widthDp = (int) (binding.scrollView.getMeasuredWidth() / context.getResources().getDisplayMetrics().density * 0.95);
                                float scale = widthDp / (float)width;
                                KeyboardEditorObject webObject = new KeyboardEditorObject(fileText, lang, height, width, scale, landscape, this::configSave);
                                binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject");
                                assetText = FileUtils.getAssetText(context, "vkconfig/index.html");
                                baseUrl = "file:///android_asset/vkconfig/";
                            }
                            if (assetText != null) {
                                binding.editTextConfigWebview.loadDataWithBaseURL(
                                        baseUrl,
                                        assetText,
                                        "text/html",
                                        "utf-8", "");
                            }
                        }));
                    });
                }
            } else {
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onConfigSave() {
        binding.editTextConfigWebview.loadUrl("javascript:getJson()");
    }

    private void configSave(String config) {
        try {
            JsonUtil.checkJson(config);
            FileOutputStream outputStream = new FileOutputStream(configPath);
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
                outputStreamWriter.write(config);
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
