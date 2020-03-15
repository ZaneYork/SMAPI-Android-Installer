package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * SMAPI的配置
 */
@Data
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY, getterVisibility= JsonAutoDetect.Visibility.NONE)
public class FrameworkConfig {
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
}
