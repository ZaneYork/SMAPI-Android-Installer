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
package com.reandroid.dex.id;

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class TypeId extends IdItem implements Comparable<TypeId> {

    private final IndirectStringReference nameReference;

    public TypeId() {
        super(4);
        this.nameReference = new IndirectStringReference(this, 0, StringId.USAGE_TYPE_NAME);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return EmptyIterator.of();
    }
    public SectionType<TypeId> getSectionType(){
        return SectionType.TYPE_ID;
    }
    @Override
    public TypeKey getKey(){
        return checkKey(TypeKey.create(getName()));
    }
    @Override
    public void setKey(Key key){
        TypeKey typeKey = (TypeKey) key;
        setKey(typeKey);
    }
    public void setKey(TypeKey key){
        TypeKey old = getKey();
        if(Objects.equals(key, old)){
            return;
        }
        nameReference.setString(key.getTypeName());
        keyChanged(old);
    }
    public String getName(){
        StringId stringId = getNameId();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public boolean isWide(){
        TypeKey key = getKey();
        if(key != null){
            return key.isWide();
        }
        return false;
    }
    public StringId getNameId(){
        return nameReference.getItem();
    }

    public IndirectStringReference getNameReference() {
        return nameReference;
    }

    @Override
    public void refresh() {
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        nameReference.pullItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
    }

    @Override
    public int compareTo(TypeId typeId) {
        if(typeId == null){
            return -1;
        }
        return nameReference.compareTo(typeId.nameReference);
    }

    @Override
    public String toString(){
        String name = getName();
        if(name != null){
            return name;
        }
        return getIndex() + ":string-index=" + nameReference.get();
    }

    public static boolean equals(TypeId typeId1, TypeId typeId2) {
        if(typeId1 == typeId2){
            return true;
        }
        if(typeId1 == null){
            return false;
        }
        return ObjectsUtil.equals(typeId1.getName(), typeId2.getName());
    }
}
