package com.zane.smapiinstaller.entity;

/**
 * 可下载内容包
 * @author Zane
 */
public class DownloadableContent {
    /**
     * 类型，COMPAT:兼容包/LOCALE:语言包
     */
    private String type;
    /**
     * 名称
     */
    private String name;
    /**
     * 资源存放位置
     */
    private String assetPath;
    /**
     * 下载位置
     */
    private String url;
    /**
     * 描述
     */
    private String description;
    /**
     * 文件SHA3-256校验码
     */
    private String hash;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public DownloadableContent() {
    }

    /**
     * 类型，COMPAT:兼容包/LOCALE:语言包
     */
    @SuppressWarnings("all")
    public String getType() {
        return this.type;
    }

    /**
     * 名称
     */
    @SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    /**
     * 资源存放位置
     */
    @SuppressWarnings("all")
    public String getAssetPath() {
        return this.assetPath;
    }

    /**
     * 下载位置
     */
    @SuppressWarnings("all")
    public String getUrl() {
        return this.url;
    }

    /**
     * 描述
     */
    @SuppressWarnings("all")
    public String getDescription() {
        return this.description;
    }

    /**
     * 文件SHA3-256校验码
     */
    @SuppressWarnings("all")
    public String getHash() {
        return this.hash;
    }

    /**
     * 类型，COMPAT:兼容包/LOCALE:语言包
     */
    @SuppressWarnings("all")
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * 名称
     */
    @SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * 资源存放位置
     */
    @SuppressWarnings("all")
    public void setAssetPath(final String assetPath) {
        this.assetPath = assetPath;
    }

    /**
     * 下载位置
     */
    @SuppressWarnings("all")
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * 描述
     */
    @SuppressWarnings("all")
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * 文件SHA3-256校验码
     */
    @SuppressWarnings("all")
    public void setHash(final String hash) {
        this.hash = hash;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DownloadableContent)) return false;
        final DownloadableContent other = (DownloadableContent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$assetPath = this.getAssetPath();
        final Object other$assetPath = other.getAssetPath();
        if (this$assetPath == null ? other$assetPath != null : !this$assetPath.equals(other$assetPath)) return false;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        final Object this$hash = this.getHash();
        final Object other$hash = other.getHash();
        if (this$hash == null ? other$hash != null : !this$hash.equals(other$hash)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof DownloadableContent;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $assetPath = this.getAssetPath();
        result = result * PRIME + ($assetPath == null ? 43 : $assetPath.hashCode());
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $hash = this.getHash();
        result = result * PRIME + ($hash == null ? 43 : $hash.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "DownloadableContent(type=" + this.getType() + ", name=" + this.getName() + ", assetPath=" + this.getAssetPath() + ", url=" + this.getUrl() + ", description=" + this.getDescription() + ", hash=" + this.getHash() + ")";
    }
    //</editor-fold>
}
