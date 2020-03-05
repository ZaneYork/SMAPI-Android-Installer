package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;

@Data
public class ApkFilesManifest {
    private Long minBuildCode;
    private Long maxBuildCode;
    private List<ManifestEntry> manifestEntries;
}
