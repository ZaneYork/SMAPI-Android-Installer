package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;

@Data
public class YouDaoTranslationDto {
    //{"type":"ZH_CN2EN","errorCode":0,"elapsedTime":2,"translateResult":[[{"src":"云计算","tgt":"Cloud computing"}],[{"src":"前往合肥","tgt":"Travel to hefei"}]]}
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
