package com.zane.smapiinstaller

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.AppBarConfiguration.Builder
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.abdurazaaqmohammed.AntiSplit.main.LegacyUtils
import com.hjq.language.MultiLanguages
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lmntrx.android.library.livin.missme.ProgressDialog
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.constant.AppConfigKeyConstants
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.databinding.ActivityMainBinding
import com.zane.smapiinstaller.dto.AppUpdateCheckResultDto
import com.zane.smapiinstaller.entity.AppConfig
import com.zane.smapiinstaller.logic.ActivityResultHandler
import com.zane.smapiinstaller.logic.CommonLogic
import com.zane.smapiinstaller.logic.ConfigManager
import com.zane.smapiinstaller.logic.GameLauncher
import com.zane.smapiinstaller.logic.ModAssetsManager
import com.zane.smapiinstaller.utils.ConfigUtils
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.JsonCallback
import com.zane.smapiinstaller.utils.JsonUtil
import com.zane.smapiinstaller.utils.TranslateUtil
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.util.Locale

/**
 * @author Zane
 */
class MainActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var currentFragment = R.id.nav_main

    private lateinit var binding: ActivityMainBinding
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val haveInstallPermission = this.packageManager.canRequestPackageInstalls()
            if (!haveInstallPermission) {
                DialogUtils.showConfirmDialog(
                    instance, R.string.confirm, R.string.request_unknown_source_permission
                ) { _, dialogAction ->
                    if (dialogAction == DialogAction.POSITIVE) {
                        XXPermissions.with(this).permission(Permission.REQUEST_INSTALL_PACKAGES)
                            .request(permissionCallback)
                    } else {
                        finish()
                    }
                }
                return
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                DialogUtils.showConfirmDialog(
                    instance, R.string.confirm, R.string.request_all_files_access_permission
                ) { _, dialogAction ->
                    if (dialogAction == DialogAction.POSITIVE) {
                        XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
                            .request(permissionCallback)
                    } else {
                        finish()
                    }
                }
                return
            }
        }
        XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .permission(Permission.REQUEST_INSTALL_PACKAGES).request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (!all) {
                        requestPermissions()
                        return
                    }
                    initView()
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        XXPermissions.startPermissionActivity(instance, permissions)
                    }
                    requestPermissions()
                }
            })
    }
    fun doesNotHaveStoragePerm(context: Context): Boolean {
        return Build.VERSION.SDK_INT > 22 && (if (LegacyUtils.supportsWriteExternalStorage) context.checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_DENIED else !Environment.isExternalStorageManager())
    }

    private val permissionCallback: OnPermissionCallback
        get() = object : OnPermissionCallback {
            override fun onGranted(permissions: List<String>, all: Boolean) {
                requestPermissions()
            }

            override fun onDenied(permissions: List<String>, never: Boolean) {
                if (never) {
                    XXPermissions.startPermissionActivity(instance, permissions)
                }
                requestPermissions()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initView()
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ActivityResultHandler.triggerListener(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val appConfig = ConfigUtils.getConfig(
            this.application as MainApplication, AppConfigKeyConstants.PRIVACY_POLICY_CONFIRM, false
        )
        if (java.lang.Boolean.parseBoolean(appConfig.value)) {
            requestPermissions()
        } else {
            CommonLogic.showPrivacyPolicy(binding.appBarMain.toolbar) { _, action ->
                if (action == DialogAction.POSITIVE) {
                    appConfig.value = true.toString()
                    ConfigUtils.saveConfig(this.application as MainApplication, appConfig)
                    requestPermissions()
                } else {
                    finish()
                }
            }
        }
        binding.appBarMain.launch.setOnClickListener { launchButtonClick() }
    }

    private fun initView() {
        AppCenter.start(
            application, Constants.APP_CENTER_SECRET, Analytics::class.java, Crashes::class.java
        )
        setSupportActionBar(binding.appBarMain.toolbar)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = Builder(
            R.id.nav_main, R.id.nav_config, R.id.nav_help, R.id.nav_download, R.id.nav_about
        ).setOpenableLayout(binding.drawerLayout).build()
        val navController = findNavController(this, R.id.nav_host_fragment)
        mAppBarConfiguration?.let { setupActionBarWithNavController(this, navController, it) }
        setupWithNavController(binding.navView, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragment = destination.id
            invalidateOptionsMenu()
            if (currentFragment == R.id.nav_about || currentFragment == R.id.nav_help || currentFragment == R.id.config_edit_fragment) {
                binding.appBarMain.launch.visibility = View.INVISIBLE
            } else {
                binding.appBarMain.launch.visibility = View.VISIBLE
            }
        }
        val appConfig = ConfigUtils.getConfig(
            this.application as MainApplication,
            AppConfigKeyConstants.IGNORE_UPDATE_VERSION_CODE,
            Constants.PATCHED_APP_NAME
        )
        Constants.PATCHED_APP_NAME = appConfig.value
        checkAppUpdate()
    }

    private fun checkAppUpdate() {
        val application = this.application as MainApplication
        OkGo.get<AppUpdateCheckResultDto>(Constants.SELF_UPDATE_CHECK_SERVICE_URL)
            .execute(object : JsonCallback<AppUpdateCheckResultDto>(
                AppUpdateCheckResultDto::class.java
            ) {
                override fun onSuccess(response: Response<AppUpdateCheckResultDto>) {
                    val dto = response.body()
                    if (dto != null && CommonLogic.getVersionCode(this@MainActivity) < dto.versionCode) {
                        val appConfig = ConfigUtils.getConfig(
                            application,
                            AppConfigKeyConstants.IGNORE_UPDATE_VERSION_CODE,
                            dto.versionCode
                        )
                        if (StringUtils.equals(appConfig.value, dto.versionCode.toString())) {
                            return
                        }
                        DialogUtils.showConfirmDialog(
                            binding.appBarMain.toolbar,
                            R.string.settings_check_for_updates,
                            this@MainActivity.getString(
                                R.string.app_update_detected, dto.versionName
                            )
                        ) { _, which ->
                            if (which == DialogAction.POSITIVE) {
                                CommonLogic.openUrl(this@MainActivity, Constants.RELEASE_URL)
                            } else {
                                ConfigUtils.saveConfig(application, appConfig)
                            }
                        }
                    }
                }
            })
    }

    fun launchButtonClick() {
        GameLauncher(binding.navView).launch()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val manager = ConfigManager()
        val config = manager.config
        menu.findItem(R.id.settings_verbose_logging).isChecked = config.isVerboseLogging
        menu.findItem(R.id.settings_check_for_updates).isChecked = config.isCheckForUpdates
        menu.findItem(R.id.toolbar_update_check).isVisible = currentFragment == R.id.nav_config
        menu.findItem(R.id.settings_developer_mode).isChecked = config.isDeveloperMode
        menu.findItem(R.id.settings_disable_mono_mod).isChecked = config.isDisableMonoMod
        menu.findItem(R.id.settings_rewrite_missing).isChecked = config.isRewriteMissing
        menu.findItem(R.id.settings_advanced_mode).isChecked = java.lang.Boolean.parseBoolean(
            ConfigUtils.getConfig(
                application as MainApplication, AppConfigKeyConstants.ADVANCED_MODE, "false"
            ).value
        )
        Constants.MOD_PATH = config.modsPath
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.isCheckable) {
            item.isChecked = !item.isChecked
        }
        val manager = ConfigManager()
        val config = manager.config
        if (item.itemId == R.id.settings_verbose_logging) {
            config.isVerboseLogging = item.isChecked
        } else if (item.itemId == R.id.settings_check_for_updates) {
            config.isCheckForUpdates = item.isChecked
        } else if (item.itemId == R.id.settings_developer_mode) {
            config.isDeveloperMode = item.isChecked
        } else if (item.itemId == R.id.settings_disable_mono_mod) {
            config.isDisableMonoMod = item.isChecked
        } else if (item.itemId == R.id.settings_rewrite_missing) {
            config.isRewriteMissing = item.isChecked
        } else if (item.itemId == R.id.settings_set_app_name) {
            DialogUtils.showInputDialog(
                binding.appBarMain.toolbar,
                R.string.input,
                R.string.settings_set_app_name,
                Constants.PATCHED_APP_NAME,
                Constants.PATCHED_APP_NAME,
                true
            ) { _, input ->
                val appName = input.toString()
                val appConfig = ConfigUtils.getConfig(
                    application as MainApplication,
                    AppConfigKeyConstants.IGNORE_UPDATE_VERSION_CODE,
                    appName
                )
                appConfig.value = appName
                ConfigUtils.saveConfig(application as MainApplication, appConfig)
                Constants.PATCHED_APP_NAME = appName
            }
            return true
        } else if (item.itemId == R.id.settings_set_mod_path) {
            DialogUtils.showInputDialog(
                binding.appBarMain.toolbar,
                R.string.input,
                R.string.input_mods_path,
                Constants.MOD_PATH,
                Constants.MOD_PATH
            ) { _, input ->
                if (StringUtils.isNoneBlank(input)) {
                    val pathString = input.toString()
                    val file = File(FileUtils.stadewValleyBasePath, pathString)
                    if (file.exists() && file.isDirectory) {
                        Constants.MOD_PATH = pathString
                        config.modsPath = pathString
                        manager.flushConfig()
                    } else {
                        DialogUtils.showAlertDialog(
                            binding.drawerLayout, R.string.error, R.string.error_illegal_path
                        )
                    }
                }
            }
            return true
        } else if (item.itemId == R.id.settings_language) {
            selectLanguageLogic()
            return true
        } else if (item.itemId == R.id.settings_translation_service) {
            selectTranslateServiceLogic()
            return true
        } else if (item.itemId == R.id.toolbar_update_check) {
            checkModUpdateLogic()
            return true
        } else if (item.itemId == R.id.settings_advanced_mode) {
            val appConfig = ConfigUtils.getConfig(
                application as MainApplication, AppConfigKeyConstants.ADVANCED_MODE, "false"
            )
            appConfig.value = item.isChecked.toString()
            ConfigUtils.saveConfig(application as MainApplication, appConfig)
            startActivity(Intent(this, MainActivity::class.java))
            //            overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
            finish()
        } else {
            return super.onOptionsItemSelected(item)
        }
        manager.flushConfig()
        return true
    }

    private fun getTranslateServiceIndex(selectedTranslator: AppConfig): Int {
        return when (selectedTranslator.value) {
            "OFF" -> 0
            "Google" -> 1
            else -> 2
        }
    }

    private fun selectTranslateServiceLogic() {
        val application = this.application as MainApplication
        val activeTranslator = ConfigUtils.getConfig(
            application, AppConfigKeyConstants.ACTIVE_TRANSLATOR, TranslateUtil.NONE
        )
        val index = getTranslateServiceIndex(activeTranslator)
        DialogUtils.showSingleChoiceDialog(
            binding.appBarMain.toolbar,
            R.string.settings_translation_service,
            R.array.translators,
            index
        ) { _, position ->
            when (position) {
                0 -> {
                    activeTranslator.value = TranslateUtil.NONE
                    ConfigUtils.saveConfig(application, activeTranslator)
                }

                1 -> {
                    activeTranslator.value = TranslateUtil.GOOGLE
                    ConfigUtils.saveConfig(application, activeTranslator)
                }

                2 -> {
                    activeTranslator.value = TranslateUtil.YOU_DAO
                    ConfigUtils.saveConfig(application, activeTranslator)
                }

                else -> {}
            }
        }
    }

    private fun selectLanguageLogic() {
        DialogUtils.showListItemsDialog(
            binding.appBarMain.toolbar, R.string.settings_set_language, R.array.languages
        ) { _, position ->
            val restart: Boolean = when (position) {
                0 -> MultiLanguages.setSystemLanguage(this)
                1 -> MultiLanguages.setAppLanguage(this, Locale.ENGLISH)
                2 -> MultiLanguages.setAppLanguage(this, Locale.SIMPLIFIED_CHINESE)
                3 -> MultiLanguages.setAppLanguage(this, Locale.TRADITIONAL_CHINESE)
                4 -> MultiLanguages.setAppLanguage(this, Locale.KOREA)
                5 -> MultiLanguages.setAppLanguage(this, Locale("th", ""))
                6 -> MultiLanguages.setAppLanguage(this, Locale("es", ""))
                7 -> MultiLanguages.setAppLanguage(this, Locale.FRENCH)
                8 -> MultiLanguages.setAppLanguage(this, Locale("pt", ""))
                9 -> MultiLanguages.setAppLanguage(this, Locale("in", ""))
                10 -> MultiLanguages.setAppLanguage(this, Locale("uk", ""))
                else -> return@showListItemsDialog
            }
            if (restart) {
                // 我们可以充分运用 Activity 跳转动画，在跳转的时候设置一个渐变的效果
                startActivity(Intent(this, MainActivity::class.java))
                //                overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                finish()
            }
        }
    }

    private fun checkModUpdateLogic() {
        val modAssetsManager = ModAssetsManager(binding.appBarMain.toolbar)
        modAssetsManager.checkModUpdate { list ->
            if (list.isEmpty()) {
                CommonLogic.runOnUiThread(this) { activity ->
                    Toast.makeText(
                        activity, R.string.no_update_text, Toast.LENGTH_SHORT
                    ).show()
                }
                return@checkModUpdate
            }
            try {
                val controller = findNavController(this, R.id.nav_host_fragment)
                controller.navigate(
                    MobileNavigationDirections.actionNavAnyToModUpdateFragment(
                        JsonUtil.toJson(list)
                    )
                )
            } catch (e: Exception) {
                Crashes.trackError(e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        return mAppBarConfiguration?.let { navigateUp(navController, it) }
            ?: super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val dialog = DialogUtils.currentDialog
        if (dialog is ProgressDialog) {
            dialog.onBackPressed {
                super.onBackPressed()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // 国际化适配（绑定语种）
        super.attachBaseContext(MultiLanguages.attach(newBase))
    }

    override fun onDestroy() {
        DialogUtils.dismissDialog()
        super.onDestroy()
    }

    fun setFloatingBarVisibility(value: Boolean) {
        binding.appBarMain.launch.visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

    companion object {
        @JvmField
        var instance: MainActivity? = null
    }
}