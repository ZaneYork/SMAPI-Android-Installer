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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;

import java.io.IOException;
import java.util.Objects;

public class DirectoryEntry<DEFINITION extends DefIndex, VALUE extends DataItem>
        extends DexBlockItem
        implements BlockRefresh, Comparable<DirectoryEntry<?, ?>> {

    private final SectionType<VALUE> sectionType;

    private DEFINITION mDefinition;
    private VALUE mValue;
    private Key mDefinitionKey;

    public DirectoryEntry(SectionType<VALUE> sectionType) {
        super(SIZE);
        this.sectionType = sectionType;
    }
    public int getDefinitionIndexValue() {
        return getInteger(getBytesInternal(), 0);
    }
    public void setDefinitionIndexValue(int value){
        if(value == getDefinitionIndexValue()){
            return;
        }
        putInteger(getBytesInternal(),0, value);
    }
    public int getValueOffset() {
        return getInteger(getBytesInternal(), 4);
    }
    public void setValueOffset(int value) {
        putInteger(getBytesInternal(), 4, value);
    }

    public DEFINITION getDefinition() {
        return mDefinition;
    }
    public void link(DEFINITION definition){
        if(definition == null || this.mDefinition != null){
            return;
        }
        Key key = getDefinitionKey();
        if(key != null){
            if(key.equals(definition.getKey())){
                this.mDefinition = definition;
                setDefinitionIndexValue(definition.getDefinitionIndex());
            }
        }else if(getDefinitionIndexValue() == definition.getDefinitionIndex()){
            this.mDefinition = definition;
        }
    }
    public void setDefinition(DEFINITION definition){
        this.mDefinition = definition;
        Key key = null;
        if(definition != null){
            key = definition.getKey();
        }
        setDefinitionKey(key);
    }
    public Key getDefinitionKey(){
        DEFINITION definition = getDefinition();
        if(definition != null){
            mDefinitionKey = definition.getKey();
        }
        return mDefinitionKey;
    }
    public void setDefinitionKey(Key key) {
        this.mDefinitionKey = key;
    }

    public VALUE getValue() {
        return mValue;
    }
    public void setValue(Key key){
        Section<VALUE> section = getOrCreateSection(sectionType);
        DexSectionPool<VALUE> pool = section.getPool();
        VALUE value = pool.getOrCreate(key);
        setValue(value);
    }
    public void setValue(VALUE value) {
        this.mValue = value;
        int offset = 0;
        if(value != null){
            offset = value.getOffset();
        }
        setValueOffset(offset);
    }
    public void set(DEFINITION definition, VALUE value){
        setDefinition(definition);
        setValue(value);
    }
    public Key getValueKey(){
        VALUE value = getValue();
        if(value != null){
            return value.getKey();
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItem();
    }
    private void cacheItem(){
        this.mValue = getSectionItem(sectionType, getValueOffset());
        if(this.mValue != null){
            this.mValue.addUsageType(UsageMarker.USAGE_ANNOTATION);
        }
    }

    @Override
    public void refresh() {
        DEFINITION definition = getDefinition();
        if(definition != null){
            setDefinitionIndexValue(definition.getDefinitionIndex());
        }
        VALUE value = refreshValue();
        int offset = 0;
        if(value != null){
            offset = value.getOffset();
        }
        setValueOffset(offset);
    }
    private VALUE refreshValue(){
        VALUE value = getValue();
        if(value == null){
            return null;
        }
        value = value.getReplace();
        this.mValue = value;
        if(value != null){
            value.addUsageType(UsageMarker.USAGE_ANNOTATION);
        }
        return value;
    }
    public boolean equalsDefIndex(int defIndex) {
        return getDefinitionIndexValue() == defIndex;
    }
    public boolean equalsDefIndex(DefIndex defIndex) {
        if(defIndex == null){
            return false;
        }
        DEFINITION definition = getDefinition();
        if(definition != null){
            return definition == defIndex;
        }
        return false;
    }
    public boolean equalsValue(VALUE value){
        return Objects.equals(getValue(), value);
    }
    public boolean matchesDefinition(Key definitionKey){
        return Objects.equals(getDefinitionKey(), definitionKey);
    }
    public boolean matchesValue(Key key){
        return Objects.equals(getValueKey(), key);
    }
    @Override
    public int compareTo(DirectoryEntry<?, ?> entry) {
        return compareDefIndex(getDefinition(), entry.getDefinition());
    }

    public void merge(DirectoryEntry<DEFINITION, VALUE> entry){
        if(entry == this){
            return;
        }
        setDefinitionKey(entry.getDefinitionKey());
        setValue(entry.getValueKey());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DirectoryEntry<?, ?> entry = (DirectoryEntry<?, ?>) obj;

        return Objects.equals(getDefinitionKey(), entry.getDefinitionKey()) &&
                Objects.equals(getValue(), entry.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 1;
        Object obj = getDefinitionKey();
        hash = hash * 31;
        if(obj != null){
            hash = hash + obj.hashCode();
        }
        obj = getValue();
        hash = hash * 31;
        if(obj != null){
            hash = hash + obj.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        DEFINITION definition = getDefinition();
        if(definition != null){
            builder.append(definition.getKey());
        }else {
            builder.append(getDefinitionIndexValue());
        }
        builder.append(" (");
        VALUE value = getValue();
        if(value != null){
            builder.append(value);
        }else {
            builder.append(getValueOffset());
        }
        builder.append(')');
        return builder.toString();
    }

    public static int compareDefIndex(DefIndex defIndex1, DefIndex defIndex2) {
        if(defIndex1 == defIndex2){
            return 0;
        }
        if(defIndex1 == null){
            return 1;
        }
        if(defIndex2 == null){
            return -1;
        }
        return Integer.compare(defIndex1.getDefinitionIndex(), defIndex2.getDefinitionIndex());
    }

    public static final int SIZE = 8;
}
