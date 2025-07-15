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
package com.reandroid.arsc.value;

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.refactor.ResourceMergeOption;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ResTableMapEntry extends CompoundEntry<ResValueMap, ResValueMapArray> {
    public ResTableMapEntry(){
        super(new ResValueMapArray());
    }

    public boolean isAttr(){
        boolean hasFormats = false;
        Iterator<ResValueMap> iterator = getValue().iterator();
        while (iterator.hasNext()){
            ResValueMap valueMap = iterator.next();
            AttributeType attributeType = valueMap.getAttributeType();
            if(attributeType != null && attributeType.isPlural()){
                return false;
            }
            if(attributeType == AttributeType.FORMATS){
                if(hasFormats){
                    return false;
                }
                hasFormats = true;
            }
        }
        return hasFormats;
    }
    public boolean isPlural(){
        Set<AttributeType> uniqueSet = new HashSet<>();
        Iterator<ResValueMap> iterator = getValue().iterator();
        while (iterator.hasNext()){
            ResValueMap valueMap = iterator.next();
            AttributeType attributeType = valueMap.getAttributeType();
            if(attributeType == null || !attributeType.isPlural()){
                return false;
            }
            if(uniqueSet.contains(attributeType)){
                return false;
            }
            uniqueSet.add(attributeType);
        }
        return uniqueSet.size() > 0;
    }
    public boolean isArray(){
        int size = getValue().size();
        Iterator<ResValueMap> iterator = getValue().iterator();
        while (iterator.hasNext()){
            ResValueMap valueMap = iterator.next();
            int id = valueMap.getArrayIndex();
            if(id >= 0 && id <= size){
                continue;
            }
            return false;
        }
        if(size != 0){
            return true;
        }
        Entry entry = getParentEntry();
        if(entry == null){
            return false;
        }
        return TypeString.isTypeArray(entry.getTypeName());
    }
    public boolean isStyle(){
        if(getParentId() != 0){
            return true;
        }
        Entry entry = getParentEntry();
        if(entry != null){
            return TypeString.isTypeStyle(entry.getTypeName());
        }
        return false;
    }
    public ValueType isAllSameValueType(){
        ValueType allValueType = null;
        Iterator<ResValueMap> iterator = getValue().iterator();
        while (iterator.hasNext()){
            ResValueMap valueMap = iterator.next();
            ValueType valueType = valueMap.getValueType();
            if(valueType == null){
                return null;
            }
            if(valueType == ValueType.REFERENCE){
                continue;
            }
            if(allValueType == null){
                allValueType = valueType;
            }else if(valueType != allValueType){
                return null;
            }
        }
        return allValueType;
    }
    @Override
    boolean canMerge(TableEntry<?, ?> tableEntry){
        if(tableEntry == this || !(tableEntry instanceof ResTableMapEntry)){
            return false;
        }
        ResValueMapArray coming = ((ResTableMapEntry) tableEntry).getValue();
        if(coming.size() == 0){
            return false;
        }
        return getValue().size() == 0;
    }

    @Override
    public void merge(TableEntry<?, ?> tableEntry){
        if(tableEntry==null || tableEntry==this){
            return;
        }
        ResTableMapEntry coming = (ResTableMapEntry) tableEntry;
        getHeader().merge(coming.getHeader());
        getValue().merge(coming.getValue());
        refresh();
    }
    @Override
    public void mergeWithName(ResourceMergeOption mergeOption, TableEntry<?, ?> tableEntry){
        if(tableEntry==null || tableEntry==this){
            return;
        }
        ResTableMapEntry coming = (ResTableMapEntry) tableEntry;
        getHeader().mergeWithName(mergeOption, coming.getHeader());
        getValue().mergeWithName(mergeOption, coming.getValue());
        refresh();
    }
}
