package com.zane.smapiinstaller.ui.config

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import com.hjq.language.MultiLanguages
import com.zane.smapiinstaller.BuildConfig
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.databinding.FragmentConfigEditBinding
import com.zane.smapiinstaller.dto.JsonEditorObject
import com.zane.smapiinstaller.dto.KeyboardEditorObject
import com.zane.smapiinstaller.logic.CommonLogic.getActivityFromView
import com.zane.smapiinstaller.logic.CommonLogic.openUrl
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.JsonUtil
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * @author Zane
 */
class ConfigEditFragment : Fragment() {
    private var editable: Boolean = false
    private var virtualKeyboardConfigMode: Boolean = false
    private var configPath: String? = null
    private lateinit var binding: FragmentConfigEditBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfigEditBinding.inflate(inflater, container, false)
        initView()
        binding.buttonConfigCancel.setOnClickListener { onConfigCancel() }
        binding.buttonConfigSave.setOnClickListener { onConfigSave() }
        binding.buttonLogParser.setOnClickListener { onLogParser() }
        return binding.root
    }

    private fun initView() {
        this.arguments?.let {
            val args = ConfigEditFragmentArgs.fromBundle(it)
            editable = args.editable
            virtualKeyboardConfigMode = args.virtualKeyboardConfigMode
            if (!editable) {
                binding.buttonConfigSave.visibility = View.INVISIBLE
                binding.buttonConfigCancel.visibility = View.INVISIBLE
                binding.buttonLogParser.visibility = View.VISIBLE
            }
            configPath = args.configPath
            configPath?.let { path ->
                val file = File(path)
                if (file.exists() && file.length() < Constants.TEXT_FILE_OPEN_SIZE_LIMIT) {
                    initAssetWebView()
                    binding.scrollView.post {
                        this.context?.let {
                            onScrollViewRendered(
                                file, it
                            )
                        }
                    }
                } else {
                    DialogUtils.showConfirmDialog(
                        binding.root,
                        R.string.error,
                        this.getString(R.string.text_too_large),
                        R.string.open_with,
                        R.string.cancel
                    ) { _, which ->
                        if (which === DialogAction.POSITIVE) {
                            val intent = Intent("android.intent.action.VIEW")
                            intent.addCategory("android.intent.category.DEFAULT")
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                this.context?.let { context ->
                                    val contentUri = FileProvider.getUriForFile(
                                        context, BuildConfig.APPLICATION_ID + ".provider", file
                                    )
                                    intent.setDataAndType(contentUri, "text/plain")
                                }
                            } else {
                                intent.setDataAndType(Uri.fromFile(file), "text/plain")
                            }
                            this.startActivity(intent)
                        }
                        onConfigCancel()
                    }
                }
            }
        }
    }

    private fun onScrollViewRendered(file: File, context: Context) {
        val fileText = FileUtils.getFileText(file)
        if (fileText != null) {
            var lang = MultiLanguages.getAppLanguage().language
            when (lang) {
                "zh" -> lang = "zh-CN"
                "fr" -> lang = "fr-FR"
                "pt" -> lang = "pt-BR"
                else -> {}
            }
            if (!virtualKeyboardConfigMode) {
                loadJsonEditor(context, fileText, lang)
            } else {
                loadVirtualKeyboardEditor(context, fileText, lang)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initAssetWebView() {
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(requireContext())).build()
        binding.editTextConfigWebview.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView, request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }
        val webViewSettings = binding.editTextConfigWebview.settings
        webViewSettings.allowFileAccess = false
        webViewSettings.allowContentAccess = false
        webViewSettings.javaScriptEnabled = true
    }

    private fun loadJsonEditor(context: Context, fileText: String, lang: String) {
        val height =
            (binding.scrollView.measuredHeight / context.resources.displayMetrics.density * 0.95).toInt()
        val webObject: JsonEditorObject
        if (editable) {
            try {
                JsonUtil.checkJson(fileText)
                val jsonText = JsonUtil.toJson(JsonUtil.fromJson(fileText, Any::class.java))
                webObject = JsonEditorObject(
                    jsonText, "tree", lang, true, height
                ) { config -> configSave(config) }
                binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject")
            } catch (e: Exception) {
                DialogUtils.showAlertDialog(view, R.string.error, e.localizedMessage)
                return
            }
        } else {
            webObject = JsonEditorObject(fileText, "text-plain", lang, false, height, null)
            binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject")
        }
        getActivityFromView(binding.editTextConfigWebview)?.let {
            if (it.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        val targetUrl = "https://appassets.androidplatform.net/assets/jsoneditor/editor.html"
        binding.editTextConfigWebview.loadUrl(targetUrl)
    }

    private fun loadVirtualKeyboardEditor(context: Context, fileText: String, lang: String) {
        var height = context.resources.displayMetrics.heightPixels
        var width = context.resources.displayMetrics.widthPixels
        var landscape = true
        if (height > width) {
            height = height xor width
            width = width xor height
            height = height xor width
            landscape = false
        }
        val widthDp =
            (binding.scrollView.measuredWidth / context.resources.displayMetrics.density * 0.95).toInt()
        val scale = widthDp / width.toFloat()
        val webObject = KeyboardEditorObject(
            fileText, lang, height, width, scale, landscape
        ) { config -> configSave(config) }
        binding.editTextConfigWebview.addJavascriptInterface(webObject, "webObject")
        val targetUrl = "https://appassets.androidplatform.net/assets/vkconfig/index.html"
        binding.editTextConfigWebview.loadUrl(targetUrl)
    }

    private fun onConfigSave() {
        binding.editTextConfigWebview.loadUrl("javascript:getJson()")
    }

    private fun configSave(config: String) {
        try {
            JsonUtil.checkJson(config)
            val outputStream = FileOutputStream(configPath)
            OutputStreamWriter(outputStream).use { outputStreamWriter ->
                outputStreamWriter.write(config)
                outputStreamWriter.flush()
            }
        } catch (e: Exception) {
            DialogUtils.showAlertDialog(view, R.string.error, e.localizedMessage)
        }
    }

    private fun onConfigCancel() {
        view?.let {
            findNavController(it).popBackStack()
        }
    }

    private fun onLogParser() {
        context?.let {
            openUrl(it, "https://smapi.io/log")
        }
    }
}