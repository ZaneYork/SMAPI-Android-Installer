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
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.SingleIterator;

import java.util.Iterator;

public abstract class SectionIdValue<T extends IdItem> extends SectionValue<T> {

    public SectionIdValue(SectionType<T> sectionType, DexValueType<?> type) {
        super(sectionType, type);
    }

    public void setKey(Key key) {
        setItem(key);
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
    int getSectionValue(T data){
        if(data == null){
            throw new NullPointerException("Section data can not be null: " +
                    getSectionType().getName());
        }
        return data.getIndex();
    }
    @Override
    T getReplacement(T data) {
        if(data != null){
            data = data.getReplace();
        }
        if(data == null){
            throw new NullPointerException("Section data can not be null: " +
                    getSectionType().getName());
        }
        return data;
    }

    @Override
    void updateUsageType(T data){
        if(data != null){
            int usage;
            if(getParent(AnnotationElement.class) != null){
                usage = UsageMarker.USAGE_ANNOTATION;
            }else {
                usage = UsageMarker.USAGE_ENCODED_VALUE;
            }
            data.addUsageType(usage);
        }
    }
    @Override
    public void replaceKeys(Key search, Key replace) {
        Key key = getKey();
        Key key2 = key.replaceKey(search, replace);
        if(key != key2){
            setItem(key2);
        }
    }

    @Override
    public void editInternal(Block user) {
        // TODO: what ?
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(getItem());
    }

}
