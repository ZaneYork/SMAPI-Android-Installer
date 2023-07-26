package com.zane.smapiinstaller.constant

/**
 * 常量
 * @author Zane
 */
object Constants {
    /**
     * Mod安装路径
     */
    @JvmField
    var MOD_PATH = "StardewValley/Mods"

    /**
     * 日志路径
     */
    const val LOG_PATH = "StardewValley/ErrorLogs/SMAPI-latest.txt"

    /**
     * 配置文件路径
     */
    const val CONFIG_PATH = "StardewValley/smapi-internal/config.user.json"

    /**
     * 原始安装包名
     */
    const val ORIGIN_PACKAGE_NAME_GOOGLE = "com.chucklefish.stardewvalley"

    /**
     * 安装包目标包名
     */
    const val TARGET_PACKAGE_NAME = "com.zane.stardewvalley"
    const val TARGET_DATA_FILE_URI = "Android/data/" + TARGET_PACKAGE_NAME

    /**
     * 安装包目标包名
     */
    const val TARGET_PACKAGE_NAME_SAMSUNG = "com.zane.stardewvalleysamsung"

    /**
     * DLC下载路径
     */
    const val DLC_LIST_UPDATE_URL = "http://dl.zaneyork.cn/smapi/downloadable_content_list.json"

    /**
     * 软件发布页
     */
    const val RELEASE_URL = "https://github.com/ZaneYork/SMAPI-Android-Installer/releases"

    /**
     * 帮助内容下载路径
     */
    const val HELP_LIST_UPDATE_URL = "http://dl.zaneyork.cn/smapi/help_item_list.json"

    /**
     * AppCenter秘钥
     */
    const val APP_CENTER_SECRET = "cb44e94a-7b2f-431e-9ad9-48013ec8c208"
    const val RED_PACKET_CODE = "9188262"
    const val HIDDEN_FILE_PREFIX = "."
    const val URL_LENGTH_LIMIT = 4096

    /**
     * 有道翻译服务
     */
    const val TRANSLATE_SERVICE_URL_YOUDAO =
        "http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=%s"

    /**
     * Google翻译服务
     */
    const val TRANSLATE_SERVICE_URL_GOOGLE =
        "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=%s&q=%s"

    /**
     * 文本文件打开大小阈值
     */
    const val TEXT_FILE_OPEN_SIZE_LIMIT = 16 * 1024 * 1024

    /**
     * SMAPI版本
     */
    const val SMAPI_VERSION = "3.7.6"

    /**
     * Mono Android 10 起始版本号
     */
    const val MONO_10_VERSION_CODE = 148

    /**
     * 应用名称
     */
    @JvmField
    var PATCHED_APP_NAME: String? = null

    /**
     * Manifest中使用的路径分隔符
     */
    const val FILE_SEPARATOR = "/"

    /**
     * 平台
     */
    const val PLATFORM = "Android"

    /**
     * SMAPI更新服务
     */
    const val UPDATE_CHECK_SERVICE_URL = "https://smapi.io/api/v" + SMAPI_VERSION + "/mods"

    /**
     * 软件检查更新服务
     */
    const val SELF_UPDATE_CHECK_SERVICE_URL = "http://zaneyork.cn/dl/app_version.json"
}