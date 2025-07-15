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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;

public class EntryHeaderMap extends ValueHeader {
    public EntryHeaderMap(){
        super(HEADER_SIZE_COMPLEX);
        setComplex(true);
    }
    public ResourceEntry resolveParentId() {
        Entry parentEntry = getParentInstance(Entry.class);
        if(parentEntry != null){
            return parentEntry.resolve(getParentId());
        }
        return null;
    }
    public int getParentId(){
        return getInteger(getBytesInternal(), OFFSET_PARENT_ID);
    }
    public void setParentId(int parentId){
        putInteger(getBytesInternal(), OFFSET_PARENT_ID, parentId);
    }
    public int getValuesCount(){
        return getInteger(getBytesInternal(), OFFSET_VALUE_COUNT);
    }
    public void setValuesCount(int valuesCount){
        putInteger(getBytesInternal(), OFFSET_VALUE_COUNT, valuesCount);
    }

    @Override
    public void merge(ValueHeader valueHeader){
        if(valueHeader == this || !(valueHeader instanceof EntryHeaderMap)){
            return;
        }
        super.merge(valueHeader);
        EntryHeaderMap entryHeaderMap = (EntryHeaderMap) valueHeader;
        setParentId(entryHeaderMap.getParentId());
        setValuesCount(entryHeaderMap.getValuesCount());
    }

    @Override
    public void mergeWithName(ResourceMergeOption mergeOption, ValueHeader valueHeader) {
        if(valueHeader == this || !(valueHeader instanceof EntryHeaderMap)){
            return;
        }
        super.merge(valueHeader);
        EntryHeaderMap entryHeaderMap = (EntryHeaderMap) valueHeader;
        setValuesCount(entryHeaderMap.getValuesCount());

        ResourceEntry parentId = entryHeaderMap.resolveParentId();
        if(parentId == null){
            setParentId(entryHeaderMap.getParentId());
        }else {
            int id = 0;
            if(parentId.isContext(entryHeaderMap)){
                PackageBlock packageBlock = getParentInstance(PackageBlock.class);
                parentId = packageBlock.mergeWithName(mergeOption, parentId);
                if(parentId != null){
                    id = parentId.getResourceId();
                }
            }else {
                id = parentId.getResourceId();
            }
            setParentId(id);
        }
    }

    @Override
    public void toJson(JSONObject jsonObject) {
        super.toJson(jsonObject);
        jsonObject.put(NAME_is_complex, true);
        int parent_id = getParentId();
        if(parent_id!=0){
            jsonObject.put(NAME_parent_id, parent_id);
        }
    }
    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        setComplex(json.optBoolean(NAME_is_complex, true));
        setParentId(json.optInt(NAME_parent_id));
    }
    @Override
    public String toString(){
        if(isNull()){
            return "null";
        }
        StringBuilder builder=new StringBuilder();
        int byte_size = getSize();
        int read_size = readSize();
        if(byte_size!=16){
            builder.append("size=").append(byte_size);
        }
        if(byte_size!=read_size){
            builder.append(", readSize=").append(read_size);
        }
        if(isComplex()){
            builder.append(" complex");
        }
        if(isPublic()){
            builder.append(", public");
        }
        if(isWeak()){
            builder.append(", weak");
        }
        String name = getName();
        if(name!=null){
            builder.append(", name=").append(name);
        }else {
            builder.append(", key=").append(getKey());
        }
        int parentId = getParentId();
        if(parentId!=0){
            builder.append(", parentId=");
            builder.append(HexUtil.toHex8(getParentId()));
        }
        builder.append(", count=").append(getValuesCount());
        return builder.toString();
    }

    private static final short HEADER_SIZE_COMPLEX = 16;

    private static final int OFFSET_PARENT_ID = 8;
    private static final int OFFSET_VALUE_COUNT = 12;

    public static final String NAME_parent_id = "parent_id";
}
