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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.value.LibraryInfo;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.io.IOException;

public class LibraryInfoArray extends BlockArray<LibraryInfo> implements JSONConvert<JSONArray> {
    private final IntegerItem mInfoCount;
    public LibraryInfoArray(IntegerItem infoCount){
        this.mInfoCount=infoCount;
    }

    public boolean containsLibraryInfo(String packageName){
        for(LibraryInfo info : getChildes()){
            if(info.packageNameMatches(packageName)){
                return true;
            }
        }
        return false;
    }

    public LibraryInfo getOrCreate(int pkgId){
        LibraryInfo info=getById(pkgId);
        if(info!=null){
            return info;
        }
        int index= size();
        ensureSize(index+1);
        info=get(index);
        info.setId(pkgId);
        return info;
    }
    public LibraryInfo getById(int pkgId){
        for(LibraryInfo info:listItems()){
            if(info !=null && pkgId==info.getId()){
                return info;
            }
        }
        return null;
    }
    @Override
    public LibraryInfo newInstance() {
        return new LibraryInfo();
    }
    @Override
    public LibraryInfo[] newArrayInstance(int len) {
        return new LibraryInfo[len];
    }
    @Override
    protected void onRefreshed() {
        mInfoCount.set(size());
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setSize(mInfoCount.get());
        super.onReadBytes(reader);
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(LibraryInfo libraryInfo:listItems()){
            JSONObject jsonObject= libraryInfo.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clear();
        if(json==null){
            return;
        }
        int length= json.length();
        ensureSize(length);
        for (int i=0;i<length;i++){
            JSONObject jsonObject=json.getJSONObject(i);
            LibraryInfo libraryInfo=get(i);
            libraryInfo.fromJson(jsonObject);
        }
    }
    public void merge(LibraryInfoArray infoArray){
        if(infoArray==null||infoArray==this||infoArray.size()==0){
            return;
        }
        for(LibraryInfo info:infoArray.listItems()){
            LibraryInfo exist=getOrCreate(info.getId());
            exist.merge(info);
        }
    }
}
