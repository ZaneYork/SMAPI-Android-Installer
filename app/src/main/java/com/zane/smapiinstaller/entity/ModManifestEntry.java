package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * Mod信息
 * @author Zane
 */
@Data
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY, getterVisibility= JsonAutoDetect.Visibility.NONE)
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
     * MOD更新源
     */
    private List<String> UpdateKeys;
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

    /**
     * 是否清理安装
     */
    private Boolean CleanInstall;

    /**
     * 原唯一ID列表
     */
    private List<String> OriginUniqueId;

    /**
     * 翻译后的Description
     */
    private transient String translatedDescription;
    /**
     * 文件修改日期
     */
    private transient Long lastModified;
}
