package com.zane.smapiinstaller.dto;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.ModManifestEntry;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Zane
 */
@Data
@RequiredArgsConstructor
public class ModUpdateCheckRequestDto {

    public ModUpdateCheckRequestDto(List<ModInfo> mods, SemanticVersion gameVersion) {
        this.mods = mods;
        this.gameVersion = gameVersion;
    }

    /**
     * 待检查MOD列表
     */
    @NonNull
    private List<ModInfo> mods;
    /**
     * SMAPI版本
     */
    private SemanticVersion apiVersion = new SemanticVersion(Constants.SMAPI_VERSION);
    /**
     * 游戏版本
     */
    private SemanticVersion gameVersion;
    /**
     * 平台版本
     */
    private String platform = Constants.PLATFORM;
    /**
     * 是否拉取MOD详情
     */
    private boolean includeExtendedMetadata = false;

    @Data
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SemanticVersion {
        private int MajorVersion;
        private int MinorVersion;
        private int PatchVersion;
        private int PlatformRelease;
        private String PrereleaseTag;
        private String BuildMetadata;

        public SemanticVersion(String versionStr) {
            // init
            MajorVersion = 0;
            MinorVersion = 0;
            PatchVersion = 0;
            PlatformRelease = 0;
            PrereleaseTag = null;
            BuildMetadata = null;
            // normalize
            versionStr = StringUtils.trim(versionStr);
            if (StringUtils.isBlank(versionStr)) {
                return;
            }
            List<String> versionSections = Splitter.on(CharMatcher.anyOf(".-+")).splitToList(versionStr);
            // read major/minor version
            int i = 0;
            if (versionSections.size() > i) {
                MajorVersion = Integer.parseInt(versionSections.get(i));
            }
            else {
                return;
            }
            i++;
            if (versionSections.size() > i) {
                MinorVersion = Integer.parseInt(versionSections.get(i));
            }
            else {
                return;
            }
            i++;
            // read optional patch version
            if (versionSections.size() > i) {
                PatchVersion = Integer.parseInt(versionSections.get(i));
            }
            else {
                return;
            }
            i++;
            // read optional non-standard platform release version
            try {
                if (versionSections.size() > i) {
                    PlatformRelease = Integer.parseInt(versionSections.get(i));
                }
                else {
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
            // read optional prerelease tag
            versionSections = Splitter.on("-").limit(2).splitToList(versionStr);
            if (versionSections.size() > 1) {
                PrereleaseTag = RegExUtils.removeFirst(versionSections.get(1), "\\+.*");
            }
            else {
                return;
            }
            // read optional build tag
            versionSections = Splitter.on("+").limit(2).splitToList(versionStr);
            if (versionSections.size() > 1) {
                BuildMetadata = versionSections.get(1);
            }
        }
    }

    @Data
    @EqualsAndHashCode(of = "id")
    public static class ModInfo {
        private String id;
        private List<String> updateKeys;
        private SemanticVersion installedVersion;

        public static ModInfo fromModManifestEntry(ModManifestEntry mod) {
            ModInfo modInfo = new ModInfo();
            modInfo.setId(mod.getUniqueID());
            try {
                modInfo.setInstalledVersion(new SemanticVersion(mod.getVersion()));
            } catch (Exception e) {
                Log.d("", "", e);
            }
            modInfo.setUpdateKeys(mod.getUpdateKeys());
            return modInfo;
        }
    }
}
