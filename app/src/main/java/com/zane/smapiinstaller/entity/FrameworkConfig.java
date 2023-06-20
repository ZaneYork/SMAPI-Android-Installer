package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SMAPI的配置
 * @author Zane
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
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
     * 是否启用重写丢失的引用点
     */
    @JsonProperty("RewriteMissing")
    private boolean RewriteMissing = false;
    /**
     * Mod存放位置
     */
    @JsonProperty("ModsPath")
    private String ModsPath = "StardewValley/Mods";

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public FrameworkConfig() {
    }

    @SuppressWarnings("all")
    public boolean isInitial() {
        return this.initial;
    }

    /**
     * 详细日志
     */
    @SuppressWarnings("all")
    public boolean isVerboseLogging() {
        return this.VerboseLogging;
    }

    /**
     * 检查更新
     */
    @SuppressWarnings("all")
    public boolean isCheckForUpdates() {
        return this.CheckForUpdates;
    }

    /**
     * 开发者模式
     */
    @SuppressWarnings("all")
    public boolean isDeveloperMode() {
        return this.DeveloperMode;
    }

    /**
     * 禁用MonoMod
     */
    @SuppressWarnings("all")
    public boolean isDisableMonoMod() {
        return this.DisableMonoMod;
    }

    /**
     * 是否启用重写丢失的引用点
     */
    @SuppressWarnings("all")
    public boolean isRewriteMissing() {
        return this.RewriteMissing;
    }

    /**
     * Mod存放位置
     */
    @SuppressWarnings("all")
    public String getModsPath() {
        return this.ModsPath;
    }

    @JsonIgnore
    @SuppressWarnings("all")
    public void setInitial(final boolean initial) {
        this.initial = initial;
    }

    /**
     * 详细日志
     */
    @JsonProperty("VerboseLogging")
    @SuppressWarnings("all")
    public void setVerboseLogging(final boolean VerboseLogging) {
        this.VerboseLogging = VerboseLogging;
    }

    /**
     * 检查更新
     */
    @JsonProperty("CheckForUpdates")
    @SuppressWarnings("all")
    public void setCheckForUpdates(final boolean CheckForUpdates) {
        this.CheckForUpdates = CheckForUpdates;
    }

    /**
     * 开发者模式
     */
    @JsonProperty("DeveloperMode")
    @SuppressWarnings("all")
    public void setDeveloperMode(final boolean DeveloperMode) {
        this.DeveloperMode = DeveloperMode;
    }

    /**
     * 禁用MonoMod
     */
    @JsonProperty("DisableMonoMod")
    @SuppressWarnings("all")
    public void setDisableMonoMod(final boolean DisableMonoMod) {
        this.DisableMonoMod = DisableMonoMod;
    }

    /**
     * 是否启用重写丢失的引用点
     */
    @JsonProperty("RewriteMissing")
    @SuppressWarnings("all")
    public void setRewriteMissing(final boolean RewriteMissing) {
        this.RewriteMissing = RewriteMissing;
    }

    /**
     * Mod存放位置
     */
    @JsonProperty("ModsPath")
    @SuppressWarnings("all")
    public void setModsPath(final String ModsPath) {
        this.ModsPath = ModsPath;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FrameworkConfig)) return false;
        final FrameworkConfig other = (FrameworkConfig) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isVerboseLogging() != other.isVerboseLogging()) return false;
        if (this.isCheckForUpdates() != other.isCheckForUpdates()) return false;
        if (this.isDeveloperMode() != other.isDeveloperMode()) return false;
        if (this.isDisableMonoMod() != other.isDisableMonoMod()) return false;
        if (this.isRewriteMissing() != other.isRewriteMissing()) return false;
        final Object this$ModsPath = this.getModsPath();
        final Object other$ModsPath = other.getModsPath();
        if (this$ModsPath == null ? other$ModsPath != null : !this$ModsPath.equals(other$ModsPath)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof FrameworkConfig;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isVerboseLogging() ? 79 : 97);
        result = result * PRIME + (this.isCheckForUpdates() ? 79 : 97);
        result = result * PRIME + (this.isDeveloperMode() ? 79 : 97);
        result = result * PRIME + (this.isDisableMonoMod() ? 79 : 97);
        result = result * PRIME + (this.isRewriteMissing() ? 79 : 97);
        final Object $ModsPath = this.getModsPath();
        result = result * PRIME + ($ModsPath == null ? 43 : $ModsPath.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "FrameworkConfig(initial=" + this.isInitial() + ", VerboseLogging=" + this.isVerboseLogging() + ", CheckForUpdates=" + this.isCheckForUpdates() + ", DeveloperMode=" + this.isDeveloperMode() + ", DisableMonoMod=" + this.isDisableMonoMod() + ", RewriteMissing=" + this.isRewriteMissing() + ", ModsPath=" + this.getModsPath() + ")";
    }
    //</editor-fold>
}
