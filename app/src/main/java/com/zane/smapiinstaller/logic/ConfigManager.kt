package com.zane.smapiinstaller.logic

import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.entity.FrameworkConfig
import com.zane.smapiinstaller.utils.FileUtils
import java.io.File

/**
 * 配置管理器
 * @author Zane
 */
class ConfigManager {
    var config: FrameworkConfig

    init {
        val configFile = File(FileUtils.stadewValleyBasePath, Constants.CONFIG_PATH)
        var conf: FrameworkConfig? = null
        if (configFile.exists()) {
            conf = FileUtils.getFileJson(configFile, FrameworkConfig::class.java)
        }
        if (conf != null) {
            config = conf
        }
        else {
            config = FrameworkConfig()
            config.setInitial(true)
        }
    }

    fun flushConfig() {
        val configFile = File(FileUtils.stadewValleyBasePath, Constants.CONFIG_PATH)
        FileUtils.writeFileJson(configFile, config)
    }
}