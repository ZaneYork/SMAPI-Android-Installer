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

import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.data.TypeList;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeListKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.Iterator;

public class TypeListReference extends DataItemIndirectReference<TypeList> implements Iterable<TypeId>{

    public TypeListReference(SectionItem sectionItem, int offset, int usageType) {
        super(SectionType.TYPE_LIST, sectionItem, offset, usageType);
    }

    @Override
    public Iterator<TypeId> iterator() {
        TypeList typeList = getItem();
        if(typeList != null){
            return typeList.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<TypeKey> getTypeKeys() {
        TypeList typeList = getItem();
        if(typeList != null){
            return typeList.getTypeKeys();
        }
        return EmptyIterator.of();
    }
    @Override
    public TypeListKey getKey() {
        return (TypeListKey) super.getKey();
    }
    public void add(String name){
        TypeListKey key = getKey();
        if(key != null){
            key = key.add(name);
        }else {
            key = new TypeListKey(new String[]{name});
        }
        setItem(key);
    }
    public void remove(int i){
        TypeListKey key = getKey();
        if(key == null){
            return;
        }
        key = key.remove(i);
        setItem(key);
    }
    public TypeKey getType(int i){
        TypeId typeId = get(i);
        if(typeId != null){
            return typeId.getKey();
        }
        return null;
    }
    public TypeId get(int i){
        TypeList typeList = getItem();
        if(typeList != null){
            return typeList.getTypeId(i);
        }
        return null;
    }
    public TypeId getForRegister(int register){
        TypeList typeList = getItem();
        if(typeList != null){
            return typeList.getTypeIdForRegister(register);
        }
        return null;
    }
    public int size(){
        TypeList typeList = getItem();
        if(typeList != null){
            return typeList.size();
        }
        return 0;
    }
    public int indexOf(String name){
        TypeListKey key = getKey();
        if(key != null){
            return key.indexOf(name);
        }
        return -1;
    }
}
