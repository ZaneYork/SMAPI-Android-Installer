package com.zane.smapiinstaller.entity;

import java.util.List;
import java.util.Set;

/**
 * SMAPI所需处理的文件清单
 * @author Zane
 */
public class ApkFilesManifest {
    /**
     * 最小兼容版本，包含
     */
    private long minBuildCode;
    /**
     * 最大兼容版本，包含
     */
    private Long maxBuildCode;
    /**
     * 兼容包基础文件路径
     */
    private String basePath;
    private Set<String> targetPackageName;
    /**
     * 文件清单
     */
    private List<ManifestEntry> manifestEntries;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public ApkFilesManifest() {
    }

    /**
     * 最小兼容版本，包含
     */
    @SuppressWarnings("all")
    public long getMinBuildCode() {
        return this.minBuildCode;
    }

    /**
     * 最大兼容版本，包含
     */
    @SuppressWarnings("all")
    public Long getMaxBuildCode() {
        return this.maxBuildCode;
    }

    /**
     * 兼容包基础文件路径
     */
    @SuppressWarnings("all")
    public String getBasePath() {
        return this.basePath;
    }

    @SuppressWarnings("all")
    public Set<String> getTargetPackageName() {
        return this.targetPackageName;
    }

    /**
     * 文件清单
     */
    @SuppressWarnings("all")
    public List<ManifestEntry> getManifestEntries() {
        return this.manifestEntries;
    }

    /**
     * 最小兼容版本，包含
     */
    @SuppressWarnings("all")
    public void setMinBuildCode(final long minBuildCode) {
        this.minBuildCode = minBuildCode;
    }

    /**
     * 最大兼容版本，包含
     */
    @SuppressWarnings("all")
    public void setMaxBuildCode(final Long maxBuildCode) {
        this.maxBuildCode = maxBuildCode;
    }

    /**
     * 兼容包基础文件路径
     */
    @SuppressWarnings("all")
    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    @SuppressWarnings("all")
    public void setTargetPackageName(final Set<String> targetPackageName) {
        this.targetPackageName = targetPackageName;
    }

    /**
     * 文件清单
     */
    @SuppressWarnings("all")
    public void setManifestEntries(final List<ManifestEntry> manifestEntries) {
        this.manifestEntries = manifestEntries;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ApkFilesManifest)) return false;
        final ApkFilesManifest other = (ApkFilesManifest) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getMinBuildCode() != other.getMinBuildCode()) return false;
        final Object this$maxBuildCode = this.getMaxBuildCode();
        final Object other$maxBuildCode = other.getMaxBuildCode();
        if (this$maxBuildCode == null ? other$maxBuildCode != null : !this$maxBuildCode.equals(other$maxBuildCode)) return false;
        final Object this$basePath = this.getBasePath();
        final Object other$basePath = other.getBasePath();
        if (this$basePath == null ? other$basePath != null : !this$basePath.equals(other$basePath)) return false;
        final Object this$targetPackageName = this.getTargetPackageName();
        final Object other$targetPackageName = other.getTargetPackageName();
        if (this$targetPackageName == null ? other$targetPackageName != null : !this$targetPackageName.equals(other$targetPackageName)) return false;
        final Object this$manifestEntries = this.getManifestEntries();
        final Object other$manifestEntries = other.getManifestEntries();
        if (this$manifestEntries == null ? other$manifestEntries != null : !this$manifestEntries.equals(other$manifestEntries)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof ApkFilesManifest;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $minBuildCode = this.getMinBuildCode();
        result = result * PRIME + (int) ($minBuildCode >>> 32 ^ $minBuildCode);
        final Object $maxBuildCode = this.getMaxBuildCode();
        result = result * PRIME + ($maxBuildCode == null ? 43 : $maxBuildCode.hashCode());
        final Object $basePath = this.getBasePath();
        result = result * PRIME + ($basePath == null ? 43 : $basePath.hashCode());
        final Object $targetPackageName = this.getTargetPackageName();
        result = result * PRIME + ($targetPackageName == null ? 43 : $targetPackageName.hashCode());
        final Object $manifestEntries = this.getManifestEntries();
        result = result * PRIME + ($manifestEntries == null ? 43 : $manifestEntries.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "ApkFilesManifest(minBuildCode=" + this.getMinBuildCode() + ", maxBuildCode=" + this.getMaxBuildCode() + ", basePath=" + this.getBasePath() + ", targetPackageName=" + this.getTargetPackageName() + ", manifestEntries=" + this.getManifestEntries() + ")";
    }
    //</editor-fold>
}
