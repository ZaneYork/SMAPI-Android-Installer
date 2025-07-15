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

import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.SingleIterator;
import java.util.Iterator;

public class ResTableEntry extends TableEntry<EntryHeader, ResValue> {
    public ResTableEntry() {
        super(new EntryHeader(), new ResValue());
    }

    @Override
    public Iterator<ValueItem> allValues(){
        return new SingleIterator<>(getValue());
    }
    @Override
    void linkTableStringsInternal(TableStringPool tableStringPool){
        getValue().linkTableStrings(tableStringPool);
    }
    @Override
    void onRemoved(){
        getHeader().onRemoved();
        getValue().onRemoved();
    }
    @Override
    boolean canMerge(TableEntry<?, ?> tableEntry){
        if(tableEntry == this || !(tableEntry instanceof ResTableEntry)){
            return false;
        }
        ResValue coming = ((ResTableEntry) tableEntry).getValue();
        ValueType valueType = coming.getValueType();
        if(valueType == null || valueType == ValueType.NULL){
            return false;
        }
        valueType = getValue().getValueType();
        return valueType == null || valueType == ValueType.NULL;
    }
    @Override
    public void merge(TableEntry<?, ?> tableEntry){
        if(tableEntry == this || !(tableEntry instanceof ResTableEntry)){
            return;
        }
        getHeader().merge(tableEntry.getHeader());
        getValue().merge((ValueItem) tableEntry.getValue());
    }
    @Override
    public void mergeWithName(ResourceMergeOption mergeOption, TableEntry<?, ?> tableEntry) {
        if(tableEntry == this || !(tableEntry instanceof ResTableEntry)){
            return;
        }
        getHeader().mergeWithName(mergeOption, tableEntry.getHeader());
        getValue().mergeWithName(mergeOption, (ValueItem) tableEntry.getValue());
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        getHeader().toJson(jsonObject);
        jsonObject.put(NAME_value, getValue().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getHeader().fromJson(json);
        JSONObject jsonObject = json.getJSONObject(NAME_value);
        getValue().fromJson(jsonObject);
    }

    public static final String NAME_value = "value";
}
