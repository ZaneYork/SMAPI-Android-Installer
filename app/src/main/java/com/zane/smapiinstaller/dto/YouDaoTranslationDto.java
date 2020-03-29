package com.zane.smapiinstaller.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Zane
 */
@Data
public class YouDaoTranslationDto {
    private String type;
    private int errorCode;
    private int elapsedTime;
    private List<List<Entry>> translateResult;

    @Data
    public static class Entry {
        private String src;
        private String tgt;
    }
}
