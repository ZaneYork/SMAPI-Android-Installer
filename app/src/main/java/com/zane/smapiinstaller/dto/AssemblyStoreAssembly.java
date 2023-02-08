package com.zane.smapiinstaller.dto;

import lombok.Data;

@Data
public class AssemblyStoreAssembly {
    private Integer dataOffset;
    private Integer dataSize;
    private Integer debugDataOffset;
    private Integer debugDataSize;
    private Integer configDataOffset;
    private Integer configDataSize;
}
