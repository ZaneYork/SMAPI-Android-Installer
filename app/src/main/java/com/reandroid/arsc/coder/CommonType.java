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
package com.reandroid.arsc.coder;

import com.reandroid.arsc.value.ValueType;

import java.util.HashMap;
import java.util.Map;

public class CommonType {
    private static final Map<String, CommonType> TYPE_MAP;

    public static final CommonType ANIM;
    public static final CommonType ANIMATOR;
    public static final CommonType BOOL;
    public static final CommonType COLOR;
    public static final CommonType DIMEN;
    public static final CommonType DRAWABLE;
    public static final CommonType FRACTION;
    public static final CommonType FONT;
    public static final CommonType ID;
    public static final CommonType INTEGER;
    public static final CommonType INTERPOLATOR;
    public static final CommonType LAYOUT;
    public static final CommonType MENU;
    public static final CommonType MIPMAP;
    public static final CommonType NAVIGATION;
    public static final CommonType RAW;
    public static final CommonType STRING;
    public static final CommonType TRANSITION;
    public static final CommonType XML;

    private static final CommonType[] VALUES;
    private static final String[] COMPLEX_TYPES;

    static {
        Map<String, CommonType> map = new HashMap<>();
        TYPE_MAP = map;
        CommonType type  = new CommonType("anim", new ValueType[]{
                ValueType.STRING,
        });
        map.put(type.name, type);
        ANIM = type;
        type  = new CommonType("animator", new ValueType[]{
                ValueType.STRING,
        });
        map.put(type.name, type);
        ANIMATOR = type;
        type  = new CommonType("bool", new ValueType[]{
                ValueType.BOOLEAN
        });
        map.put(type.name, type);
        BOOL = type;
        type  = new CommonType("color", new ValueType[]{
                ValueType.COLOR_ARGB8,
                ValueType.COLOR_RGB8,
                ValueType.COLOR_RGB4,
                ValueType.COLOR_ARGB4
        });
        map.put(type.name, type);
        COLOR = type;
        type  = new CommonType("dimen", new ValueType[]{
                ValueType.DIMENSION,
                ValueType.FRACTION,
                ValueType.FLOAT,
                ValueType.DEC
        });
        map.put(type.name, type);
        DIMEN = type;
        type  = new CommonType("drawable", new ValueType[]{
                ValueType.COLOR_ARGB8,
                ValueType.COLOR_RGB8,
                ValueType.COLOR_RGB4,
                ValueType.COLOR_ARGB4,
                ValueType.STRING
        });
        map.put(type.name, type);
        DRAWABLE = type;
        type  = new CommonType("fraction", new ValueType[]{
                ValueType.FRACTION
        });
        map.put(type.name, type);
        FRACTION = type;
        type  = new CommonType("font", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        FONT = type;
        type  = new CommonType("id", new ValueType[]{
                ValueType.BOOLEAN,
                ValueType.STRING
        });
        map.put(type.name, type);
        ID = type;
        type  = new CommonType("integer", new ValueType[]{
                ValueType.DEC,
                ValueType.HEX
        });
        map.put(type.name, type);
        INTEGER = type;
        type  = new CommonType("interpolator", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        INTERPOLATOR = type;
        type  = new CommonType("layout", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        LAYOUT = type;
        type  = new CommonType("menu", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        MENU = type;
        type  = new CommonType("mipmap", new ValueType[]{
                ValueType.COLOR_ARGB8,
                ValueType.COLOR_RGB8,
                ValueType.COLOR_RGB4,
                ValueType.COLOR_ARGB4,
                ValueType.STRING
        });
        map.put(type.name, type);
        MIPMAP = type;
        type  = new CommonType("navigation", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        NAVIGATION = type;
        type  = new CommonType("raw", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        RAW = type;
        type  = new CommonType("string", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        STRING = type;
        type  = new CommonType("transition", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        TRANSITION = type;
        type  = new CommonType("xml", new ValueType[]{
                ValueType.STRING
        });
        map.put(type.name, type);
        XML = type;

        VALUES = new CommonType[]{
                ANIM,
                ANIMATOR,
                BOOL,
                COLOR,
                DIMEN,
                DRAWABLE,
                FRACTION,
                FONT,
                ID,
                INTEGER,
                INTERPOLATOR,
                LAYOUT,
                MENU,
                MIPMAP,
                NAVIGATION,
                RAW,
                STRING,
                TRANSITION,
                XML,
        };

        COMPLEX_TYPES = new String[]{
                "attr",
                "array",
                "plurals",
                "style"
        };
    }

    private final String name;
    private final ValueType[] valueTypes;
    private final int hash;

    private CommonType(String name, ValueType[] valueTypes){
        this.name = name;
        this.valueTypes = valueTypes;
        int h = name.hashCode();
        h = h * 31 + h;
        this.hash = h;
    }

    public String name(){
        return name;
    }

    public ValueType[] valueTypes() {
        return valueTypes;
    }
    public boolean isDifferent(ValueType valueType){
        if(valueType == null
                || valueType == ValueType.NULL
                || valueType.isReference()){
            return false;
        }
        return !contains(valueType);
    }
    public boolean contains(ValueType valueType){
        for(ValueType vt : this.valueTypes){
            if(vt == valueType){
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return this.hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(", types = [");
        ValueType[] valueTypes = this.valueTypes;
        for(int i = 0; i < valueTypes.length; i++){
            if(i != 0){
                builder.append(", ");
            }
            builder.append(valueTypes[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    public static CommonType[] getValues(){
        return VALUES.clone();
    }
    public static CommonType valueOf(String typeName){
        return TYPE_MAP.get(typeName);
    }
    public static ValueType[] getExpectedTypes(String typeName){
        CommonType commonType = valueOf(typeName);
        if(commonType != null){
            return commonType.valueTypes();
        }
        return null;
    }
    public static boolean isCommonTypeName(String typeName){
        return TYPE_MAP.containsKey(typeName)
                || isComplexTypeName(typeName);
    }
    public static boolean isComplexTypeName(String typeName){
        for(String complex : COMPLEX_TYPES){
            if(complex.equals(typeName)){
                return true;
            }
        }
        return false;
    }
}
