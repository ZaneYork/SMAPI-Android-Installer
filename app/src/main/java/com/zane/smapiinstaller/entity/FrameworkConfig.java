package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * SMAPI的配置
 * @author Zane
 */
@Data
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY, getterVisibility= JsonAutoDetect.Visibility.NONE)
public class FrameworkConfig {
    @JsonIgnore
    private transient boolean initial;
    /**
     * 详细日志
     */
    @JsonProperty("VerboseLogging")
    private boolean VerboseLogging = false;
    /**
     * 检查更新
     */
    @JsonProperty("CheckForUpdates")
    private boolean CheckForUpdates = true;
    /**
     * 开发者模式
     */
    @JsonProperty("DeveloperMode")
    private boolean DeveloperMode = false;

    /**
     * 禁用MonoMod
     */
    @JsonProperty("DisableMonoMod")
    private boolean DisableMonoMod = false;

    /**
     * 是否启用多线程重写
     */
    @JsonProperty("RewriteInParallel")
    private boolean RewriteInParallel = false;

    /**
     * 最大日志大小
     */
    @JsonProperty("MaxLogSize")
    private int MaxLogSize = Integer.MAX_VALUE;

    /**
     * Mod存放位置
     */
    @JsonProperty("ModsPath")
    private String ModsPath = "StardewValley/Mods";
}
