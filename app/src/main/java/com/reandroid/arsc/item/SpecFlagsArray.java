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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.value.Entry;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.AbstractList;

public class SpecFlagsArray extends IntegerArrayBlock implements BlockLoad, JSONConvert<JSONArray> {
    private final IntegerItem entryCount;
    private AbstractList<SpecFlag> specFlagList;
    public SpecFlagsArray(IntegerItem entryCount) {
        super();
        this.entryCount = entryCount;
        this.entryCount.setBlockLoad(this);
        setBlockLoad(this);
    }
    public AbstractList<SpecFlag> listSpecFlags(){
        if(specFlagList==null){
            specFlagList = new AbstractList<SpecFlag>() {
                @Override
                public SpecFlag get(int i) {
                    return SpecFlagsArray.this.getFlag(i);
                }
                @Override
                public int size() {
                    return SpecFlagsArray.this.size();
                }
            };
        }
        return specFlagList;
    }
    public SpecFlag getFlag(int id){
        id = id & 0xffff;
        if(id >= size()){
            return null;
        }
        int offset = id * 4;
        return new SpecFlag(this, offset);
    }
    public void set(int entryId, int value){
        setFlag(entryId, value);
        refresh();
    }
    private void setFlag(int id, int flag){
        id = 0xffff & id;
        ensureArraySize(id+1);
        super.put(id, flag);
    }
    @Override
    public int get(int entryId){
        entryId = 0xffff & entryId;
        return super.get(entryId);
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==this.entryCount){
            super.setSize(entryCount.get());
        }
    }
    public void refresh(){
        entryCount.set(size());
    }

    public void merge(SpecFlagsArray specFlagsArray){
        if(specFlagsArray == null || specFlagsArray==this){
            return;
        }
        this.ensureArraySize(specFlagsArray.size());
        int[] comingValues = specFlagsArray.toArray();
        int[] existValues = this.toArray();
        for(int i=0; i<comingValues.length; i++){
            int valueComing = comingValues[i];
            if(valueComing == 0){
                continue;
            }
            int value = existValues[i] | valueComing;
            put(i, value);
        }
        refresh();
    }

    @Override
    public JSONArray toJson() {
        JSONArray jsonArray = new JSONArray();
        int[] flagValues = toArray();
        for(int i=0; i<flagValues.length; i++){
            int value = flagValues[i];
            if(value==0){
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Entry.NAME_id, i);
            jsonObject.put(SpecBlock.NAME_flag, value);
            jsonArray.put(jsonObject);
        }
        if(jsonArray.length()==0){
            return null;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        if(json==null || json.length()==0){
            setSize(0);
            refresh();
            return;
        }
        int length = json.length();
        length = length-1;
        // to minimise calling new array creation during ensureSize,
        // start from last entry
        flagFromJson(json.getJSONObject(length));

        for(int i=0;i<length;i++){
            flagFromJson(json.getJSONObject(i));
        }
        refresh();
    }
    private void flagFromJson(JSONObject jsonObject){
        int id = jsonObject.getInt(Entry.NAME_id);
        int flag = jsonObject.getInt(SpecBlock.NAME_flag);
        setFlag(id, flag);
    }
}
