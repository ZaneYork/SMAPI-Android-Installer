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
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.reference.DexReference;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueKey;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Objects;

public abstract class SectionValue<T extends SectionItem> extends DexValueBlock<NumberValue>
        implements SmaliFormat, KeyItem, DexReference<T> {

    private final SectionType<T> sectionType;
    private T mData;

    public SectionValue(SectionType<T> sectionType, DexValueType<?> type){
        super(new NumberValue(), type);
        this.sectionType = sectionType;
    }

    @Override
    public abstract Key getKey();
    @Override
    public T getItem(){
        return mData;
    }
    @Override
    public void setItem(T data){
        if(data == mData){
            return;
        }
        this.mData = data;
        set(getSectionValue(data));
        updateUsageType(data);
    }
    @Override
    public void setItem(Key key){
        Section<T> section = getOrCreateSection();
        T item = section.getOrCreate(key);
        setItem(item);
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }
    @Override
    public int get(){
        return (int) getValueContainer().getUnsignedNumber();
    }
    @Override
    public void set(int value){
        NumberValue numberValue = getValueContainer();
        numberValue.setUnsignedNumber(value & 0xffffffffL);
        int size = numberValue.getSize();
        setValueSize(size - 1);
    }
    @Override
    public abstract DexValueType<?> getValueType();
    abstract int getSectionValue(T data);
    abstract T getReplacement(T data);
    abstract void updateUsageType(T data);
    Section<T> getSection(){
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null) {
            return sectionList.getSection(getSectionType());
        }
        return null;
    }
    private Section<T> getOrCreateSection(){
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null) {
            return sectionList.getOrCreateSection(getSectionType());
        }
        throw new NullPointerException("Null parent SectionList");
    }
    @SuppressWarnings("unchecked")
    @Override
    public void merge(DexValueBlock<?> valueBlock){
        super.merge(valueBlock);
        SectionValue<T> value = (SectionValue<T>) valueBlock;
        setItem(value.getKey());
    }
    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueKey smaliValueKey = (SmaliValueKey) smaliValue;
        setItem(smaliValueKey.getValue());
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        getValueTypeItem().onReadBytes(reader);
        NumberValue numberValue = getValueContainer();
        numberValue.setSize(getValueSize() + 1);
        numberValue.readBytes(reader);
        pullItem();
    }
    @Override
    protected void onPreRefresh() {
        refreshItem();
    }
    private void refreshItem() {
        T data = getReplacement(this.mData);
        this.mData = data;
        set(getSectionValue(data));
        updateUsageType(data);
    }
    public void pullItem(){
        Section<T> section = getSection();
        if(section != null){
            mData = section.getSectionItem(get());
            updateUsageType(mData);
        }
    }

    @Override
    public String getAsString() {
        Key key = getKey();
        if(key != null){
            return key.toString();
        }
        return null;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        T data = getItem();
        if(data == null){
            writer.append("value error: ");
            writer.append(getSectionType().getName());
            writer.append(' ');
            writer.append(HexUtil.toHex(get(), getValueSize()));
        }else {
            ((SmaliFormat) data).append(writer);
        }
    }

    @Override
    public int hashCode() {
        Key key = getKey();
        if(key != null){
            return key.hashCode();
        }
        return 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SectionValue<?> value = (SectionValue<?>)obj;
        return Objects.equals(getKey(), value.getKey());
    }
    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }
}
