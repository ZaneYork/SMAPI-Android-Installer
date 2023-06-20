package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.List;
import java.util.Set;

/**
 * Mod信息
 * @author Zane
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
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

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public ModManifestEntry() {
    }

    /**
     * 存放位置
     */
    @SuppressWarnings("all")
    public String getAssetPath() {
        return this.assetPath;
    }

    /**
     * 名称
     */
    @SuppressWarnings("all")
    public String getName() {
        return this.Name;
    }

    /**
     * 唯一ID
     */
    @SuppressWarnings("all")
    public String getUniqueID() {
        return this.UniqueID;
    }

    /**
     * 版本
     */
    @SuppressWarnings("all")
    public String getVersion() {
        return this.Version;
    }

    /**
     * 描述
     */
    @SuppressWarnings("all")
    public String getDescription() {
        return this.Description;
    }

    /**
     * 依赖
     */
    @SuppressWarnings("all")
    public Set<ModManifestEntry> getDependencies() {
        return this.Dependencies;
    }

    /**
     * MOD更新源
     */
    @SuppressWarnings("all")
    public List<String> getUpdateKeys() {
        return this.UpdateKeys;
    }

    /**
     * 资源包类型
     */
    @SuppressWarnings("all")
    public ModManifestEntry getContentPackFor() {
        return this.ContentPackFor;
    }

    /**
     * 最小依赖版本
     */
    @SuppressWarnings("all")
    public String getMinimumVersion() {
        return this.MinimumVersion;
    }

    /**
     * 是否必须依赖
     */
    @SuppressWarnings("all")
    public Boolean getIsRequired() {
        return this.IsRequired;
    }

    /**
     * 是否清理安装
     */
    @SuppressWarnings("all")
    public Boolean getCleanInstall() {
        return this.CleanInstall;
    }

    /**
     * 原唯一ID列表
     */
    @SuppressWarnings("all")
    public List<String> getOriginUniqueId() {
        return this.OriginUniqueId;
    }

    /**
     * 翻译后的Description
     */
    @SuppressWarnings("all")
    public String getTranslatedDescription() {
        return this.translatedDescription;
    }

    /**
     * 文件修改日期
     */
    @SuppressWarnings("all")
    public Long getLastModified() {
        return this.lastModified;
    }

    /**
     * 存放位置
     */
    @SuppressWarnings("all")
    public void setAssetPath(final String assetPath) {
        this.assetPath = assetPath;
    }

    /**
     * 名称
     */
    @SuppressWarnings("all")
    public void setName(final String Name) {
        this.Name = Name;
    }

    /**
     * 唯一ID
     */
    @SuppressWarnings("all")
    public void setUniqueID(final String UniqueID) {
        this.UniqueID = UniqueID;
    }

    /**
     * 版本
     */
    @SuppressWarnings("all")
    public void setVersion(final String Version) {
        this.Version = Version;
    }

    /**
     * 描述
     */
    @SuppressWarnings("all")
    public void setDescription(final String Description) {
        this.Description = Description;
    }

    /**
     * 依赖
     */
    @SuppressWarnings("all")
    public void setDependencies(final Set<ModManifestEntry> Dependencies) {
        this.Dependencies = Dependencies;
    }

    /**
     * MOD更新源
     */
    @SuppressWarnings("all")
    public void setUpdateKeys(final List<String> UpdateKeys) {
        this.UpdateKeys = UpdateKeys;
    }

    /**
     * 资源包类型
     */
    @SuppressWarnings("all")
    public void setContentPackFor(final ModManifestEntry ContentPackFor) {
        this.ContentPackFor = ContentPackFor;
    }

    /**
     * 最小依赖版本
     */
    @SuppressWarnings("all")
    public void setMinimumVersion(final String MinimumVersion) {
        this.MinimumVersion = MinimumVersion;
    }

    /**
     * 是否必须依赖
     */
    @SuppressWarnings("all")
    public void setIsRequired(final Boolean IsRequired) {
        this.IsRequired = IsRequired;
    }

    /**
     * 是否清理安装
     */
    @SuppressWarnings("all")
    public void setCleanInstall(final Boolean CleanInstall) {
        this.CleanInstall = CleanInstall;
    }

    /**
     * 原唯一ID列表
     */
    @SuppressWarnings("all")
    public void setOriginUniqueId(final List<String> OriginUniqueId) {
        this.OriginUniqueId = OriginUniqueId;
    }

    /**
     * 翻译后的Description
     */
    @SuppressWarnings("all")
    public void setTranslatedDescription(final String translatedDescription) {
        this.translatedDescription = translatedDescription;
    }

    /**
     * 文件修改日期
     */
    @SuppressWarnings("all")
    public void setLastModified(final Long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ModManifestEntry)) return false;
        final ModManifestEntry other = (ModManifestEntry) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$IsRequired = this.getIsRequired();
        final Object other$IsRequired = other.getIsRequired();
        if (this$IsRequired == null ? other$IsRequired != null : !this$IsRequired.equals(other$IsRequired)) return false;
        final Object this$CleanInstall = this.getCleanInstall();
        final Object other$CleanInstall = other.getCleanInstall();
        if (this$CleanInstall == null ? other$CleanInstall != null : !this$CleanInstall.equals(other$CleanInstall)) return false;
        final Object this$assetPath = this.getAssetPath();
        final Object other$assetPath = other.getAssetPath();
        if (this$assetPath == null ? other$assetPath != null : !this$assetPath.equals(other$assetPath)) return false;
        final Object this$Name = this.getName();
        final Object other$Name = other.getName();
        if (this$Name == null ? other$Name != null : !this$Name.equals(other$Name)) return false;
        final Object this$UniqueID = this.getUniqueID();
        final Object other$UniqueID = other.getUniqueID();
        if (this$UniqueID == null ? other$UniqueID != null : !this$UniqueID.equals(other$UniqueID)) return false;
        final Object this$Version = this.getVersion();
        final Object other$Version = other.getVersion();
        if (this$Version == null ? other$Version != null : !this$Version.equals(other$Version)) return false;
        final Object this$Description = this.getDescription();
        final Object other$Description = other.getDescription();
        if (this$Description == null ? other$Description != null : !this$Description.equals(other$Description)) return false;
        final Object this$Dependencies = this.getDependencies();
        final Object other$Dependencies = other.getDependencies();
        if (this$Dependencies == null ? other$Dependencies != null : !this$Dependencies.equals(other$Dependencies)) return false;
        final Object this$UpdateKeys = this.getUpdateKeys();
        final Object other$UpdateKeys = other.getUpdateKeys();
        if (this$UpdateKeys == null ? other$UpdateKeys != null : !this$UpdateKeys.equals(other$UpdateKeys)) return false;
        final Object this$ContentPackFor = this.getContentPackFor();
        final Object other$ContentPackFor = other.getContentPackFor();
        if (this$ContentPackFor == null ? other$ContentPackFor != null : !this$ContentPackFor.equals(other$ContentPackFor)) return false;
        final Object this$MinimumVersion = this.getMinimumVersion();
        final Object other$MinimumVersion = other.getMinimumVersion();
        if (this$MinimumVersion == null ? other$MinimumVersion != null : !this$MinimumVersion.equals(other$MinimumVersion)) return false;
        final Object this$OriginUniqueId = this.getOriginUniqueId();
        final Object other$OriginUniqueId = other.getOriginUniqueId();
        if (this$OriginUniqueId == null ? other$OriginUniqueId != null : !this$OriginUniqueId.equals(other$OriginUniqueId)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof ModManifestEntry;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $IsRequired = this.getIsRequired();
        result = result * PRIME + ($IsRequired == null ? 43 : $IsRequired.hashCode());
        final Object $CleanInstall = this.getCleanInstall();
        result = result * PRIME + ($CleanInstall == null ? 43 : $CleanInstall.hashCode());
        final Object $assetPath = this.getAssetPath();
        result = result * PRIME + ($assetPath == null ? 43 : $assetPath.hashCode());
        final Object $Name = this.getName();
        result = result * PRIME + ($Name == null ? 43 : $Name.hashCode());
        final Object $UniqueID = this.getUniqueID();
        result = result * PRIME + ($UniqueID == null ? 43 : $UniqueID.hashCode());
        final Object $Version = this.getVersion();
        result = result * PRIME + ($Version == null ? 43 : $Version.hashCode());
        final Object $Description = this.getDescription();
        result = result * PRIME + ($Description == null ? 43 : $Description.hashCode());
        final Object $Dependencies = this.getDependencies();
        result = result * PRIME + ($Dependencies == null ? 43 : $Dependencies.hashCode());
        final Object $UpdateKeys = this.getUpdateKeys();
        result = result * PRIME + ($UpdateKeys == null ? 43 : $UpdateKeys.hashCode());
        final Object $ContentPackFor = this.getContentPackFor();
        result = result * PRIME + ($ContentPackFor == null ? 43 : $ContentPackFor.hashCode());
        final Object $MinimumVersion = this.getMinimumVersion();
        result = result * PRIME + ($MinimumVersion == null ? 43 : $MinimumVersion.hashCode());
        final Object $OriginUniqueId = this.getOriginUniqueId();
        result = result * PRIME + ($OriginUniqueId == null ? 43 : $OriginUniqueId.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "ModManifestEntry(assetPath=" + this.getAssetPath() + ", Name=" + this.getName() + ", UniqueID=" + this.getUniqueID() + ", Version=" + this.getVersion() + ", Description=" + this.getDescription() + ", Dependencies=" + this.getDependencies() + ", UpdateKeys=" + this.getUpdateKeys() + ", ContentPackFor=" + this.getContentPackFor() + ", MinimumVersion=" + this.getMinimumVersion() + ", IsRequired=" + this.getIsRequired() + ", CleanInstall=" + this.getCleanInstall() + ", OriginUniqueId=" + this.getOriginUniqueId() + ", translatedDescription=" + this.getTranslatedDescription() + ", lastModified=" + this.getLastModified() + ")";
    }
    //</editor-fold>
}
