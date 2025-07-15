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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public abstract class PrimitiveValueBlock extends DexValueBlock<NumberValue> {

    public PrimitiveValueBlock(DexValueType<?> type) {
        super(new NumberValue(), type);
    }

    @Override
    public abstract Number getData();
    @Override
    public void setData(Object number) {
        setData((Number) number);
    }
    public abstract void setData(Number number);

    public long getSignedValue(){
        return getValueContainer().getSignedNumber();
    }
    public long getUnsigned(){
        return getValueContainer().getUnsignedNumber();
    }

    void setNumberValue(byte value){
        NumberValue container = getValueContainer();
        container.setNumberValue(value);
        setValueSize(container.getSize() - 1);
    }
    void setNumberValue(short value){
        if(value < 0) {
            byte b = (byte) value;
            if(b == value) {
                setNumberValue(b);
                return;
            }
        }
        NumberValue container = getValueContainer();
        container.setNumberValue(value);
        setValueSize(container.getSize() - 1);
    }
    void setNumberValue(int value){
        if(value < 0) {
            short s = (short) value;
            if(s == value) {
                setNumberValue(s);
                return;
            }
        }
        NumberValue container = getValueContainer();
        container.setNumberValue(value);
        setValueSize(container.getSize() - 1);
    }
    void setNumberValue(long value){
        if(value < 0) {
            int i = (int)value;
            if(i == value) {
                setNumberValue(i);
                return;
            }
        }
        NumberValue container = getValueContainer();
        container.setNumberValue(value);
        setValueSize(container.getSize() - 1);
    }
    void setUnsignedValue(long value){
        NumberValue container = getValueContainer();
        container.setUnsignedNumber(value);
        setValueSize(container.getSize() - 1);
    }
    void setValue(long value, int size){
        NumberValue container = getValueContainer();
        container.setNumber(value, size);
        setValueSize(container.getSize() - 1);
    }

    @Override
    public abstract PrimitiveKey getKey();
    public abstract String getHex();
    @Override
    public String getAsString() {
        return getHex();
    }
    @Override
    public abstract TypeKey getDataTypeKey();
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        getValueTypeItem().onReadBytes(reader);
        NumberValue container = getValueContainer();
        container.setSize(getValueSize() + 1);
        container.readBytes(reader);
    }

    @Override
    public void merge(DexValueBlock<?> valueBlock){
        super.merge(valueBlock);
        PrimitiveValueBlock coming = (PrimitiveValueBlock) valueBlock;
        getValueContainer().merge(coming.getValueContainer());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getHex());
    }
    @Override
    public String toString() {
        return getHex();
    }
}
