package com.zane.smapiinstaller.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 文件映射关系
 * @author Zane
 */
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
    /**
     * 是否为XABA压缩格式
     */
    @JsonProperty("isXABA")
    private boolean isXABA;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public ManifestEntry() {
    }

    /**
     * 目标路径
     */
    @SuppressWarnings("all")
    public String getTargetPath() {
        return this.targetPath;
    }

    /**
     * 资源路径
     */
    @SuppressWarnings("all")
    public String getAssetPath() {
        return this.assetPath;
    }

    /**
     * 压缩级别
     */
    @SuppressWarnings("all")
    public int getCompression() {
        return this.compression;
    }

    /**
     * 来源位置，0:安装器自带/1:从APK中抽取
     */
    @SuppressWarnings("all")
    public int getOrigin() {
        return this.origin;
    }

    /**
     * 文件是否不属于兼容包中
     */
    @SuppressWarnings("all")
    public boolean isExternal() {
        return this.external;
    }

    /**
     * 补丁CRC
     */
    @SuppressWarnings("all")
    public String getPatchCrc() {
        return this.patchCrc;
    }

    /**
     * 补丁后CRC
     */
    @SuppressWarnings("all")
    public String getPatchedCrc() {
        return this.patchedCrc;
    }

    /**
     * 是否为高级模式补丁
     */
    @SuppressWarnings("all")
    public boolean isAdvanced() {
        return this.advanced;
    }

    /**
     * 是否为XALZ压缩格式
     */
    @SuppressWarnings("all")
    public boolean isXALZ() {
        return this.isXALZ;
    }

    /**
     * 是否为XABA压缩格式
     */
    @SuppressWarnings("all")
    public boolean isXABA() {
        return this.isXABA;
    }

    /**
     * 目标路径
     */
    @SuppressWarnings("all")
    public void setTargetPath(final String targetPath) {
        this.targetPath = targetPath;
    }

    /**
     * 资源路径
     */
    @SuppressWarnings("all")
    public void setAssetPath(final String assetPath) {
        this.assetPath = assetPath;
    }

    /**
     * 压缩级别
     */
    @SuppressWarnings("all")
    public void setCompression(final int compression) {
        this.compression = compression;
    }

    /**
     * 来源位置，0:安装器自带/1:从APK中抽取
     */
    @SuppressWarnings("all")
    public void setOrigin(final int origin) {
        this.origin = origin;
    }

    /**
     * 文件是否不属于兼容包中
     */
    @SuppressWarnings("all")
    public void setExternal(final boolean external) {
        this.external = external;
    }

    /**
     * 补丁CRC
     */
    @SuppressWarnings("all")
    public void setPatchCrc(final String patchCrc) {
        this.patchCrc = patchCrc;
    }

    /**
     * 补丁后CRC
     */
    @SuppressWarnings("all")
    public void setPatchedCrc(final String patchedCrc) {
        this.patchedCrc = patchedCrc;
    }

    /**
     * 是否为高级模式补丁
     */
    @SuppressWarnings("all")
    public void setAdvanced(final boolean advanced) {
        this.advanced = advanced;
    }

    /**
     * 是否为XALZ压缩格式
     */
    @JsonProperty("isXALZ")
    @SuppressWarnings("all")
    public void setXALZ(final boolean isXALZ) {
        this.isXALZ = isXALZ;
    }

    /**
     * 是否为XABA压缩格式
     */
    @JsonProperty("isXABA")
    @SuppressWarnings("all")
    public void setXABA(final boolean isXABA) {
        this.isXABA = isXABA;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ManifestEntry)) return false;
        final ManifestEntry other = (ManifestEntry) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getCompression() != other.getCompression()) return false;
        if (this.getOrigin() != other.getOrigin()) return false;
        if (this.isExternal() != other.isExternal()) return false;
        if (this.isAdvanced() != other.isAdvanced()) return false;
        if (this.isXALZ() != other.isXALZ()) return false;
        if (this.isXABA() != other.isXABA()) return false;
        final Object this$targetPath = this.getTargetPath();
        final Object other$targetPath = other.getTargetPath();
        if (this$targetPath == null ? other$targetPath != null : !this$targetPath.equals(other$targetPath)) return false;
        final Object this$assetPath = this.getAssetPath();
        final Object other$assetPath = other.getAssetPath();
        if (this$assetPath == null ? other$assetPath != null : !this$assetPath.equals(other$assetPath)) return false;
        final Object this$patchCrc = this.getPatchCrc();
        final Object other$patchCrc = other.getPatchCrc();
        if (this$patchCrc == null ? other$patchCrc != null : !this$patchCrc.equals(other$patchCrc)) return false;
        final Object this$patchedCrc = this.getPatchedCrc();
        final Object other$patchedCrc = other.getPatchedCrc();
        if (this$patchedCrc == null ? other$patchedCrc != null : !this$patchedCrc.equals(other$patchedCrc)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof ManifestEntry;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getCompression();
        result = result * PRIME + this.getOrigin();
        result = result * PRIME + (this.isExternal() ? 79 : 97);
        result = result * PRIME + (this.isAdvanced() ? 79 : 97);
        result = result * PRIME + (this.isXALZ() ? 79 : 97);
        result = result * PRIME + (this.isXABA() ? 79 : 97);
        final Object $targetPath = this.getTargetPath();
        result = result * PRIME + ($targetPath == null ? 43 : $targetPath.hashCode());
        final Object $assetPath = this.getAssetPath();
        result = result * PRIME + ($assetPath == null ? 43 : $assetPath.hashCode());
        final Object $patchCrc = this.getPatchCrc();
        result = result * PRIME + ($patchCrc == null ? 43 : $patchCrc.hashCode());
        final Object $patchedCrc = this.getPatchedCrc();
        result = result * PRIME + ($patchedCrc == null ? 43 : $patchedCrc.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "ManifestEntry(targetPath=" + this.getTargetPath() + ", assetPath=" + this.getAssetPath() + ", compression=" + this.getCompression() + ", origin=" + this.getOrigin() + ", external=" + this.isExternal() + ", patchCrc=" + this.getPatchCrc() + ", patchedCrc=" + this.getPatchedCrc() + ", advanced=" + this.isAdvanced() + ", isXALZ=" + this.isXALZ() + ", isXABA=" + this.isXABA() + ")";
    }
    //</editor-fold>
}
