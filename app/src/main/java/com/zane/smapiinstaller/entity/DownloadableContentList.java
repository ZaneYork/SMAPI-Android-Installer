package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 可下载内容列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DownloadableContentList extends UpdatableList {
    /**
     * 列表
     */
    List<DownloadableContent> contents;
}
