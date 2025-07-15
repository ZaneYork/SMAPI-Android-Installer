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
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionType;

import java.io.IOException;

public class Ule128IdItemReference<T extends IdItem> extends Ule128Item implements IdReference<T> {

    private final SectionType<T> sectionType;
    private T item;
    private final int usageType;

    public Ule128IdItemReference(SectionType<T> sectionType, int usageType){
        super();
        this.sectionType = sectionType;
        this.usageType = usageType;
    }

    @Override
    public T getItem(){
        return item;
    }
    @Override
    public void setItem(T item) {
        int index = 0;
        if(item != null){
            index = item.getIdx();
            item.addUsageType(usageType);
        }
        this.item = item;
        set(index);
    }
    @Override
    public Key getKey(){
        T item = getItem();
        if(item != null){
            return ((KeyItem) item).getKey();
        }
        return null;
    }
    @Override
    public void setItem(Key item){
        DexSectionPool<T> pool = getPool(getSectionType());
        setItem(pool.getOrCreate(item));
    }
    @Override
    public SectionType<T> getSectionType(){
        return sectionType;
    }
    @Override
    public void pullItem(){
        this.item = getSectionItem(getSectionType(), get());
        updateItemUsage();
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
    private void updateItemUsage(){
        T item = this.item;
        if(item != null){
            item.addUsageType(this.usageType);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        pullItem();
    }

    @Override
    public String toString() {
        T item = this.item;
        if(item != null){
            return item.toString();
        }
        return getSectionType().getName() + ": " + get();
    }
}
