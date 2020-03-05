package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;

@Data
public class DownloadableContentList {
    private int version;
    List<DownloadableContent> contents;
}
