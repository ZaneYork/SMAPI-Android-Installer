package com.zane.smapiinstaller.dto;

import java.util.List;

/**
 * @author Zane
 */
public class YouDaoTranslationDto {
    private String type;
    private int errorCode;
    private int elapsedTime;
    private List<List<Entry>> translateResult;


    public static class Entry {
        private String src;
        private String tgt;

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public Entry() {
        }

        @SuppressWarnings("all")
        public String getSrc() {
            return this.src;
        }

        @SuppressWarnings("all")
        public String getTgt() {
            return this.tgt;
        }

        @SuppressWarnings("all")
        public void setSrc(final String src) {
            this.src = src;
        }

        @SuppressWarnings("all")
        public void setTgt(final String tgt) {
            this.tgt = tgt;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof YouDaoTranslationDto.Entry)) return false;
            final YouDaoTranslationDto.Entry other = (YouDaoTranslationDto.Entry) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$src = this.getSrc();
            final Object other$src = other.getSrc();
            if (this$src == null ? other$src != null : !this$src.equals(other$src)) return false;
            final Object this$tgt = this.getTgt();
            final Object other$tgt = other.getTgt();
            if (this$tgt == null ? other$tgt != null : !this$tgt.equals(other$tgt)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof YouDaoTranslationDto.Entry;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $src = this.getSrc();
            result = result * PRIME + ($src == null ? 43 : $src.hashCode());
            final Object $tgt = this.getTgt();
            result = result * PRIME + ($tgt == null ? 43 : $tgt.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "YouDaoTranslationDto.Entry(src=" + this.getSrc() + ", tgt=" + this.getTgt() + ")";
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public YouDaoTranslationDto() {
    }

    @SuppressWarnings("all")
    public String getType() {
        return this.type;
    }

    @SuppressWarnings("all")
    public int getErrorCode() {
        return this.errorCode;
    }

    @SuppressWarnings("all")
    public int getElapsedTime() {
        return this.elapsedTime;
    }

    @SuppressWarnings("all")
    public List<List<Entry>> getTranslateResult() {
        return this.translateResult;
    }

    @SuppressWarnings("all")
    public void setType(final String type) {
        this.type = type;
    }

    @SuppressWarnings("all")
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    @SuppressWarnings("all")
    public void setElapsedTime(final int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @SuppressWarnings("all")
    public void setTranslateResult(final List<List<Entry>> translateResult) {
        this.translateResult = translateResult;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof YouDaoTranslationDto)) return false;
        final YouDaoTranslationDto other = (YouDaoTranslationDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getErrorCode() != other.getErrorCode()) return false;
        if (this.getElapsedTime() != other.getElapsedTime()) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$translateResult = this.getTranslateResult();
        final Object other$translateResult = other.getTranslateResult();
        if (this$translateResult == null ? other$translateResult != null : !this$translateResult.equals(other$translateResult)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof YouDaoTranslationDto;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getErrorCode();
        result = result * PRIME + this.getElapsedTime();
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $translateResult = this.getTranslateResult();
        result = result * PRIME + ($translateResult == null ? 43 : $translateResult.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "YouDaoTranslationDto(type=" + this.getType() + ", errorCode=" + this.getErrorCode() + ", elapsedTime=" + this.getElapsedTime() + ", translateResult=" + this.getTranslateResult() + ")";
    }
    //</editor-fold>
}
