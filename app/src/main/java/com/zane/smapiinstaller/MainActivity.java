package com.zane.smapiinstaller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.hjq.language.MultiLanguages;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lmntrx.android.library.livin.missme.ProgressDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.constant.AppConfigKeyConstants;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.databinding.ActivityMainBinding;
import com.zane.smapiinstaller.dto.AppUpdateCheckResultDto;
import com.zane.smapiinstaller.entity.AppConfig;
import com.zane.smapiinstaller.entity.FrameworkConfig;
import com.zane.smapiinstaller.logic.ActivityResultHandler;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ConfigManager;
import com.zane.smapiinstaller.logic.GameLauncher;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.utils.ConfigUtils;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JsonCallback;
import com.zane.smapiinstaller.utils.JsonUtil;
import com.zane.smapiinstaller.utils.TranslateUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

/**
 * @author Zane
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private int currentFragment = R.id.nav_main;

    public static MainActivity instance;
    private ActivityMainBinding binding;

    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            boolean haveInstallPermission = this.getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {
                DialogUtils.showConfirmDialog(MainActivity.instance, R.string.confirm, R.string.request_unknown_source_permission, ((dialog, dialogAction) -> {
                    if (dialogAction == DialogAction.POSITIVE) {
                        XXPermissions.with(this).permission(Permission.REQUEST_INSTALL_PACKAGES)
                                .request(getPermissionCallback());
                    } else {
                        this.finish();
                    }
                }));
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                DialogUtils.showConfirmDialog(MainActivity.instance, R.string.confirm, R.string.request_all_files_access_permission, ((dialog, dialogAction) -> {
                    if (dialogAction == DialogAction.POSITIVE) {
                        XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
                                .request(getPermissionCallback());
                    } else {
                        this.finish();
                    }
                }));
                return;
            }
        }
        XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .permission(Permission.REQUEST_INSTALL_PACKAGES)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (!all) {
                            requestPermissions();
                            return;
                        }
                        initView();
                    }
                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            XXPermissions.startPermissionActivity(instance, permissions);
                        }
                        requestPermissions();
                    }
                });
    }

    @NonNull
    private OnPermissionCallback getPermissionCallback() {
        return new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                requestPermissions();
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                if (never) {
                    XXPermissions.startPermissionActivity(instance, permissions);
                }
                requestPermissions();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initView();
        } else {
            this.finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultHandler.triggerListener(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppConfig appConfig = ConfigUtils.getConfig((MainApplication) this.getApplication(), AppConfigKeyConstants.PRIVACY_POLICY_CONFIRM, false);
        if (Boolean.parseBoolean(appConfig.getValue())) {
            requestPermissions();
        } else {
            CommonLogic.showPrivacyPolicy(binding.appBarMain.toolbar, (dialog, action) -> {
                if (action == DialogAction.POSITIVE) {
                    appConfig.setValue(String.valueOf(true));
                    ConfigUtils.saveConfig((MainApplication) this.getApplication(), appConfig);
                    requestPermissions();
                } else {
                    this.finish();
                }
            });
        }
        binding.appBarMain.launch.setOnClickListener(v -> launchButtonClick());
    }

    private void initView() {
        AppCenter.start(getApplication(), Constants.APP_CENTER_SECRET, Analytics.class, Crashes.class);
        setSupportActionBar(binding.appBarMain.toolbar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_main, R.id.nav_config, R.id.nav_help, R.id.nav_download, R.id.nav_about)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            currentFragment = destination.getId();
            this.invalidateOptionsMenu();
            if (currentFragment == R.id.nav_about
                    || currentFragment == R.id.nav_help
                    || currentFragment == R.id.config_edit_fragment
            ) {
                binding.appBarMain.launch.setVisibility(View.INVISIBLE);
            }
            else {
                binding.appBarMain.launch.setVisibility(View.VISIBLE);
            }
        });
        AppConfig appConfig = ConfigUtils.getConfig((MainApplication) this.getApplication(), AppConfigKeyConstants.IGNORE_UPDATE_VERSION_CODE, Constants.PATCHED_APP_NAME);
        Constants.PATCHED_APP_NAME = appConfig.getValue();
        checkAppUpdate();
    }

    private void checkAppUpdate() {
        MainApplication application = (MainApplication) this.getApplication();
        OkGo.<AppUpdateCheckResultDto>get(Constants.SELF_UPDATE_CHECK_SERVICE_URL).execute(new JsonCallback<AppUpdateCheckResultDto>(AppUpdateCheckResultDto.class) {
            @Override
            public void onSuccess(Response<AppUpdateCheckResultDto> response) {
                AppUpdateCheckResultDto dto = response.body();
                if (dto != null && CommonLogic.getVersionCode(MainActivity.this) < dto.getVersionCode()) {
                    AppConfig appConfig = ConfigUtils.getConfig(application, AppConfigKeyConstants.IGNORE_UPDATE_VERSION_CODE, dto.getVersionCode());
                    if (StringUtils.equals(appConfig.getValue(), String.valueOf(dto.getVersionCode()))) {
                        return;
                    }
                    DialogUtils.showConfirmDialog(binding.appBarMain.toolbar, R.string.settings_check_for_updates,
                            MainActivity.this.getString(R.string.app_update_detected, dto.getVersionName()), (dialog, which) -> {
                                if (which == DialogAction.POSITIVE) {
                                    CommonLogic.openUrl(MainActivity.this, Constants.RELEASE_URL);
                                } else {
                                    ConfigUtils.saveConfig(application, appConfig);
                                }
                            });
                }
            }
        });
    }

    void launchButtonClick() {
        new GameLauncher(binding.navView).launch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ConfigManager manager = new ConfigManager();
        FrameworkConfig config = manager.getConfig();
        menu.findItem(R.id.settings_verbose_logging).setChecked(config.isVerboseLogging());
        menu.findItem(R.id.settings_check_for_updates).setChecked(config.isCheckForUpdates());
        menu.findItem(R.id.toolbar_update_check).setVisible(currentFragment == R.id.nav_config);
        menu.findItem(R.id.settings_developer_mode).setChecked(config.isDeveloperMode());
        menu.findItem(R.id.settings_disable_mono_mod).setChecked(config.isDisableMonoMod());
        menu.findItem(R.id.settings_rewrite_missing).setChecked(config.isRewriteMissing());
        menu.findItem(R.id.settings_advanced_mode).setChecked(Boolean.parseBoolean(ConfigUtils.getConfig((MainApplication) getApplication(), AppConfigKeyConstants.ADVANCED_MODE, "false").getValue()));
        Constants.MOD_PATH = config.getModsPath();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.isCheckable()) {
            item.setChecked(!item.isChecked());
        }
        ConfigManager manager = new ConfigManager();
        FrameworkConfig config = manager.getConfig();
        if (item.getItemId() == R.id.settings_verbose_logging) {
            config.setVerboseLogging(item.isChecked());
        } else if (item.getItemId() == R.id.settings_check_for_updates) {
            config.setCheckForUpdates(item.isChecked());
        } else if (item.getItemId() == R.id.settings_developer_mode) {
            config.setDeveloperMode(item.isChecked());
        } else if (item.getItemId() == R.id.settings_disable_mono_mod) {
            config.setDisableMonoMod(item.isChecked());
        } else if (item.getItemId() == R.id.settings_rewrite_missing) {
            config.setRewriteMissing(item.isChecked());
        } else if (item.getItemId() == R.id.settings_set_app_name) {
            DialogUtils.showInputDialog(binding.appBarMain.toolbar, R.string.input, R.string.settings_set_app_name, Constants.PATCHED_APP_NAME, Constants.PATCHED_APP_NAME, true, (dialog, input) -> {
                String appName = input.toString();
                AppConfig appConfig = ConfigUtils.getConfig((MainApplication) getApplication(), AppConfigKeyConstants.IGNORE_UPDATE_VERSION_CODE, appName);
                appConfig.setValue(appName);
                ConfigUtils.saveConfig((MainApplication) getApplication(), appConfig);
                Constants.PATCHED_APP_NAME = appName;
            });
            return true;
        } else if (item.getItemId() == R.id.settings_set_mod_path) {
            DialogUtils.showInputDialog(binding.appBarMain.toolbar, R.string.input, R.string.input_mods_path, Constants.MOD_PATH, Constants.MOD_PATH, (dialog, input) -> {
                if (StringUtils.isNoneBlank(input)) {
                    String pathString = input.toString();
                    File file = new File(FileUtils.getStadewValleyBasePath(), pathString);
                    if (file.exists() && file.isDirectory()) {
                        Constants.MOD_PATH = pathString;
                        config.setModsPath(pathString);
                        manager.flushConfig();
                    } else {
                        DialogUtils.showAlertDialog(binding.drawerLayout, R.string.error, R.string.error_illegal_path);
                    }
                }
            });
            return true;
        } else if (item.getItemId() == R.id.settings_language) {
            selectLanguageLogic();
            return true;
        } else if (item.getItemId() == R.id.settings_translation_service) {
            selectTranslateServiceLogic();
            return true;
        } else if (item.getItemId() == R.id.toolbar_update_check) {
            checkModUpdateLogic();
            return true;
        } else if (item.getItemId() == R.id.settings_advanced_mode) {
            AppConfig appConfig = ConfigUtils.getConfig((MainApplication) getApplication(), AppConfigKeyConstants.ADVANCED_MODE, "false");
            appConfig.setValue(String.valueOf(item.isChecked()));
            ConfigUtils.saveConfig((MainApplication) getApplication(), appConfig);
            startActivity(new Intent(this, MainActivity.class));
//            overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
            finish();
        } else {
            return super.onOptionsItemSelected(item);
        }
        manager.flushConfig();
        return true;
    }

    private int getTranslateServiceIndex(AppConfig selectedTranslator) {
        switch (selectedTranslator.getValue()) {
            case "OFF":
                return 0;
            case "Google":
                return 1;
            default:
                return 2;
        }
    }

    private void selectTranslateServiceLogic() {
        MainApplication application = (MainApplication) this.getApplication();
        AppConfig activeTranslator = ConfigUtils.getConfig(application, AppConfigKeyConstants.ACTIVE_TRANSLATOR, TranslateUtil.NONE);
        int index = getTranslateServiceIndex(activeTranslator);
        DialogUtils.showSingleChoiceDialog(binding.appBarMain.toolbar, R.string.settings_translation_service, R.array.translators, index, (dialog, position) -> {
            switch (position) {
                case 0:
                    activeTranslator.setValue(TranslateUtil.NONE);
                    ConfigUtils.saveConfig(application, activeTranslator);
                    break;
                case 1:
                    activeTranslator.setValue(TranslateUtil.GOOGLE);
                    ConfigUtils.saveConfig(application, activeTranslator);
                    break;
                case 2:
                    activeTranslator.setValue(TranslateUtil.YOU_DAO);
                    ConfigUtils.saveConfig(application, activeTranslator);
                    break;
                default:
            }
        });
    }

    private void selectLanguageLogic() {
        DialogUtils.showListItemsDialog(binding.appBarMain.toolbar, R.string.settings_set_language, R.array.languages, (dialog, position) -> {
            boolean restart;
            switch (position) {
                case 0:
                    restart = MultiLanguages.setSystemLanguage(this);
                    break;
                case 1:
                    restart = MultiLanguages.setAppLanguage(this, Locale.ENGLISH);
                    break;
                case 2:
                    restart = MultiLanguages.setAppLanguage(this, Locale.SIMPLIFIED_CHINESE);
                    break;
                case 3:
                    restart = MultiLanguages.setAppLanguage(this, Locale.TRADITIONAL_CHINESE);
                    break;
                case 4:
                    restart = MultiLanguages.setAppLanguage(this, Locale.KOREA);
                    break;
                case 5:
                    restart = MultiLanguages.setAppLanguage(this, new Locale("th", ""));
                    break;
                case 6:
                    restart = MultiLanguages.setAppLanguage(this, new Locale("es", ""));
                    break;
                case 7:
                    restart = MultiLanguages.setAppLanguage(this, Locale.FRENCH);
                    break;
                case 8:
                    restart = MultiLanguages.setAppLanguage(this, new Locale("pt", ""));
                    break;
                case 9:
                    restart = MultiLanguages.setAppLanguage(this, new Locale("in", ""));
                    break;
                case 10:
                    restart = MultiLanguages.setAppLanguage(this, new Locale("uk", ""));
                    break;
                default:
                    return;
            }
            if (restart) {
                // 我们可以充分运用 Activity 跳转动画，在跳转的时候设置一个渐变的效果
                startActivity(new Intent(this, MainActivity.class));
//                overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                finish();
            }
        });
    }

    private void checkModUpdateLogic() {
        ModAssetsManager modAssetsManager = new ModAssetsManager(binding.appBarMain.toolbar);
        modAssetsManager.checkModUpdate((list) -> {
            if (list.isEmpty()) {
                CommonLogic.runOnUiThread(this, (activity) -> Toast.makeText(activity, R.string.no_update_text, Toast.LENGTH_SHORT).show());
                return;
            }
            try {
                NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);
                controller.navigate(MobileNavigationDirections.actionNavAnyToModUpdateFragment(JsonUtil.toJson(list)));
            } catch (Exception e) {
                Crashes.trackError(e);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Object dialog = DialogUtils.getCurrentDialog();
        if (dialog instanceof ProgressDialog) {
            ProgressDialog progressDialog = ((ProgressDialog) dialog);
            progressDialog.onBackPressed(
                    () -> {
                        super.onBackPressed();
                        return null;
                    }
            );
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 国际化适配（绑定语种）
        super.attachBaseContext(MultiLanguages.attach(newBase));
    }

    @Override
    protected void onDestroy() {
        DialogUtils.dismissDialog();
        super.onDestroy();
    }

    public void setFloatingBarVisibility(boolean value) {
        binding.appBarMain.launch.setVisibility(value ? View.VISIBLE : View.INVISIBLE);
    }
}

