package com.zane.smapiinstaller.entity;

import java.util.Set;

import lombok.Data;

/**
 * Mod信息
 */
@Data
public class ModManifestEntry {
    /**
     * 存放位置
     */
    private String assetPath;
    /**
     * 名称
     */
    private String Name;
    /**
     * 唯一ID
     */
    private String UniqueID;
    /**
     * 版本
     */
    private String Version;
    /**
     * 描述
     */
    private String Description;
    /**
     * 依赖
     */
    private Set<ModManifestEntry> Dependencies;
    /**
     * 资源包类型
     */
    private ModManifestEntry ContentPackFor;

    /**
     * 最小依赖版本
     */
    private String MinimumVersion;
    /**
     * 是否必须依赖
     */
    private Boolean IsRequired;

    /*
     * 翻译后的Description
     */
    private transient String translatedDescription;
}
