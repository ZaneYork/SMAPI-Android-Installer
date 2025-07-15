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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.data.DataItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;

import java.io.IOException;

public class DataItemUle128Reference<T extends DataItem> extends Ule128Item implements DataReference<T> {

    private final SectionType<T> sectionType;
    private T item;
    private final int usageType;

    public DataItemUle128Reference(SectionType<T> sectionType, int usageType){
        super();
        this.sectionType = sectionType;
        this.usageType = usageType;
    }

    @Override
    public T getItem(){
        return item;
    }
    @Override
    public T getOrCreate() {
        T item = getItem();
        if(item != null) {
            return item;
        }
        item = createSectionItem(getSectionType());
        setItem(item);
        return item;
    }
    @Override
    public void setItem(T item) {
        int offset = 0;
        if(item != null){
            offset = item.getOffset();
        }
        this.item = item;
        set(offset);
        updateItemUsage();
    }
    @Override
    public void setItem(Key key){
        setItem(getOrCreateSectionItem(getSectionType(), key));
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
    public void pullItem(){
        this.item = getSectionItem(getSectionType(), get());
        updateItemUsage();
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }
    @Override
    public void refresh() {
        T item = getItem();
        int value = 0;
        if(item != null){
            item = item.getReplace();
        }
        if(item != null){
            value = item.getIdx();
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
    @Override
    public void editInternal(Block user) {
        T item = getUniqueItem(user);
        if(item != null){
            item.editInternal(user);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        pullItem();
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
        item = createSectionItem(getSectionType());
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
        T itemNew = createSectionItem(getSectionType());
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
    public String toString() {
        T item = this.item;
        if(item != null){
            return item.toString();
        }
        return sectionType.getName() + ": " + get();
    }
}
