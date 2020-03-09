package com.zane.smapiinstaller.entity;

import java.util.Set;

import lombok.Data;

@Data
public class ModManifestEntry {
    private String assetPath;
    private String Name;
    private String UniqueID;
    private String Version;
    private String Description;
    private Set<ModManifestEntry> Dependencies;
    private ModManifestEntry ContentPackFor;
}
