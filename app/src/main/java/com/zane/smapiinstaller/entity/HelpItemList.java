package com.zane.smapiinstaller.entity;

import java.util.List;

/**
 * 帮助内容列表
 * @author Zane
 */
public class HelpItemList extends UpdatableList {
    /**
     * 列表
     */
    private List<HelpItem> items;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public HelpItemList() {
    }

    /**
     * 列表
     */
    @SuppressWarnings("all")
    public List<HelpItem> getItems() {
        return this.items;
    }

    /**
     * 列表
     */
    @SuppressWarnings("all")
    public void setItems(final List<HelpItem> items) {
        this.items = items;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "HelpItemList(items=" + this.getItems() + ")";
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof HelpItemList)) return false;
        final HelpItemList other = (HelpItemList) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$items = this.getItems();
        final Object other$items = other.getItems();
        if (this$items == null ? other$items != null : !this$items.equals(other$items)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof HelpItemList;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $items = this.getItems();
        result = result * PRIME + ($items == null ? 43 : $items.hashCode());
        return result;
    }
    //</editor-fold>
}
