package com.zane.smapiinstaller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;
import com.hjq.language.LanguagesManager;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.constant.AppConfigKey;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.AppConfig;
import com.zane.smapiinstaller.entity.AppConfigDao;
import com.zane.smapiinstaller.entity.DaoSession;
import com.zane.smapiinstaller.entity.FrameworkConfig;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ConfigManager;
import com.zane.smapiinstaller.logic.GameLauncher;
import com.zane.smapiinstaller.utils.DialogUtils;
import com.zane.smapiinstaller.utils.TranslateUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initView();
        } else {
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppCenter.start(getApplication(), Constants.APP_CENTER_SECRET, Analytics.class, Crashes.class);
        requestPermissions();
    }

    private void initView() {
        setSupportActionBar(toolbar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_install, R.id.nav_config, R.id.nav_help, R.id.nav_download, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @OnClick(R.id.launch)
    void launchButtonClick() {
        new GameLauncher(navigationView).launch();
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
        menu.findItem(R.id.settings_developer_mode).setChecked(config.isDeveloperMode());
        Constants.MOD_PATH = config.getModsPath();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.isCheckable()) {
            if (item.isChecked()) {
                item.setChecked(false);
            } else {
                item.setChecked(true);
            }
        }
        ConfigManager manager = new ConfigManager();
        FrameworkConfig config = manager.getConfig();
        switch (item.getItemId()) {
            case R.id.settings_verbose_logging:
                config.setVerboseLogging(item.isChecked());
                break;
            case R.id.settings_check_for_updates:
                config.setCheckForUpdates(item.isChecked());
                break;
            case R.id.settings_developer_mode:
                config.setDeveloperMode(item.isChecked());
                break;
            case R.id.settings_set_mod_path:
                DialogUtils.showInputDialog(this, R.string.input, R.string.input_mods_path, Constants.MOD_PATH, Constants.MOD_PATH, (dialog, input) -> {
                    if(StringUtils.isNoneBlank(input)) {
                        String pathString = input.toString();
                        File file = new File(Environment.getExternalStorageDirectory(), pathString);
                        if(file.exists() && file.isDirectory()) {
                            Constants.MOD_PATH = pathString;
                            config.setModsPath(pathString);
                            manager.flushConfig();
                        }
                        else {
                            DialogUtils.showAlertDialog(drawer, R.string.error, R.string.error_illegal_path);
                        }
                    }
                });
                return true;
            case R.id.settings_language:
                selectLanguageLogic();
                return true;
            case R.id.settings_translation_service:
                selectTranslateServiceLogic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        manager.flushConfig();
        return true;
    }

    private void selectTranslateServiceLogic() {
        DialogUtils.setCurrentDialog(new MaterialDialog.Builder(this).title(R.string.settings_translation_service).items(R.array.translators).itemsCallback((dialog, itemView, position, text) -> {
            DaoSession daoSession = ((MainApplication)this.getApplication()).getDaoSession();
            AppConfigDao appConfigDao = daoSession.getAppConfigDao();
            AppConfig activeTranslator = appConfigDao.queryBuilder().where(AppConfigDao.Properties.Name.eq(AppConfigKey.ACTIVE_TRANSLATOR)).build().unique();
            switch (position) {
                case 0:
                    if(activeTranslator != null) {
                        appConfigDao.delete(activeTranslator);
                    }
                    break;
                case 1:
                    if(activeTranslator == null) {
                        activeTranslator = new AppConfig(null, AppConfigKey.ACTIVE_TRANSLATOR, TranslateUtil.GOOGLE);
                    } else {
                        activeTranslator.setValue(TranslateUtil.GOOGLE);
                    }
                    appConfigDao.insertOrReplace(activeTranslator);
                    break;
                case 2:
                    if(activeTranslator == null) {
                        activeTranslator = new AppConfig(null, AppConfigKey.ACTIVE_TRANSLATOR, TranslateUtil.YOU_DAO);
                    } else {
                        activeTranslator.setValue(TranslateUtil.YOU_DAO);
                    }
                    appConfigDao.insertOrReplace(activeTranslator);
                    break;
                default:
                    break;
            }
        }).show());
    }

    private void selectLanguageLogic() {
        DialogUtils.setCurrentDialog(new MaterialDialog.Builder(this).title(R.string.settings_set_language).items(R.array.languages).itemsCallback((dialog, itemView, position, text) -> {
            boolean restart;
            switch (position) {
                case 0:
                    restart = LanguagesManager.setSystemLanguage(this);
                    break;
                case 1:
                    restart = LanguagesManager.setAppLanguage(this, Locale.ENGLISH);
                    break;
                case 2:
                    restart = LanguagesManager.setAppLanguage(this, Locale.SIMPLIFIED_CHINESE);
                    break;
                case 3:
                    restart = LanguagesManager.setAppLanguage(this, Locale.TRADITIONAL_CHINESE);
                    break;
                case 4:
                    restart = LanguagesManager.setAppLanguage(this, Locale.KOREA);
                    break;
                case 5:
                    restart = LanguagesManager.setAppLanguage(this, new Locale("th", ""));
                    break;
                case 6:
                    restart = LanguagesManager.setAppLanguage(this, new Locale("es", ""));
                    break;
                case 7:
                    restart = LanguagesManager.setAppLanguage(this, Locale.FRENCH);
                    break;
                case 8:
                    restart = LanguagesManager.setAppLanguage(this, new Locale("pt", ""));
                    break;
                case 9:
                    restart = LanguagesManager.setAppLanguage(this, new Locale("in", ""));
                    break;
                default:
                    return;
            }
            if (restart) {
                // 我们可以充分运用 Activity 跳转动画，在跳转的时候设置一个渐变的效果
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                finish();
            }
        }).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 国际化适配（绑定语种）
        super.attachBaseContext(LanguagesManager.attach(newBase));
    }

    @Override
    protected void onDestroy() {
        DialogUtils.dismissDialog();
        super.onDestroy();
    }
}

