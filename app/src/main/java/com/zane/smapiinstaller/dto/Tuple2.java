package com.zane.smapiinstaller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tuple2<U, V> {
    private U first;
    private V second;
}
