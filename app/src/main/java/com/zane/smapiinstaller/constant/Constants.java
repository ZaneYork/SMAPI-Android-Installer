package com.zane.smapiinstaller.constant;

/**
 * 常量
 * @author Zane
 */
public class Constants {
    /**
     * Mod安装路径
     */
    public static String MOD_PATH = "StardewValley/Mods";
    /**
     * 日志路径
     */
    public static final String LOG_PATH = "StardewValley/ErrorLogs/SMAPI-latest.txt";

    /**
     * 配置文件路径
     */
    public static final String CONFIG_PATH = "StardewValley/smapi-internal/config.user.json";
    /**
     * 原始安装包名
     */
    public static final String ORIGIN_PACKAGE_NAME_GOOGLE =  "com.chucklefish.stardewvalley";
    /**
     * 安装包目标包名
     */
    public static final String TARGET_PACKAGE_NAME =  "com.zane.stardewvalley";
    /**
     * 安装包目标包名
     */
    public static final String TARGET_PACKAGE_NAME_SAMSUNG =  "com.zane.stardewvalleysamsung";
    /**
     * DLC下载路径
     */
    public static final String DLC_LIST_UPDATE_URL = "http://dl.zaneyork.cn/smapi/downloadable_content_list.json";
    /**
     * 软件发布页
     */
    public static final String RELEASE_URL = "https://github.com/ZaneYork/SMAPI-Android-Installer/releases";
    /**
     * 帮助内容下载路径
     */
    public static final String HELP_LIST_UPDATE_URL = "http://dl.zaneyork.cn/smapi/help_item_list.json";
    /**
     * AppCenter秘钥
     */
    public static final String APP_CENTER_SECRET = "cb44e94a-7b2f-431e-9ad9-48013ec8c208";

    public static final String RED_PACKET_CODE = "9188262";

    public static final String HIDDEN_FILE_PREFIX = ".";

    public static final int URL_LENGTH_LIMIT = 4096;

    /**
     * 有道翻译服务
     */
    public static final String TRANSLATE_SERVICE_URL_YOUDAO = "http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=%s";

    /**
     * Google翻译服务
     */
    public static final String TRANSLATE_SERVICE_URL_GOOGLE = "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=%s&q=%s";

    /**
     * 文本文件打开大小阈值
     */
    public static final int TEXT_FILE_OPEN_SIZE_LIMIT = 16 * 1024 * 1024;

    /**
     * SMAPI版本
     */
    public static final String SMAPI_VERSION = "3.7.6";

    /**
     * Mono Android 10 起始版本号
     */
    public static final int MONO_10_VERSION_CODE = 148;

    /**
     * 应用名称
     */
    public static String PATCHED_APP_NAME = null;

    /**
     * Manifest中使用的路径分隔符
     */
    public static final String FILE_SEPARATOR = "/";

    /**
     * 平台
     */
    public static final String PLATFORM = "Android";

    /**
     * SMAPI更新服务
     */
    public static final String UPDATE_CHECK_SERVICE_URL = "https://smapi.io/api/v" + SMAPI_VERSION + "/mods";

    /**
     * 软件检查更新服务
     */
    public static final String SELF_UPDATE_CHECK_SERVICE_URL = "http://zaneyork.cn/dl/app_version.json";
}
