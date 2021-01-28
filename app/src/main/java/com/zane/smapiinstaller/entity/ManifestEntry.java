package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    /**
     * 补丁CRC
     */
    private String patchCrc;

    /**
     * 补丁后CRC
     */
    private String patchedCrc;

    /**
     * 是否为高级模式补丁
     */
    private boolean advanced;
    /**
     * 是否为XALZ压缩格式
     */
    @JsonProperty("isXALZ")
    private boolean isXALZ;
}
