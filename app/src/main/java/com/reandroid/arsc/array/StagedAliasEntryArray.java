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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.value.StagedAliasEntry;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;

import java.io.IOException;
import java.util.Iterator;

public class StagedAliasEntryArray extends BlockArray<StagedAliasEntry>
        implements BlockLoad, JSONConvert<JSONArray> {
    private final IntegerItem count;
    public StagedAliasEntryArray(IntegerItem count){
        super();
        this.count=count;
        this.count.setBlockLoad(this);
    }
    public boolean contains(StagedAliasEntry aliasEntry){
        Iterator<StagedAliasEntry> iterator = iterator();
        while (iterator.hasNext()){
            StagedAliasEntry entry = iterator.next();
            if(entry.isEqual(aliasEntry)){
                return true;
            }
        }
        return false;
    }
    public StagedAliasEntry searchByStagedResId(int stagedResId){
        Iterator<StagedAliasEntry> iterator = iterator();
        while (iterator.hasNext()){
            StagedAliasEntry entry = iterator.next();
            if(stagedResId==entry.getStagedResId()){
                return entry;
            }
        }
        return null;
    }
    @Override
    public StagedAliasEntry[] newArrayInstance(int len) {
        return new StagedAliasEntry[len];
    }
    @Override
    protected void onRefreshed() {
        updateCount();
    }
    @Override
    public StagedAliasEntry newInstance() {
        return new StagedAliasEntry();
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==this.count){
            setSize(this.count.get());
        }
    }
    private void updateCount(){
        this.count.set(size());
    }

    @Override
    public JSONArray toJson() {
        if(size() == 0){
            return null;
        }
        Iterator<StagedAliasEntry> iterator = iterator();
        JSONArray jsonArray = new JSONArray(size());
        int i = 0;
        while (iterator.hasNext()){
            StagedAliasEntry item = iterator.next();
            jsonArray.put(i, item.toJson());
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clear();
        if(json==null){
            return;
        }
        int length = json.length();
        setSize(length);
        for(int i=0;i<length;i++){
            get(i).fromJson(json.getJSONObject(i));
        }
    }
}
