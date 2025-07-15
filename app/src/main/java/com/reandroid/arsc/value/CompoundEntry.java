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

import com.reandroid.arsc.array.CompoundItemArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Iterator;
import java.util.List;

public abstract class CompoundEntry<ITEM extends ResValueMap, ARRAY extends CompoundItemArray<ITEM>>
        extends TableEntry<EntryHeaderMap, ARRAY> implements Iterable<ITEM>{
    public CompoundEntry(ARRAY mapArray){
        super(new EntryHeaderMap(), mapArray);
    }

    @Override
    public Iterator<ValueItem> allValues(){
        return new ComputeIterator<>(iterator(), item -> item);
    }
    @Override
    public Iterator<ITEM> iterator(){
        return getValue().iterator();
    }

    // Valid for type attr
    public AttributeDataFormat[] getAttributeTypeFormats(){
        ITEM item = getByType(AttributeType.FORMATS);
        if(item != null){
            return item.getAttributeTypeFormats();
        }
        return null;
    }
    public boolean containsType(AttributeType attributeType){
        return getValue().containsType(attributeType);
    }
    public ITEM getByType(AttributeType attributeType){
        return getValue().getByType(attributeType);
    }
    public void refresh(){
        getHeader().setValuesCount(getValue().size());
    }
    public List<ITEM> listResValueMap(){
        return getValue().getChildes();
    }

    public String decodeParentId(){
        int parentId = getParentId();
        if(parentId == 0){
            return null;
        }
        return ValueCoder.decodeReference(
                getPackageBlock(),
                ValueType.REFERENCE,
                parentId);
    }
    public int getParentId(){
        return getHeader().getParentId();
    }
    public void setParentId(int parentId){
        getHeader().setParentId(parentId);
    }
    public int childesCount(){
        return getValue().size();
    }
    public void setValuesCount(int valuesCount){
        getHeader().setValuesCount(valuesCount);
        getValue().setSize(valuesCount);
    }
    public ResourceEntry resolveParentId(){
        int id = getParentId();
        if(id == 0){
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        TableBlock tableBlock = packageBlock.getTableBlock();
        if(tableBlock == null){
            return null;
        }
        return tableBlock.getResource(packageBlock, id);
    }
    public PackageBlock getPackageBlock(){
        Entry entry = getParentEntry();
        if(entry != null){
            return entry.getPackageBlock();
        }
        return null;
    }
    @Override
    void linkTableStringsInternal(TableStringPool tableStringPool){
        for(ITEM item : listResValueMap()){
            item.linkTableStrings(tableStringPool);
        }
    }
    @Override
    void onHeaderLoaded(ARRAY value, EntryHeaderMap header){
        value.setSize(header.getValuesCount());
    }

    @Override
    void onRemoved(){
        getHeader().onRemoved();
        getValue().onRemoved();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        getHeader().toJson(jsonObject);
        jsonObject.put(NAME_values, getValue().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getHeader().fromJson(json);
        getValue().fromJson(json.optJSONArray(NAME_values));
        refresh();
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getHeader());
        List<ITEM> valueMaps = listResValueMap();
        int len = valueMaps.size();
        int max = len;
        if(max>4){
            max = 4;
        }
        for(int i=0;i<max;i++){
            builder.append("\n    ");
            builder.append(valueMaps.get(i));
        }
        if(len>0){
            if(max!=len){
                builder.append("\n    ...");
            }
            builder.append("\n   ");
        }
        return builder.toString();
    }

    public static final String NAME_values = "values";
}
