package com.zane.smapiinstaller.entity;

import lombok.Data;

/**
 * 可下载内容包
 */
@Data
public class DownloadableContent {
    /**
     * 类型，COMPAT:兼容包/LOCALE:语言包
     */
    private String type;
    /**
     * 名称
     */
    private String name;
    /**
     * 资源存放位置
     */
    private String assetPath;
    /**
     * 下载位置
     */
    private String url;
    /**
     * 描述
     */
    private String description;
    /**
     * 文件SHA3-256校验码
     */
    private String hash;
}
