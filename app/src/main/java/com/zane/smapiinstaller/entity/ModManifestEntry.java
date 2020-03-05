package com.zane.smapiinstaller.entity;

import lombok.Data;

@Data
public class ModManifestEntry {
    private String assetPath;
    private String Name;
    private String UniqueID;
    private String Description;
    private ModManifestEntry ContentPackFor;
}
