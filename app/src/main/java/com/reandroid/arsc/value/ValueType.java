/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.arsc.value;

import com.reandroid.graphics.AndroidColor;
import com.reandroid.utils.StringsUtil;

import java.util.HashMap;
import java.util.Map;

public enum ValueType {

    NULL((byte) 0x00, ""),
    REFERENCE((byte) 0x01, "reference"),
    ATTRIBUTE((byte) 0x02, "reference"),
    FLOAT((byte) 0x04, "float"),
    DIMENSION((byte) 0x05, "dimension"),
    FRACTION((byte) 0x06, "fraction"),
    DEC((byte) 0x10, "integer"),
    HEX((byte) 0x11, "integer"),
    BOOLEAN((byte) 0x12, "bool"),
    COLOR_ARGB8((byte) 0x1c, "color"),
    COLOR_RGB8((byte) 0x1d, "color"),
    COLOR_ARGB4((byte) 0x1e, "color"),
    COLOR_RGB4((byte) 0x1f, "color"),
    STRING((byte) 0x03, "string"),
    DYNAMIC_REFERENCE((byte) 0x07, "reference"),
    DYNAMIC_ATTRIBUTE((byte) 0x08, "reference");

    private final byte mByte;
    private final String typeName;
    ValueType(byte b, String typeName) {
        this.mByte = b;
        this.typeName = typeName;
    }
    public byte getByte(){
        return mByte;
    }
    public String getTypeName() {
        return typeName;
    }
    public boolean isColor(){
        return this == COLOR_ARGB8
                || this == COLOR_RGB8
                || this == COLOR_ARGB4
                || this == COLOR_RGB4;
    }
    public boolean isInteger(){
        return this == DEC
                || this == HEX;
    }
    public boolean isReference(){
        return this == REFERENCE
                || this == ATTRIBUTE
                || this == DYNAMIC_REFERENCE
                || this == DYNAMIC_ATTRIBUTE;
    }

    public static ValueType valueOf(byte b){
        return valueOf((b & 0x00ff));
    }
    public static ValueType valueOf(int i){
        if(i < 0){
            return null;
        }
        ValueType[] sorted = getSortedValues();
        if(i < sorted.length){
            return sorted[i];
        }
        return null;
    }
    public static ValueType fromName(String name){
        return getValueTypeMap().get(StringsUtil.toUpperCase(name));
    }
    public static AndroidColor.Type colorType(ValueType valueType){
        if(valueType == ValueType.COLOR_RGB4){
            return AndroidColor.Type.RGB4;
        }
        if(valueType == ValueType.COLOR_ARGB4){
            return AndroidColor.Type.ARGB4;
        }
        if(valueType == ValueType.COLOR_RGB8){
            return AndroidColor.Type.RGB8;
        }
        if(valueType == ValueType.COLOR_ARGB8){
            return AndroidColor.Type.ARGB8;
        }
        return null;
    }
    public static ValueType colorType(AndroidColor.Type type){
        if(type == AndroidColor.Type.RGB4){
            return COLOR_RGB4;
        }
        if(type == AndroidColor.Type.ARGB4){
            return COLOR_ARGB4;
        }
        if(type == AndroidColor.Type.RGB8){
            return COLOR_RGB8;
        }
        if(type == AndroidColor.Type.ARGB8){
            return COLOR_ARGB8;
        }
        return null;
    }
    private static ValueType[] getSortedValues(){
        if(sortedValues != null){
            return sortedValues;
        }
        synchronized (ValueType.class){
            ValueType[] sorted = new ValueType[0x1f + 1];
            ValueType[] values = values();
            for(ValueType valueType : values){
                sorted[valueType.getByte() & 0xff] = valueType;
            }
            sortedValues = sorted;
            return sorted;
        }
    }
    private static Map<String, ValueType> getValueTypeMap(){
        if(valueTypeMap != null){
            return valueTypeMap;
        }
        synchronized (ValueType.class){
            Map<String, ValueType> map = new HashMap<>();
            ValueType[] values = values();
            for(ValueType valueType : values){
                map.put(valueType.name(), valueType);
            }
            valueTypeMap = map;
            return map;
        }
    }
    private static ValueType[] sortedValues;
    private static Map<String, ValueType> valueTypeMap;
}
