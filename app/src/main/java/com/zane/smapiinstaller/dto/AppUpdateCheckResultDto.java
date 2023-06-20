package com.zane.smapiinstaller.dto;

/**
 * @author Zane
 */
public class AppUpdateCheckResultDto {
    /**
     * 版本号
     */
    private long versionCode;
    /**
     * 版本名称
     */
    private String versionName;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public AppUpdateCheckResultDto() {
    }

    /**
     * 版本号
     */
    @SuppressWarnings("all")
    public long getVersionCode() {
        return this.versionCode;
    }

    /**
     * 版本名称
     */
    @SuppressWarnings("all")
    public String getVersionName() {
        return this.versionName;
    }

    /**
     * 版本号
     */
    @SuppressWarnings("all")
    public void setVersionCode(final long versionCode) {
        this.versionCode = versionCode;
    }

    /**
     * 版本名称
     */
    @SuppressWarnings("all")
    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AppUpdateCheckResultDto)) return false;
        final AppUpdateCheckResultDto other = (AppUpdateCheckResultDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getVersionCode() != other.getVersionCode()) return false;
        final Object this$versionName = this.getVersionName();
        final Object other$versionName = other.getVersionName();
        if (this$versionName == null ? other$versionName != null : !this$versionName.equals(other$versionName)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof AppUpdateCheckResultDto;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $versionCode = this.getVersionCode();
        result = result * PRIME + (int) ($versionCode >>> 32 ^ $versionCode);
        final Object $versionName = this.getVersionName();
        result = result * PRIME + ($versionName == null ? 43 : $versionName.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "AppUpdateCheckResultDto(versionCode=" + this.getVersionCode() + ", versionName=" + this.getVersionName() + ")";
    }
    //</editor-fold>
}
