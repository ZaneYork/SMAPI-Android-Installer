package com.zane.smapiinstaller.logic;

import android.os.Environment;

import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.FrameworkConfig;
import com.zane.smapiinstaller.utils.FileUtils;

import java.io.File;

/**
 * 配置管理器
 * @author Zane
 */
public class ConfigManager {
    private FrameworkConfig config;

    public ConfigManager() {
        File configFile = new File(Environment.getExternalStorageDirectory(), Constants.CONFIG_PATH);
        if(configFile.exists()) {
            config = FileUtils.getFileJson(configFile, FrameworkConfig.class);
        }
        if(config == null) {
            config = new FrameworkConfig();
            config.setInitial(true);
        }
    }

    public FrameworkConfig getConfig() {
        return config;
    }

    public void flushConfig() {
        File configFile = new File(Environment.getExternalStorageDirectory(), Constants.CONFIG_PATH);
        FileUtils.writeFileJson(configFile, config);
    }
}
