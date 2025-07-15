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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class DexValueBlock<T extends Block> extends FixedBlockContainer implements SmaliFormat {

    private final ByteItem valueTypeItem;
    private final T valueContainer;

    private boolean mTemporary;

    DexValueBlock(T value, DexValueType<?> type){
        super(2);
        valueTypeItem = new ByteItem();
        valueContainer = value;
        addChild(0, valueTypeItem);
        addChild(1, valueContainer);
        valueTypeItem.set((byte) type.getFlag(0));
    }
    DexValueBlock(DexValueType<?> type){
        this(null, type);
    }

    public boolean isTemporary() {
        return mTemporary;
    }
    public void setTemporary(boolean temporary) {
        this.mTemporary = temporary;
    }

    T getValueContainer(){
        return valueContainer;
    }
    ByteItem getValueTypeItem(){
        return valueTypeItem;
    }
    public DexValueType<?> getValueType(){
        return getValueTypeReal();
    }
    private DexValueType<?> getValueTypeReal(){
        return DexValueType.fromFlag(valueTypeItem.get());
    }
    int getValueSize(){
        return DexValueType.decodeSize(valueTypeItem.get());
    }
    void setValueSize(int size){
        int flag = getValueType().getFlag(size);
        valueTypeItem.set((byte) flag);
    }

    public Key getKey() {
        throw new RuntimeException("Method not implemented");
    }
    public void replaceKeys(Key search, Key replace){
    }
    public Iterator<IdItem> usedIds(){
        return EmptyIterator.of();
    }
    public void merge(DexValueBlock<?> valueBlock){
        valueTypeItem.set(valueBlock.valueTypeItem.getByte());
    }
    public void fromSmali(SmaliValue smaliValue){
        throw new RuntimeException("Method not implemented: " + getClass().getSimpleName());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        T value = getValueContainer();
        if(value instanceof SmaliFormat){
            ((SmaliFormat)value).append(writer);
        }
    }
    public String getAsString() {
        return String.valueOf(getValueContainer());
    }

    public boolean is(DexValueType<?> dexValueType){
        return dexValueType == getValueType();
    }
    public TypeKey getDataTypeKey(){
        return TypeKey.OBJECT;
    }
    public Object getData() {
        return null;
    }
    public void setData(Object data) {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getValueType().getType();
        hash = hash * 31 + getValueContainer().hashCode();
        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DexValueBlock<?> value = (DexValueBlock<?>) obj;
        return Objects.equals(getValueContainer(), value.getValueContainer());
    }


    @Override
    public String toString() {
        return String.valueOf(getValueContainer());
    }
}
