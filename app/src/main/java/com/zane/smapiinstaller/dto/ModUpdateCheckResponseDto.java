package com.zane.smapiinstaller.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Zane
 */
@Data
public class ModUpdateCheckResponseDto {
    private String id;
    private UpdateInfo suggestedUpdate;
    private List<String> errors;
    private Metadata metadata;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateInfo {
        private String version;
        private String url;
    }

    @Data
    public static class Metadata {
        private Main main;
        @Data
        public static class Main {
            private String version;
            private String url;
        }
    }
}
