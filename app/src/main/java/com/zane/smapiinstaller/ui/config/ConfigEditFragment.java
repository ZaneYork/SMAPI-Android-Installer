package com.zane.smapiinstaller.ui.config;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import com.hjq.language.MultiLanguages;
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
import androidx.annotation.RequiresApi;
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
            configPath = args.getConfigPath();
            File file = new File(configPath);
            if (file.exists() && file.length() < Constants.TEXT_FILE_OPEN_SIZE_LIMIT) {
                initAssetWebView();
                binding.scrollView.post(() -> CommonLogic.doOnNonNull(this.getContext(), (context -> onScrollViewRendered(file, context))));
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

    private void onScrollViewRendered(File file, Context context) {
        String fileText = FileUtils.getFileText(file);
        if (fileText != null) {
            String lang = MultiLanguages.getAppLanguage().getLanguage();
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
            if (!virtualKeyboardConfigMode) {
                loadJsonEditor(context, fileText, lang);
            } else {
                loadVirtualKeyboardEditor(context, fileText, lang);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initAssetWebView() {
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this.requireContext()))
                .build();
        binding.editTextConfigWebview.setWebViewClient(new WebViewClient() {
            @Override
            @RequiresApi(21)
            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            @SuppressWarnings("deprecation") // for API < 21
            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }
        });
        WebSettings webViewSettings = binding.editTextConfigWebview.getSettings();
        webViewSettings.setAllowFileAccess(false);
        webViewSettings.setAllowContentAccess(false);
        webViewSettings.setJavaScriptEnabled(true);
    }

    private void loadJsonEditor(Context context, String fileText, String lang) {
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
        Activity activity = CommonLogic.getActivityFromView(binding.editTextConfigWebview);
        if (activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        String targetUrl = "https://appassets.androidplatform.net/assets/jsoneditor/editor.html";
        binding.editTextConfigWebview.loadUrl(targetUrl);
    }

    private void loadVirtualKeyboardEditor(Context context, String fileText, String lang) {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        boolean landscape = true;
        if (height > width) {
            height ^= width; width ^= height; height ^= width;
            landscape = false;
        }
        int widthDp = (int) (binding.scrollView.getMeasuredWidth() / context.getResources().getDisplayMetrics().density * 0.95);
        float scale = widthDp / (float) width;
        KeyboardEditorObject webObject = new KeyboardEditorObject(fileText, lang, height, width, scale, landscape, this::configSave);
        binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject");
        String targetUrl = "https://appassets.androidplatform.net/assets/vkconfig/index.html";
        binding.editTextConfigWebview.loadUrl(targetUrl);
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
