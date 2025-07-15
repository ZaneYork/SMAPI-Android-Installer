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
import com.reandroid.dex.data.DataItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;

public class DataItemIndirectReference<T extends DataItem> extends IndirectItem<SectionItem>
        implements DataReference<T> {

    private final SectionType<T> sectionType;
    private final int usageType;

    private T item;

    public DataItemIndirectReference(SectionType<T> sectionType, SectionItem blockItem, int offset, int usageType) {
        super(blockItem, offset);
        this.sectionType = sectionType;
        this.usageType = usageType;
    }

    @Override
    public T getItem() {
        T item = this.item;
        if(item != null) {
            T replace = item.getReplace();
            if(replace != item) {
                setItem(replace);
                item = this.item;
            }
        }
        return item;
    }
    @Override
    public T getOrCreate() {
        T item = getItem();
        if(item != null) {
            return item;
        }
        item = getBlockItem().createSectionItem(getSectionType());
        setItem(item);
        return item;
    }
    @Override
    public void setItem(T item) {
        if(item == this.item){
            return;
        }
        int value = 0;
        if(item != null){
            value = item.getIdx();
        }
        set(value);
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void setItem(Key key){
        setItem(getBlockItem().getOrCreateSectionItem(getSectionType(), key));
    }
    @Override
    public Key getKey() {
        T item = getItem();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }
    @Override
    public void pullItem(){
        int i = get();
        T item;
        if(i == 0){
            item = null;
        }else {
            item = getBlockItem().getSectionItem(getSectionType(), i);
        }
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void refresh() {
        int value = 0;
        T item = getItem();
        if(item != null) {
            value = item.getIdx();
            if(value == 0){
                throw new RuntimeException("Invalid reference");
            }
        }
        this.item = item;
        set(value);
        updateItemUsage();
    }
    @Override
    public void unlink(){
        this.item = null;
        set(0);
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
    public T getUniqueItem(Block user) {
        T item = getItem();
        if(item == null){
            return null;
        }
        if(item.isSharedItem(user)){
            item = createNewCopy();
        }
        item.addUniqueUser(user);
        return item;
    }
    public T getOrCreateUniqueItem(Block user) {
        T item = getUniqueItem(user);
        if(item != null) {
            return item;
        }
        item = getBlockItem().createSectionItem(getSectionType());
        setItem(item);
        addUniqueUser(user);
        return item;
    }
    public void addUniqueUser(Block user){
        T item = getItem();
        if(item != null){
            item.addUniqueUser(user);
        }
    }
    private T createNewCopy() {
        T itemNew = getBlockItem().createSectionItem(getSectionType());
        copyToIfPresent(itemNew);
        setItem(itemNew);
        return itemNew;
    }
    private void copyToIfPresent(T itemNew){
        T item = this.getItem();
        if(item != null){
            itemNew.copyFrom(item);
        }
    }


    @Override
    public void set(int value) {
        Block.putInteger(getBytesInternal(), getOffset(), value);
    }
    @Override
    public int get() {
        return Block.getInteger(getBytesInternal(), getOffset());
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
    public void editInternal(Block user) {
        T item = getUniqueItem(user);
        if(item != null){
            item.editInternal(user);
        }
    }

    @Override
    public String toString() {
        if(item != null){
            return get() + ":" +item.toString();
        }
        return getSectionType().getName() + ": " + get();
    }
}
