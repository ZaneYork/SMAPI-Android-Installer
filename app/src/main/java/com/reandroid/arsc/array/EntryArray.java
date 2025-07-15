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
package com.reandroid.arsc.array;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.util.Iterator;

public class EntryArray extends OffsetBlockArray<Entry> implements JSONConvert<JSONArray> {

    public EntryArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super(offsets, itemCount, itemStart);
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool){
        Iterator<Entry> itr = iterator(true);
        while (itr.hasNext()){
            Entry entry = itr.next();
            entry.linkTableStringsInternal(tableStringPool);
        }
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        Iterator<Entry> itr = iterator(true);
        while (itr.hasNext()){
            Entry entry = itr.next();
            entry.linkSpecStringsInternal(specStringPool);
        }
    }
    public int getHighestEntryId(){
        if(isSparse()){
            return ((SparseOffsetsArray) getOffsetArray()).getHighestId();
        }
        return size() - 1;
    }
    public int getEntryId(int index){
        OffsetArray offsetArray = getOffsetArray();
        if(offsetArray instanceof SparseOffsetsArray){
            return ((SparseOffsetsArray) offsetArray).getIdx(index);
        }
        return index;
    }
    public int getEntryIndex(int entryId){
        OffsetArray offsetArray = getOffsetArray();
        if(offsetArray instanceof SparseOffsetsArray){
            return ((SparseOffsetsArray) offsetArray).indexOf(entryId);
        }
        return entryId;
    }
    public boolean isSparse(){
        return super.getOffsetArray() instanceof SparseOffsetsArray;
    }
    public void destroy(){
        for(Entry entry : listItems()){
            if(entry != null){
                entry.setNull(true);
            }
        }
        clear();
    }
    public Boolean hasComplexEntry(){
        Iterator<Entry> itr = iterator(true);
        while (itr.hasNext()){
            Entry entry = itr.next();
            if(entry.isComplex()){
                return true;
            }
            ResValue resValue = entry.getResValue();
            ValueType valueType = resValue.getValueType();
            if(valueType == null || valueType == ValueType.REFERENCE
                    || valueType == ValueType.NULL){
                continue;
            }
            return false;
        }
        return null;
    }
    @Override
    public boolean isEmpty(){
        return !iterator(true).hasNext();
    }

    public Entry getOrCreate(short entryId){
        int id = 0xffff & entryId;
        Entry entry = getEntry(id);
        if(entry != null){
            return entry;
        }
        boolean sparse = isSparse();
        int count;
        if(sparse){
            count = size() + 1;
        }else {
            count = id + 1;
        }
        updateHighestCount(count);
        if(!sparse){
            refreshCount();
            return super.get(id);
        }
        SparseOffsetsArray offsetsArray = (SparseOffsetsArray) getOffsetArray();
        offsetsArray.ensureArraySize(size());
        int index = count - 1;
        offsetsArray.setIdx(index, id);
        refreshCount();
        return super.get(index);
    }
    public Entry get(short entryId){
        return getEntry(entryId);
    }
    public Entry getEntry(short entryId){
        return getEntry(0xffff & entryId);
    }
    public Entry getEntry(int entryId){
        int index = getEntryIndex(entryId);
        return super.get(index);
    }
    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     */
    public Entry getEntry(String entryName){
        if(entryName == null){
            return null;
        }
        TypeBlock typeBlock = getParentInstance(TypeBlock.class);
        if(typeBlock == null){
            return null;
        }
        PackageBlock packageBlock = typeBlock.getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        Iterator<Entry> iterator = packageBlock.getEntries(
                typeBlock.getTypeName(), entryName);
        while (iterator.hasNext()){
            Entry entry = iterator.next();
            if(entry.getParentInstance(EntryArray.class) == this){
                return entry;
            }
        }
        return null;
    }
    @Override
    public Entry newInstance() {
        return new Entry();
    }
    @Override
    public Entry[] newArrayInstance(int len) {
        return new Entry[len];
    }

    @Override
    public JSONArray toJson() {
        JSONArray jsonArray = new JSONArray();
        String name_id = Entry.NAME_id;
        Iterator<Entry> iterator = iterator(true);
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            JSONObject childObject = entry.toJson();
            if(childObject != null) {
                childObject.put(name_id, entry.getId());
                jsonArray.put(childObject);
            }
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clear();
        if(isSparse()){
            fromJsonSparse(json);
        }else {
            fromJsonNonSparse(json);
        }
        refreshCountAndStart();
    }
    private void fromJsonNonSparse(JSONArray json){
        int length=json.length();
        ensureSize(length);
        String name_id = Entry.NAME_id;
        for(int i = 0; i < length; i++){
            JSONObject jsonObject = json.optJSONObject(i);
            if(jsonObject == null){
                continue;
            }
            int id = jsonObject.getInt(name_id);
            ensureSize(id + 1);
            Entry entry = super.get(id);
            entry.fromJson(jsonObject);
        }
    }
    private void fromJsonSparse(JSONArray json){
        SparseOffsetsArray offsetsArray = (SparseOffsetsArray) getOffsetArray();
        offsetsArray.setSize(0);
        int length = json.length();
        ensureSize(length);
        offsetsArray.setSize(length);
        String name_id = Entry.NAME_id;
        for(int i=0;i<length;i++){
            JSONObject jsonObject = json.optJSONObject(i);
            if(jsonObject==null){
                offsetsArray.setIdx(i , OffsetArray.NO_ENTRY);
                continue;
            }
            int id = jsonObject.getInt(name_id);
            Entry entry = super.get(i);
            offsetsArray.setIdx(i, id);
            entry.fromJson(jsonObject);
        }
    }
    public void merge(EntryArray entryArray){
        if(entryArray ==null|| entryArray == this|| entryArray.isEmpty()){
            return;
        }
        if(isSparse()){
            mergeSparse(entryArray);
        }else {
            mergeNonSparse(entryArray);
        }
        refreshCountAndStart();
    }
    private void mergeSparse(EntryArray entryArray){
        Iterator<Entry> itr = entryArray.iterator(true);
        while (itr.hasNext()){
            Entry comingBlock = itr.next();
            Entry existingBlock = getOrCreate((short) comingBlock.getId());
            existingBlock.merge(comingBlock);
        }
    }
    private void mergeNonSparse(EntryArray entryArray){
        ensureSize(entryArray.size());
        Iterator<Entry> itr = entryArray.iterator(true);
        while (itr.hasNext()){
            Entry comingBlock = itr.next();
            Entry existingBlock = super.get(comingBlock.getIndex());
            existingBlock.merge(comingBlock);
        }
    }
    private void updateHighestCount(int count){
        SpecTypePair specTypePair = getParentInstance(SpecTypePair.class);
        if(specTypePair == null){
            ensureSize(count);
            return;
        }
        int maxCount = specTypePair.getHighestEntryCount();
        if(count > maxCount){
            maxCount = count;
        }
        ensureSize(maxCount);
    }
    @Override
    public String toString(){
        return getClass().getSimpleName()+": size="+ size();
    }
}
