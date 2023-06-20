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

/**
 * @author Zane
 */
public class ModUpdateCheckRequestDto {
    public ModUpdateCheckRequestDto(List<ModInfo> mods, SemanticVersion gameVersion) {
        this.mods = mods;
        this.gameVersion = gameVersion;
    }

    /**
     * 待检查MOD列表
     */
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
            } else {
                return;
            }
            i++;
            if (versionSections.size() > i) {
                MinorVersion = Integer.parseInt(versionSections.get(i));
            } else {
                return;
            }
            i++;
            // read optional patch version
            if (versionSections.size() > i) {
                PatchVersion = Integer.parseInt(versionSections.get(i));
            } else {
                return;
            }
            i++;
            // read optional non-standard platform release version
            try {
                if (versionSections.size() > i) {
                    PlatformRelease = Integer.parseInt(versionSections.get(i));
                } else {
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
            // read optional prerelease tag
            versionSections = Splitter.on("-").limit(2).splitToList(versionStr);
            if (versionSections.size() > 1) {
                PrereleaseTag = RegExUtils.removeFirst(versionSections.get(1), "\\+.*");
            } else {
                return;
            }
            // read optional build tag
            versionSections = Splitter.on("+").limit(2).splitToList(versionStr);
            if (versionSections.size() > 1) {
                BuildMetadata = versionSections.get(1);
            }
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public int getMajorVersion() {
            return this.MajorVersion;
        }

        @SuppressWarnings("all")
        public int getMinorVersion() {
            return this.MinorVersion;
        }

        @SuppressWarnings("all")
        public int getPatchVersion() {
            return this.PatchVersion;
        }

        @SuppressWarnings("all")
        public int getPlatformRelease() {
            return this.PlatformRelease;
        }

        @SuppressWarnings("all")
        public String getPrereleaseTag() {
            return this.PrereleaseTag;
        }

        @SuppressWarnings("all")
        public String getBuildMetadata() {
            return this.BuildMetadata;
        }

        @SuppressWarnings("all")
        public void setMajorVersion(final int MajorVersion) {
            this.MajorVersion = MajorVersion;
        }

        @SuppressWarnings("all")
        public void setMinorVersion(final int MinorVersion) {
            this.MinorVersion = MinorVersion;
        }

        @SuppressWarnings("all")
        public void setPatchVersion(final int PatchVersion) {
            this.PatchVersion = PatchVersion;
        }

        @SuppressWarnings("all")
        public void setPlatformRelease(final int PlatformRelease) {
            this.PlatformRelease = PlatformRelease;
        }

        @SuppressWarnings("all")
        public void setPrereleaseTag(final String PrereleaseTag) {
            this.PrereleaseTag = PrereleaseTag;
        }

        @SuppressWarnings("all")
        public void setBuildMetadata(final String BuildMetadata) {
            this.BuildMetadata = BuildMetadata;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ModUpdateCheckRequestDto.SemanticVersion)) return false;
            final ModUpdateCheckRequestDto.SemanticVersion other = (ModUpdateCheckRequestDto.SemanticVersion) o;
            if (!other.canEqual((Object) this)) return false;
            if (this.getMajorVersion() != other.getMajorVersion()) return false;
            if (this.getMinorVersion() != other.getMinorVersion()) return false;
            if (this.getPatchVersion() != other.getPatchVersion()) return false;
            if (this.getPlatformRelease() != other.getPlatformRelease()) return false;
            final Object this$PrereleaseTag = this.getPrereleaseTag();
            final Object other$PrereleaseTag = other.getPrereleaseTag();
            if (this$PrereleaseTag == null ? other$PrereleaseTag != null : !this$PrereleaseTag.equals(other$PrereleaseTag)) return false;
            final Object this$BuildMetadata = this.getBuildMetadata();
            final Object other$BuildMetadata = other.getBuildMetadata();
            if (this$BuildMetadata == null ? other$BuildMetadata != null : !this$BuildMetadata.equals(other$BuildMetadata)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof ModUpdateCheckRequestDto.SemanticVersion;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getMajorVersion();
            result = result * PRIME + this.getMinorVersion();
            result = result * PRIME + this.getPatchVersion();
            result = result * PRIME + this.getPlatformRelease();
            final Object $PrereleaseTag = this.getPrereleaseTag();
            result = result * PRIME + ($PrereleaseTag == null ? 43 : $PrereleaseTag.hashCode());
            final Object $BuildMetadata = this.getBuildMetadata();
            result = result * PRIME + ($BuildMetadata == null ? 43 : $BuildMetadata.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "ModUpdateCheckRequestDto.SemanticVersion(MajorVersion=" + this.getMajorVersion() + ", MinorVersion=" + this.getMinorVersion() + ", PatchVersion=" + this.getPatchVersion() + ", PlatformRelease=" + this.getPlatformRelease() + ", PrereleaseTag=" + this.getPrereleaseTag() + ", BuildMetadata=" + this.getBuildMetadata() + ")";
        }
        //</editor-fold>
    }


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

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public ModInfo() {
        }

        @SuppressWarnings("all")
        public String getId() {
            return this.id;
        }

        @SuppressWarnings("all")
        public List<String> getUpdateKeys() {
            return this.updateKeys;
        }

        @SuppressWarnings("all")
        public SemanticVersion getInstalledVersion() {
            return this.installedVersion;
        }

        @SuppressWarnings("all")
        public void setId(final String id) {
            this.id = id;
        }

        @SuppressWarnings("all")
        public void setUpdateKeys(final List<String> updateKeys) {
            this.updateKeys = updateKeys;
        }

        @SuppressWarnings("all")
        public void setInstalledVersion(final SemanticVersion installedVersion) {
            this.installedVersion = installedVersion;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "ModUpdateCheckRequestDto.ModInfo(id=" + this.getId() + ", updateKeys=" + this.getUpdateKeys() + ", installedVersion=" + this.getInstalledVersion() + ")";
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ModUpdateCheckRequestDto.ModInfo)) return false;
            final ModUpdateCheckRequestDto.ModInfo other = (ModUpdateCheckRequestDto.ModInfo) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$id = this.getId();
            final Object other$id = other.getId();
            if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof ModUpdateCheckRequestDto.ModInfo;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $id = this.getId();
            result = result * PRIME + ($id == null ? 43 : $id.hashCode());
            return result;
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    /**
     * 待检查MOD列表
     */
    @SuppressWarnings("all")
    public List<ModInfo> getMods() {
        return this.mods;
    }

    /**
     * SMAPI版本
     */
    @SuppressWarnings("all")
    public SemanticVersion getApiVersion() {
        return this.apiVersion;
    }

    /**
     * 游戏版本
     */
    @SuppressWarnings("all")
    public SemanticVersion getGameVersion() {
        return this.gameVersion;
    }

    /**
     * 平台版本
     */
    @SuppressWarnings("all")
    public String getPlatform() {
        return this.platform;
    }

    /**
     * 是否拉取MOD详情
     */
    @SuppressWarnings("all")
    public boolean isIncludeExtendedMetadata() {
        return this.includeExtendedMetadata;
    }

    /**
     * 待检查MOD列表
     */
    @SuppressWarnings("all")
    public void setMods(final List<ModInfo> mods) {
        if (mods == null) {
            throw new NullPointerException("mods is marked non-null but is null");
        }
        this.mods = mods;
    }

    /**
     * SMAPI版本
     */
    @SuppressWarnings("all")
    public void setApiVersion(final SemanticVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * 游戏版本
     */
    @SuppressWarnings("all")
    public void setGameVersion(final SemanticVersion gameVersion) {
        this.gameVersion = gameVersion;
    }

    /**
     * 平台版本
     */
    @SuppressWarnings("all")
    public void setPlatform(final String platform) {
        this.platform = platform;
    }

    /**
     * 是否拉取MOD详情
     */
    @SuppressWarnings("all")
    public void setIncludeExtendedMetadata(final boolean includeExtendedMetadata) {
        this.includeExtendedMetadata = includeExtendedMetadata;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ModUpdateCheckRequestDto)) return false;
        final ModUpdateCheckRequestDto other = (ModUpdateCheckRequestDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isIncludeExtendedMetadata() != other.isIncludeExtendedMetadata()) return false;
        final Object this$mods = this.getMods();
        final Object other$mods = other.getMods();
        if (this$mods == null ? other$mods != null : !this$mods.equals(other$mods)) return false;
        final Object this$apiVersion = this.getApiVersion();
        final Object other$apiVersion = other.getApiVersion();
        if (this$apiVersion == null ? other$apiVersion != null : !this$apiVersion.equals(other$apiVersion)) return false;
        final Object this$gameVersion = this.getGameVersion();
        final Object other$gameVersion = other.getGameVersion();
        if (this$gameVersion == null ? other$gameVersion != null : !this$gameVersion.equals(other$gameVersion)) return false;
        final Object this$platform = this.getPlatform();
        final Object other$platform = other.getPlatform();
        if (this$platform == null ? other$platform != null : !this$platform.equals(other$platform)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof ModUpdateCheckRequestDto;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isIncludeExtendedMetadata() ? 79 : 97);
        final Object $mods = this.getMods();
        result = result * PRIME + ($mods == null ? 43 : $mods.hashCode());
        final Object $apiVersion = this.getApiVersion();
        result = result * PRIME + ($apiVersion == null ? 43 : $apiVersion.hashCode());
        final Object $gameVersion = this.getGameVersion();
        result = result * PRIME + ($gameVersion == null ? 43 : $gameVersion.hashCode());
        final Object $platform = this.getPlatform();
        result = result * PRIME + ($platform == null ? 43 : $platform.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "ModUpdateCheckRequestDto(mods=" + this.getMods() + ", apiVersion=" + this.getApiVersion() + ", gameVersion=" + this.getGameVersion() + ", platform=" + this.getPlatform() + ", includeExtendedMetadata=" + this.isIncludeExtendedMetadata() + ")";
    }

    @SuppressWarnings("all")
    public ModUpdateCheckRequestDto(final List<ModInfo> mods) {
        if (mods == null) {
            throw new NullPointerException("mods is marked non-null but is null");
        }
        this.mods = mods;
    }
    //</editor-fold>
}
