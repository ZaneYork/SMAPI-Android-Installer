package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;

@Data
public class ApkFilesManifest {
    private long minBuildCode;
    private Long maxBuildCode;
    private String basePath;
    private List<ManifestEntry> manifestEntries;
}
