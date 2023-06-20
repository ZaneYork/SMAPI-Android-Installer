package com.zane.smapiinstaller.entity;

import java.util.List;

/**
 * 可下载内容列表
 * @author Zane
 */
public class DownloadableContentList extends UpdatableList {
    /**
     * 列表
     */
    List<DownloadableContent> contents;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public DownloadableContentList() {
    }

    /**
     * 列表
     */
    @SuppressWarnings("all")
    public List<DownloadableContent> getContents() {
        return this.contents;
    }

    /**
     * 列表
     */
    @SuppressWarnings("all")
    public void setContents(final List<DownloadableContent> contents) {
        this.contents = contents;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "DownloadableContentList(contents=" + this.getContents() + ")";
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DownloadableContentList)) return false;
        final DownloadableContentList other = (DownloadableContentList) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$contents = this.getContents();
        final Object other$contents = other.getContents();
        if (this$contents == null ? other$contents != null : !this$contents.equals(other$contents)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof DownloadableContentList;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $contents = this.getContents();
        result = result * PRIME + ($contents == null ? 43 : $contents.hashCode());
        return result;
    }
    //</editor-fold>
}
