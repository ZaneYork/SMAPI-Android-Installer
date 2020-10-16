package com.zane.smapiinstaller.ui.config;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hjq.language.LanguagesManager;
import com.zane.smapiinstaller.BuildConfig;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.databinding.FragmentConfigEditBinding;
import com.zane.smapiinstaller.dto.WebViewObject;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

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
    private WebViewObject webObject = null;

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
                            int height = (int) (binding.scrollView.getMeasuredHeight() / context.getResources().getDisplayMetrics().density * 0.95);
                            String lang = LanguagesManager.getAppLanguage(context).getCountry();
                            switch (lang){
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
                            if (editable) {
                                webObject = new WebViewObject(fileText, "code", lang, true, height);
                                binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject");
                            } else {
                                webObject = new WebViewObject(fileText, "text-plain", lang, false, height);
                                binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject");
                            }
                            String assetText = FileUtils.getAssetText(context, "jsoneditor/editor.html");
                            if (assetText != null) {
                                binding.editTextConfigWebview.loadDataWithBaseURL(
                                        "file:///android_asset/jsoneditor/",
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
        try {
            if(webObject != null) {
                binding.editTextConfigWebview.loadUrl("javascript:getJson()");
                JsonUtil.checkJson(webObject.getText());
                FileOutputStream outputStream = new FileOutputStream(configPath);
                try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
                    outputStreamWriter.write(webObject.getText());
                    outputStreamWriter.flush();
                }
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
