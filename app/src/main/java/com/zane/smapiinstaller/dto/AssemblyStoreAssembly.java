package com.zane.smapiinstaller.dto;

public class AssemblyStoreAssembly {
    private Integer dataOffset;
    private Integer dataSize;
    private Integer debugDataOffset;
    private Integer debugDataSize;
    private Integer configDataOffset;
    private Integer configDataSize;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public AssemblyStoreAssembly() {
    }

    @SuppressWarnings("all")
    public Integer getDataOffset() {
        return this.dataOffset;
    }

    @SuppressWarnings("all")
    public Integer getDataSize() {
        return this.dataSize;
    }

    @SuppressWarnings("all")
    public Integer getDebugDataOffset() {
        return this.debugDataOffset;
    }

    @SuppressWarnings("all")
    public Integer getDebugDataSize() {
        return this.debugDataSize;
    }

    @SuppressWarnings("all")
    public Integer getConfigDataOffset() {
        return this.configDataOffset;
    }

    @SuppressWarnings("all")
    public Integer getConfigDataSize() {
        return this.configDataSize;
    }

    @SuppressWarnings("all")
    public void setDataOffset(final Integer dataOffset) {
        this.dataOffset = dataOffset;
    }

    @SuppressWarnings("all")
    public void setDataSize(final Integer dataSize) {
        this.dataSize = dataSize;
    }

    @SuppressWarnings("all")
    public void setDebugDataOffset(final Integer debugDataOffset) {
        this.debugDataOffset = debugDataOffset;
    }

    @SuppressWarnings("all")
    public void setDebugDataSize(final Integer debugDataSize) {
        this.debugDataSize = debugDataSize;
    }

    @SuppressWarnings("all")
    public void setConfigDataOffset(final Integer configDataOffset) {
        this.configDataOffset = configDataOffset;
    }

    @SuppressWarnings("all")
    public void setConfigDataSize(final Integer configDataSize) {
        this.configDataSize = configDataSize;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AssemblyStoreAssembly)) return false;
        final AssemblyStoreAssembly other = (AssemblyStoreAssembly) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$dataOffset = this.getDataOffset();
        final Object other$dataOffset = other.getDataOffset();
        if (this$dataOffset == null ? other$dataOffset != null : !this$dataOffset.equals(other$dataOffset)) return false;
        final Object this$dataSize = this.getDataSize();
        final Object other$dataSize = other.getDataSize();
        if (this$dataSize == null ? other$dataSize != null : !this$dataSize.equals(other$dataSize)) return false;
        final Object this$debugDataOffset = this.getDebugDataOffset();
        final Object other$debugDataOffset = other.getDebugDataOffset();
        if (this$debugDataOffset == null ? other$debugDataOffset != null : !this$debugDataOffset.equals(other$debugDataOffset)) return false;
        final Object this$debugDataSize = this.getDebugDataSize();
        final Object other$debugDataSize = other.getDebugDataSize();
        if (this$debugDataSize == null ? other$debugDataSize != null : !this$debugDataSize.equals(other$debugDataSize)) return false;
        final Object this$configDataOffset = this.getConfigDataOffset();
        final Object other$configDataOffset = other.getConfigDataOffset();
        if (this$configDataOffset == null ? other$configDataOffset != null : !this$configDataOffset.equals(other$configDataOffset)) return false;
        final Object this$configDataSize = this.getConfigDataSize();
        final Object other$configDataSize = other.getConfigDataSize();
        if (this$configDataSize == null ? other$configDataSize != null : !this$configDataSize.equals(other$configDataSize)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof AssemblyStoreAssembly;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $dataOffset = this.getDataOffset();
        result = result * PRIME + ($dataOffset == null ? 43 : $dataOffset.hashCode());
        final Object $dataSize = this.getDataSize();
        result = result * PRIME + ($dataSize == null ? 43 : $dataSize.hashCode());
        final Object $debugDataOffset = this.getDebugDataOffset();
        result = result * PRIME + ($debugDataOffset == null ? 43 : $debugDataOffset.hashCode());
        final Object $debugDataSize = this.getDebugDataSize();
        result = result * PRIME + ($debugDataSize == null ? 43 : $debugDataSize.hashCode());
        final Object $configDataOffset = this.getConfigDataOffset();
        result = result * PRIME + ($configDataOffset == null ? 43 : $configDataOffset.hashCode());
        final Object $configDataSize = this.getConfigDataSize();
        result = result * PRIME + ($configDataSize == null ? 43 : $configDataSize.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "AssemblyStoreAssembly(dataOffset=" + this.getDataOffset() + ", dataSize=" + this.getDataSize() + ", debugDataOffset=" + this.getDebugDataOffset() + ", debugDataSize=" + this.getDebugDataSize() + ", configDataOffset=" + this.getConfigDataOffset() + ", configDataSize=" + this.getConfigDataSize() + ")";
    }
    //</editor-fold>
}
