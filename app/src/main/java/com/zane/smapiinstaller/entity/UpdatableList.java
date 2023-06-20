package com.zane.smapiinstaller.entity;

/**
 * 可更新列表
 * @author Zane
 */
public class UpdatableList {
    /**
     * 列表版本
     */
    private int version;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public UpdatableList() {
    }

    /**
     * 列表版本
     */
    @SuppressWarnings("all")
    public int getVersion() {
        return this.version;
    }

    /**
     * 列表版本
     */
    @SuppressWarnings("all")
    public void setVersion(final int version) {
        this.version = version;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UpdatableList)) return false;
        final UpdatableList other = (UpdatableList) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getVersion() != other.getVersion()) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof UpdatableList;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getVersion();
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "UpdatableList(version=" + this.getVersion() + ")";
    }
    //</editor-fold>
}
