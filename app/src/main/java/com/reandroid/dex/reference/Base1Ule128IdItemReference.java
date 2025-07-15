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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class Base1Ule128IdItemReference<T extends IdItem> extends Ule128Item implements
        IdReference<T>, SmaliFormat {

    private final SectionType<T> sectionType;
    private final int usageType;
    private T item;

    public Base1Ule128IdItemReference(SectionType<T> sectionType, int usageType){
        super();
        this.sectionType = sectionType;
        this.usageType = usageType;
    }
    public Base1Ule128IdItemReference(SectionType<T> sectionType){
        this(sectionType, UsageMarker.USAGE_DEBUG);
    }

    @Override
    public T getItem(){
        return item;
    }
    @Override
    public void setItem(T item) {
        if(item != null){
            item = item.getReplace();
        }
        int index;
        if(item != null){
            index = item.getIdx() + 1;
        }else {
            index = 0;
        }
        this.item = item;
        set(index);
        if(item != null){
            item.addUsageType(UsageMarker.USAGE_DEBUG);
        }
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
    public void setItem(Key key) {
        T item;
        if(key != null){
            item = getSection(getSectionType()).getOrCreate(key);
        }else {
            item = null;
        }
        setItem(item);
    }
    @Override
    public SectionType<T> getSectionType() {
        return sectionType;
    }
    @Override
    public void pullItem(){
        int index = get();
        T item;
        if(index == 0){
            item = null;
        }else {
            item = getSectionItem(getSectionType(), index - 1);
            //TODO: remove this for peaceful dex loading
            checkNonNullItem(item, index - 1);
        }
        this.item = item;
        updateItemUsage();
    }
    @Override
    public void refresh() {
        T item = getItem();
        if(item != null){
            item = item.getReplace();
        }
        int idx = 0;
        if(item != null){
            idx = item.getIdx() + 1;
        }
        this.item = item;
        set(idx);
        updateItemUsage();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        pullItem();
    }

    private void updateItemUsage(){
        T item = this.item;
        if(item != null){
            item.addUsageType(usageType);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        T item = getItem();
        if(item != null){
            item.append(writer);
        }
    }
    @Override
    public String toString() {
        Key key = getKey();
        if(key != null){
            return key.toString();
        }
        return getSectionType().getName() + ": " + (get() - 1);
    }
}
