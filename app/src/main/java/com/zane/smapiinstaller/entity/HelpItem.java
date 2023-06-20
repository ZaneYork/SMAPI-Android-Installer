package com.zane.smapiinstaller.entity;

/**
 * 帮助信息
 * @author Zane
 */
public class HelpItem {
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 作者
     */
    private String author;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public HelpItem() {
    }

    /**
     * 标题
     */
    @SuppressWarnings("all")
    public String getTitle() {
        return this.title;
    }

    /**
     * 内容
     */
    @SuppressWarnings("all")
    public String getContent() {
        return this.content;
    }

    /**
     * 作者
     */
    @SuppressWarnings("all")
    public String getAuthor() {
        return this.author;
    }

    /**
     * 标题
     */
    @SuppressWarnings("all")
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * 内容
     */
    @SuppressWarnings("all")
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * 作者
     */
    @SuppressWarnings("all")
    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof HelpItem)) return false;
        final HelpItem other = (HelpItem) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        if (this$author == null ? other$author != null : !this$author.equals(other$author)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof HelpItem;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "HelpItem(title=" + this.getTitle() + ", content=" + this.getContent() + ", author=" + this.getAuthor() + ")";
    }
    //</editor-fold>
}
