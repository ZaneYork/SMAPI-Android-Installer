package com.zane.smapiinstaller.entity;

import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * SMAPI所需处理的文件清单
 */
@Data
public class ApkFilesManifest {
    /**
     * 最小兼容版本，包含
     */
    private long minBuildCode;
    /**
     * 最大兼容版本，包含
     */
    private Long maxBuildCode;
    /**
     * 兼容包基础文件路径
     */
    private String basePath;

    private Set<String> targetPackageName;
    /**
     * 文件清单
     */
    private List<ManifestEntry> manifestEntries;
}
