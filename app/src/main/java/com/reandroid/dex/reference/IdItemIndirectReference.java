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
package com.reandroid.dex.reference;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IndirectItem;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;

import java.lang.reflect.Field;

public class IdItemIndirectReference<T extends IdItem> extends IndirectItem<SectionItem>
        implements IdReference<T>, Comparable<IdReference<T>>{

    private final SectionType<T> sectionType;
    private final int usageType;
    private T item;

    public IdItemIndirectReference(SectionType<T> sectionType, SectionItem blockItem, int offset, int usage) {
        super(blockItem, offset);
        this.sectionType = sectionType;
        this.usageType = usage;
        Block.putInteger(getBytesInternal(), getOffset(), -1);
    }
    public IdItemIndirectReference(SectionType<T> sectionType, SectionItem blockItem, int offset) {
        this(sectionType, blockItem, offset, IdItem.USAGE_NONE);
    }

    @Override
    public Key getKey(){
        T item = getItem();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    @Override
    public T getItem() {
        return item;
    }
    @Override
    public void setItem(T item) {
        if(item == this.item){
            return;
        }
        int index = getItemIndex(item);
        set(index);
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void pullItem(){
        this.item = pullItem(get());
        updateItemUsage();
    }
    protected T pullItem(int i){
        return getBlockItem().getSectionItem(getSectionType(), i);
    }
    @Override
    public void setItem(Key key){
        setItem(getBlockItem().getOrCreateSectionItem(getSectionType(), key));
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }

    @Override
    public void set(int value) {
        Block.putInteger(getBytesInternal(), getOffset(), value);
    }
    @Override
    public int get() {
        return Block.getInteger(getBytesInternal(), getOffset());
    }

    @Override
    public void refresh() {
        T item = getItem();
        if(item != null){
            item = item.getReplace();
        }
        checkNonNullItem(item);
        if(item != null){
            set(item.getIdx());
        }
        this.item = item;
        updateItemUsage();
    }

    @Override
    public void checkNonNullItem(T item) {
        if(item != null){
            return;
        }
        throw new NullPointerException(buildMessage());
    }

    private String buildMessage(){
        SectionItem blockItem = getBlockItem();
        StringBuilder builder = new StringBuilder();
        builder.append("Parent = ");
        builder.append(blockItem);
        Class<?> clazz = blockItem.getClass();
        Field[] fields = clazz.getFields();
        for(Field field : fields){
            try {
                field.setAccessible(true);
                Object obj = field.get(blockItem);
                if(obj == this){
                    builder.append(", Field = ");
                    builder.append(field.getName());
                    break;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        builder.append(", section = ");
        builder.append(getSectionType().getName());
        return builder.toString();
    }

    protected int getItemIndex(T item) {
        if(item == null){
            throw new NullPointerException("Can't set null for reference of: " + getSectionType().getName());
        }
        return item.getIdx();
    }
    private void updateItemUsage(){
        int usageType = this.usageType;
        if(usageType == UsageMarker.USAGE_NONE){
            return;
        }
        T item = this.item;
        if(item != null){
            item.addUsageType(usageType);
        }
    }
    public void unlink(){
        this.item = null;
        set(0);
    }

    public void replaceKeys(Key search, Key replace){
        Key key = getKey();
        if(key == null){
            return;
        }
        Key key2 = key.replaceKey(search, replace);
        if(key != key2){
            setItem(key2);
        }
    }

    @Override
    public int compareTo(IdReference<T> reference) {
        return SectionTool.compareIdx(getItem(), reference.getItem());
    }
    @Override
    public String toString() {
        if(item != null){
            return get() + ": " + item.toString();
        }
        return getSectionType().getName() + ": " + get();
    }
}
