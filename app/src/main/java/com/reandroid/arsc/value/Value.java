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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.ParentChunk;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.graphics.AndroidColor;

public interface Value {
    void setValue(EncodeResult encodeResult);
    void setValueType(ValueType valueType);
    ValueType getValueType();
    int getData();
    void setData(int data);
    String getValueAsString();
    void setValueAsString(String value);
    PackageBlock getPackageBlock();
    ParentChunk getParentChunk();
    default void setTypeAndData(ValueType valueType, int data) {
        setData(data);
        setValueType(valueType);
    }
    default AndroidColor getValueAsColor() {
        ValueType valueType = getValueType();
        if(valueType == null || !valueType.isColor()){
            return null;
        }
        return AndroidColor.decode(ValueCoder.decode(valueType, getData()));
    }
    default void setValue(AndroidColor color) {
        setValue(ValueCoder.encode(color.toHexString()));
    }
    default ResourceEntry getValueAsReference() {
        ValueType valueType = getValueType();
        if(valueType == null || !valueType.isReference()){
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null) {
            return null;
        }
        int data = getData();
        ResourceEntry resourceEntry = packageBlock.getResource(data);
        if(resourceEntry == null) {
            TableBlock tableBlock = packageBlock.getTableBlock();
            if(tableBlock != null){
                resourceEntry = tableBlock.getResource(packageBlock, data);
            }
        }
        return resourceEntry;
    }
    default Float getValueAsFloat() {
        ValueType valueType = getValueType();
        if(valueType != ValueType.FLOAT){
            return null;
        }
        return Float.intBitsToFloat(getData());
    }
    default void setValueAsFloat(float f) {
        setTypeAndData(ValueType.FLOAT, Float.floatToIntBits(f));
    }
    default Integer getValueAsInteger() {
        ValueType valueType = getValueType();
        if(valueType == ValueType.DEC || valueType == ValueType.HEX){
            return getData();
        }
        return null;
    }
    default void setValueAsDecimal(int i) {
        setTypeAndData(ValueType.DEC, i);
    }
    default void setValueAsHex(int i) {
        setTypeAndData(ValueType.HEX, i);
    }
    default int getValueAsResourceId() {
        ValueType valueType = getValueType();
        if(valueType != null && valueType.isReference()){
            return getData();
        }
        return 0;
    }
    default void setValueAsResourceId(int id) {
        setTypeAndData(ValueType.REFERENCE, id);
    }
    default void setValueAsResourceAttributeId(int id) {
        setTypeAndData(ValueType.ATTRIBUTE, id);
    }
}
