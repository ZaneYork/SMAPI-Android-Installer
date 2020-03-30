package com.zane.smapiinstaller.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Zane
 */
@Data
public class ModUpdateCheckResponseDto {
    private String id;
    private UpdateInfo suggestedUpdate;
    private List<String> errors;
    @Data
    public static class UpdateInfo {
        private String version;
        private String url;
    }
}
