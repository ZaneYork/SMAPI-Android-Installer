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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.array.SpecStringArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class SpecStringPool extends StringPool<SpecString>{
    public SpecStringPool(boolean is_utf8){
        super(is_utf8);
    }

    public int resolveResourceId(int typeId, String name){
        Iterator<Entry> itr = getEntries(typeId, name);
        if(itr.hasNext()){
            return itr.next().getResourceId();
        }
        return 0;
    }
    public int resolveResourceId(String type, String name){
        Iterator<Entry> itr = getEntries(type, name);
        if(itr.hasNext()){
            return itr.next().getResourceId();
        }
        return 0;
    }
    public int resolveResourceId(Block parentContext, String name){
        Iterator<Entry> itr = getEntries(parentContext, name);
        if(itr.hasNext()){
            return itr.next().getResourceId();
        }
        return 0;
    }
    public Iterator<Entry> getEntries(int typeId, String name){
        return new IterableIterator<SpecString, Entry>(getAll(name)) {
            @Override
            public Iterator<Entry> iterator(SpecString element) {
                return element.getEntries(typeId);
            }
        };
    }
    public Iterator<Entry> getEntries(String type, String name){
        return new IterableIterator<SpecString, Entry>(getAll(name)) {
            @Override
            public Iterator<Entry> iterator(SpecString element) {
                return element.getEntries(type);
            }
        };
    }
    public Iterator<Entry> getEntries(Block parentContext, String name){
        return new IterableIterator<SpecString, Entry>(getAll(name)) {
            @Override
            public Iterator<Entry> iterator(SpecString element) {
                return element.getEntries(parentContext);
            }
        };
    }
    @Override
    StringArray<SpecString> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new SpecStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    public PackageBlock getPackageBlock(){
        return getParent(PackageBlock.class);
    }

    @Override
    void linkStrings(){
        super.linkStrings();
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            packageBlock.linkSpecStringsInternal(this);
        }
    }
}
