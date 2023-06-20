package com.zane.smapiinstaller.dto;

import java.util.List;

/**
 * @author Zane
 */
public class GoogleTranslationDto {
    private List<Entry> sentences;
    private String src;
    private double confidence;


    public static class Entry {
        private String trans;
        private String orig;
        private int backend;

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public Entry() {
        }

        @SuppressWarnings("all")
        public String getTrans() {
            return this.trans;
        }

        @SuppressWarnings("all")
        public String getOrig() {
            return this.orig;
        }

        @SuppressWarnings("all")
        public int getBackend() {
            return this.backend;
        }

        @SuppressWarnings("all")
        public void setTrans(final String trans) {
            this.trans = trans;
        }

        @SuppressWarnings("all")
        public void setOrig(final String orig) {
            this.orig = orig;
        }

        @SuppressWarnings("all")
        public void setBackend(final int backend) {
            this.backend = backend;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof GoogleTranslationDto.Entry)) return false;
            final GoogleTranslationDto.Entry other = (GoogleTranslationDto.Entry) o;
            if (!other.canEqual((Object) this)) return false;
            if (this.getBackend() != other.getBackend()) return false;
            final Object this$trans = this.getTrans();
            final Object other$trans = other.getTrans();
            if (this$trans == null ? other$trans != null : !this$trans.equals(other$trans)) return false;
            final Object this$orig = this.getOrig();
            final Object other$orig = other.getOrig();
            if (this$orig == null ? other$orig != null : !this$orig.equals(other$orig)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof GoogleTranslationDto.Entry;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getBackend();
            final Object $trans = this.getTrans();
            result = result * PRIME + ($trans == null ? 43 : $trans.hashCode());
            final Object $orig = this.getOrig();
            result = result * PRIME + ($orig == null ? 43 : $orig.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "GoogleTranslationDto.Entry(trans=" + this.getTrans() + ", orig=" + this.getOrig() + ", backend=" + this.getBackend() + ")";
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public GoogleTranslationDto() {
    }

    @SuppressWarnings("all")
    public List<Entry> getSentences() {
        return this.sentences;
    }

    @SuppressWarnings("all")
    public String getSrc() {
        return this.src;
    }

    @SuppressWarnings("all")
    public double getConfidence() {
        return this.confidence;
    }

    @SuppressWarnings("all")
    public void setSentences(final List<Entry> sentences) {
        this.sentences = sentences;
    }

    @SuppressWarnings("all")
    public void setSrc(final String src) {
        this.src = src;
    }

    @SuppressWarnings("all")
    public void setConfidence(final double confidence) {
        this.confidence = confidence;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof GoogleTranslationDto)) return false;
        final GoogleTranslationDto other = (GoogleTranslationDto) o;
        if (!other.canEqual((Object) this)) return false;
        if (Double.compare(this.getConfidence(), other.getConfidence()) != 0) return false;
        final Object this$sentences = this.getSentences();
        final Object other$sentences = other.getSentences();
        if (this$sentences == null ? other$sentences != null : !this$sentences.equals(other$sentences)) return false;
        final Object this$src = this.getSrc();
        final Object other$src = other.getSrc();
        if (this$src == null ? other$src != null : !this$src.equals(other$src)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof GoogleTranslationDto;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $confidence = Double.doubleToLongBits(this.getConfidence());
        result = result * PRIME + (int) ($confidence >>> 32 ^ $confidence);
        final Object $sentences = this.getSentences();
        result = result * PRIME + ($sentences == null ? 43 : $sentences.hashCode());
        final Object $src = this.getSrc();
        result = result * PRIME + ($src == null ? 43 : $src.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "GoogleTranslationDto(sentences=" + this.getSentences() + ", src=" + this.getSrc() + ", confidence=" + this.getConfidence() + ")";
    }
    //</editor-fold>
}
