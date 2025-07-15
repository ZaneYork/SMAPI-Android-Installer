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
package com.reandroid.apk;

import com.reandroid.archive.PathTree;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.archive.InputSource;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.util.*;

public class PathMap implements JSONConvert<JSONArray> {
    private final Object mLock = new Object();
    private final Map<String, String> mNameAliasMap;
    private final Map<String, String> mAliasNameMap;

    public PathMap(){
        this.mNameAliasMap = new HashMap<>();
        this.mAliasNameMap = new HashMap<>();
    }

    public void restore(ApkModule apkModule){
        if(apkModule.getLoadedTableBlock() != null){
            restoreResFile(apkModule.listResFiles());
        }
        restore(apkModule.getInputSources());
    }
    public void restoreResFile(Collection<ResFile> files){
        if(files == null){
            return;
        }
        for(ResFile resFile:files){
            restoreResFile(resFile);
        }
    }
    public void restoreResFile(ResFile resFile){
        InputSource inputSource = resFile.getInputSource();
        String alias = restore(inputSource);
        if(alias==null){
            return;
        }
        resFile.setFilePath(alias);
    }
    private void restore(InputSource[] sources){
        for(InputSource inputSource:sources){
            restore(inputSource);
        }
    }
    public String restore(InputSource inputSource){
        if(inputSource==null){
            return null;
        }
        String name = inputSource.getName();
        String alias = getName(name);
        if(alias==null){
            name = inputSource.getAlias();
            alias = getName(name);
        }
        if(alias==null || alias.equals(inputSource.getAlias())){
            return null;
        }
        inputSource.setAlias(alias);
        return alias;
    }

    public String getAlias(String name){
        synchronized (mLock){
            return mNameAliasMap.get(name);
        }
    }
    public String getName(String alias){
        synchronized (mLock){
            return mAliasNameMap.get(alias);
        }
    }
    public int size(){
        synchronized (mLock){
            return mNameAliasMap.size();
        }
    }
    public void clear(){
        synchronized (mLock){
            mNameAliasMap.clear();
            mAliasNameMap.clear();
        }
    }
    public void add(ZipEntryMap zipEntryMap){
        if(zipEntryMap == null){
            return;
        }
        add(zipEntryMap.toArray());
    }
    public void add(InputSource[] sources){
        if(sources == null){
            return;
        }
        for(InputSource inputSource:sources){
            add(inputSource);
        }
    }
    public void add(InputSource inputSource){
        if(inputSource==null){
            return;
        }
        add(inputSource.getName(), inputSource.getAlias());
    }
    public void add(String name, String alias){
        if(name==null || alias==null){
            return;
        }
        if(name.equals(alias)){
            return;
        }
        synchronized (mLock){
            mNameAliasMap.remove(name);
            mNameAliasMap.put(name, alias);
            mAliasNameMap.remove(alias);
            mAliasNameMap.put(alias, name);
        }
    }

    private void add(JSONObject json){
        if(json==null){
            return;
        }
        add(json.optString(NAME_name), json.optString(NAME_alias));
    }

    @Override
    public JSONArray toJson() {
        JSONArray jsonArray = new JSONArray();
        Map<String, String> nameMap = this.mNameAliasMap;
        Iterator<String> nameList = PathTree.sortPaths(nameMap.keySet().iterator());
        while (nameList.hasNext()) {
            String name = nameList.next();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NAME_name, name);
            jsonObject.put(NAME_alias, nameMap.get(name));
            jsonArray.put(jsonObject);
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
        for(int i=0;i<length;i++){
            add(json.optJSONObject(i));
        }
    }
    @Override
    public String toString(){
        return "PathMap size="+size();
    }

    public static final String NAME_name = "name";
    public static final String NAME_alias = "alias";
    public static final String JSON_FILE = "path-map.json";
}
