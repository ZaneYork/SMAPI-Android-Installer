package com.zane.smapiinstaller.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Zane
 */
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
