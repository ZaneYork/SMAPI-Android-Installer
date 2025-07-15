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
package com.reandroid.dex.value;

import com.reandroid.arsc.base.BlockCreator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class DexValueType<T extends DexValueBlock<?>> implements BlockCreator<T> {

    private static final DexValueType<?>[] VALUES;
    private static final DexValueType<?>[] VALUES_COPY;

    public static final DexValueType<ByteValue> BYTE;
    public static final DexValueType<ShortValue> SHORT;
    public static final DexValueType<CharValue> CHAR;
    public static final DexValueType<IntValue> INT;
    public static final DexValueType<LongValue> LONG;
    public static final DexValueType<FloatValue> FLOAT;
    public static final DexValueType<DoubleValue> DOUBLE;
    public static final DexValueType<ProtoValue> PROTO;
    public static final DexValueType<MethodHandleValue> METHOD_HANDLE;
    public static final DexValueType<StringValue> STRING;
    public static final DexValueType<TypeValue> TYPE;
    public static final DexValueType<FieldIdValue> FIELD;
    public static final DexValueType<MethodIdValue> METHOD;
    public static final DexValueType<EnumValue> ENUM;
    public static final DexValueType<ArrayValue> ARRAY;
    public static final DexValueType<AnnotationValue> ANNOTATION;
    public static final DexValueType<NullValue> NULL;
    public static final DexValueType<BooleanValue> BOOLEAN;

    static {

        DexValueType<?>[] valueTypes = new DexValueType[0x1f + 1];
        VALUES = valueTypes;

        BYTE = new DexValueType<>("BYTE", 0x00, 0,
                ByteValue::new);
        valueTypes[0x00] = BYTE;
        SHORT = new DexValueType<>("SHORT", 0x02, 1,
                ShortValue::new);
        valueTypes[0x02] = SHORT;
        CHAR = new DexValueType<>("CHAR", 0x03, 1,
                CharValue::new);
        valueTypes[0x03] = CHAR;
        INT = new DexValueType<>("INT", 0x04, 3,
                IntValue::new);
        valueTypes[0x04] = INT;
        LONG = new DexValueType<>("LONG", 0x06, 7,
                LongValue::new);
        valueTypes[0x06] = LONG;
        FLOAT = new DexValueType<>("FLOAT", 0x10, 3,
                FloatValue::new);
        valueTypes[0x10] = FLOAT;
        DOUBLE = new DexValueType<>("DOUBLE", 0x11, 7,
                DoubleValue::new);
        valueTypes[0x11] = DOUBLE;

        PROTO = new DexValueType<>("PROTO", 0x15, 3,
                ProtoValue::new);
        valueTypes[0x15] = PROTO;

        METHOD_HANDLE = new DexValueType<>("METHOD_HANDLE", 0x16, 3,
                MethodHandleValue::new);
        valueTypes[0x16] = METHOD_HANDLE;

        STRING = new DexValueType<>("STRING", 0x17, 3,
                StringValue::new);
        valueTypes[0x17] = STRING;

        TYPE = new DexValueType<>("TYPE", 0x18, 3,
                TypeValue::new);
        valueTypes[0x18] = TYPE;

        FIELD = new DexValueType<>("FIELD", 0x19, 3,
                FieldIdValue::new);
        valueTypes[0x19] = FIELD;

        METHOD = new DexValueType<>("METHOD", 0x1a, 3,
                MethodIdValue::new);
        valueTypes[0x1a] = METHOD;

        ENUM = new DexValueType<>("ENUM", 0x1b, 3,
                EnumValue::new);
        valueTypes[0x1b] = ENUM;

        ARRAY = new DexValueType<>("ARRAY", 0x1c, 0,
                ArrayValue::new);
        valueTypes[0x1c] = ARRAY;

        ANNOTATION = new DexValueType<>("ANNOTATION", 0x1d, 0,
                AnnotationValue::new);
        valueTypes[0x1d] = ANNOTATION;

        NULL = new DexValueType<>("NULL", 0x1e, 0,
                NullValue::new);
        valueTypes[0x1e] = NULL;

        BOOLEAN = new DexValueType<>("BOOLEAN", 0x1f, 1,
                BooleanValue::new);
        valueTypes[0x1f] = BOOLEAN;

        int index = 0;
        for(int i = 0; i < valueTypes.length; i++){
            if(valueTypes[i] != null){
                index ++;
            }
        }
        VALUES_COPY = new DexValueType[index];
        index = 0;
        for(int i = 0; i < valueTypes.length; i++){
            if(valueTypes[i] != null){
                VALUES_COPY[index] = valueTypes[i];
                index ++;
            }
        }

    }

    private final String name;
    private final int type;
    private final int size;
    private final BlockCreator<T> creator;
    private final int flag;

    private DexValueType(String name, int type, int size, BlockCreator<T> creator){
        this.name = name;
        this.type = type;
        this.size = size;
        this.creator = creator;

        flag = (size << 5) | type;
    }

    public int getSize() {
        return size;
    }
    public int getType() {
        return type;
    }

    public int getFlag(){
        return flag;
    }
    public int getFlag(int size){
        return (size << 5) | type;
    }

    @Override
    public T newInstance() {
        return creator.newInstance();
    }

    @Override
    public String toString() {
        return name;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return flag;
    }

    public static DexValueType<?> fromFlag(int flag){
        return VALUES[flag & 0x1f];
    }

    public static DexValueBlock<?> create(BlockReader reader) throws IOException {
        int type = reader.read();
        reader.offset(-1);
        DexValueType<?> valueType = fromFlag(type);
        if(valueType == null){
            throw new IOException("Invalid value type: "
                    + HexUtil.toHex2((byte) type) + ", " + reader);
        }
        return valueType.newInstance();
    }
    public static int decodeSize(int flag){
        return flag >>> 5;
    }
    public static DexValueType<?>[] values() {
        return VALUES_COPY;
    }

    @SuppressWarnings("unchecked")
    public static<T1 extends IdItem> DexValueType<? extends SectionValue<T1>> get(SectionType<T1> sectionType){
        Object obj = null;
        if(sectionType == SectionType.STRING_ID){
            obj = STRING;
        }
        if(sectionType == SectionType.TYPE_ID){
            obj = TYPE;
        }
        if(sectionType == SectionType.FIELD_ID){
            // TODO: ambiguous stage could be DexValueType.ENUM or DexValueType.FIELD,
            //  lets favour the most common ENUM for now
            obj = ENUM;
        }
        if(sectionType == SectionType.METHOD_ID){
            obj = METHOD;
        }
        if(sectionType == SectionType.METHOD_HANDLE){
            obj = METHOD_HANDLE;
        }
        return (DexValueType<? extends SectionValue<T1>>) obj;
    }
}
