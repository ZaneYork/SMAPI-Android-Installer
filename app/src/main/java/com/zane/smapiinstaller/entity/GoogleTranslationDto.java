package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;

@Data
public class GoogleTranslationDto {
    private List<Entry> sentences;
    private String src;
    private double confidence;

    @Data
    public static class Entry {
        private String trans;
        private String orig;
        private int backend;
    }
}
