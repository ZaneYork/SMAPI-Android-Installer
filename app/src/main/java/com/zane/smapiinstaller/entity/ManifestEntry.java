package com.zane.smapiinstaller.entity;

import lombok.Data;

@Data
public class ManifestEntry {
    private String targetPath;
    private String assetPath;
    private int compression;
    private int origin;
}
