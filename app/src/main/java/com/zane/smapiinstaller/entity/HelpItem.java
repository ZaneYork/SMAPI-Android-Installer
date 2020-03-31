package com.zane.smapiinstaller.entity;

import lombok.Data;

/**
 * 帮助信息
 * @author Zane
 */
@Data
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
}
