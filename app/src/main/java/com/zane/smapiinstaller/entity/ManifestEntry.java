package com.zane.smapiinstaller.entity;

import lombok.Data;

/**
 * 文件映射关系
 * @author Zane
 */
@Data
public class ManifestEntry {
    /**
     * 目标路径
     */
    private String targetPath;
    /**
     * 资源路径
     */
    private String assetPath;
    /**
     * 压缩级别
     */
    private int compression;
    /**
     * 来源位置，0:安装器自带/1:从APK中抽取
     */
    private int origin;
    /**
     * 文件是否不属于兼容包中
     */
    private boolean external;
}
