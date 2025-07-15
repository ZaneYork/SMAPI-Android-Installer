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
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SpecTypePairArray extends BlockArray<SpecTypePair>
        implements JSONConvert<JSONArray>, Comparator<SpecTypePair> {
    public SpecTypePairArray(){
        super();
    }


    public void trimConfigSizes(int resConfigSize){
        Iterator<SpecTypePair> iterator = iterator(true);
        while (iterator.hasNext()){
            iterator.next().trimConfigSizes(resConfigSize);
        }
    }
    public void sort(){
        for(SpecTypePair specTypePair:listItems()){
            specTypePair.sortTypes();
        }
        sort(this);
    }
    public void removeEmptyPairs(){
        Iterator<SpecTypePair> iterator = clonedIterator();
        while (iterator.hasNext()){
            SpecTypePair typePair = iterator.next();
            typePair.removeEmptyTypeBlocks();
            if(typePair.isEmpty()){
                super.remove(typePair);
            }
        }
    }
    public boolean isEmpty(){
        Iterator<SpecTypePair> iterator=iterator(true);
        while (iterator.hasNext()){
            SpecTypePair pair=iterator.next();
            if(!pair.isEmpty()){
                return false;
            }
        }
        return true;
    }
    public Entry getOrCreateEntry(byte typeId, short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreateTypeBlock(typeId, qualifiers);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public Entry getOrCreateEntry(byte typeId, short entryId, ResConfig resConfig){
        TypeBlock typeBlock=getOrCreateTypeBlock(typeId, resConfig);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public Entry getEntry(byte typeId, short entryId, String qualifiers){
        TypeBlock typeBlock=getTypeBlock(typeId, qualifiers);
        if(typeBlock==null){
            return null;
        }
        return typeBlock.getEntry(entryId);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, String qualifiers){
        SpecTypePair pair=getOrCreate(typeId);
        return pair.getOrCreateTypeBlock(qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, ResConfig resConfig){
        SpecTypePair pair=getOrCreate(typeId);
        return pair.getOrCreateTypeBlock(resConfig);
    }
    public TypeBlock getTypeBlock(byte typeId, String qualifiers){
        SpecTypePair pair= getSpecTypePair(typeId);
        if(pair==null){
            return null;
        }
        return pair.getTypeBlock(qualifiers);
    }
    public TypeBlock getOrCreate(byte typeId, ResConfig resConfig){
        SpecTypePair pair=getOrCreate(typeId);
        return pair.getTypeBlockArray().getOrCreate(resConfig);
    }
    public SpecTypePair getOrCreate(byte typeId){
        SpecTypePair pair = getSpecTypePair(typeId);
        if(pair!=null){
            return pair;
        }
        pair = createNext();
        pair.setTypeId(typeId);
        return pair;
    }
    public SpecTypePair getOrCreate(String typeName){
        SpecTypePair specTypePair = getSpecTypePair(typeName);
        if(specTypePair != null){
            return specTypePair;
        }
        TypeString typeString = getOrCreateTypeString(typeName);
        byte id = (byte) typeString.getId();
        specTypePair = createNext();
        specTypePair.setTypeId(id);
        return specTypePair;
    }
    public TypeBlock getOrCreateTypeBlock(String typeName, ResConfig resConfig){
        return getOrCreate(typeName).getOrCreateTypeBlock(resConfig);
    }
    public TypeBlock getOrCreateTypeBlock(String typeName, String qualifiers){
        return getOrCreate(typeName).getOrCreateTypeBlock(qualifiers);
    }
    public SpecTypePair getSpecTypePair(int typeId){
        return getSpecTypePair((byte) typeId);
    }
    public SpecTypePair getSpecTypePair(byte typeId){
        Iterator<SpecTypePair> iterator = iterator();
        while (iterator.hasNext()){
            SpecTypePair specTypePair = iterator.next();
            if(specTypePair != null && specTypePair.getTypeId() == typeId){
                return specTypePair;
            }
        }
        return null;
    }
    public SpecTypePair getSpecTypePair(String typeName){
        if(typeName == null){
            return null;
        }
        Iterator<SpecTypePair> itr = iterator(true);
        while (itr.hasNext()){
            SpecTypePair specTypePair = itr.next();
            if(specTypePair.isEqualTypeName(typeName)){
                return specTypePair;
            }
        }
        return null;
    }
    public Entry getAnyEntry(byte typeId, short entryId){
        if(typeId == 0){
            return null;
        }
        SpecTypePair specTypePair = getSpecTypePair(typeId);
        if(specTypePair != null){
            return specTypePair.getAnyEntry(entryId);
        }
        return null;
    }
    public Entry getAnyEntry(String typeName, String entryName){
        SpecTypePair specTypePair = getSpecTypePair(typeName);
        if(specTypePair != null){
            return specTypePair.getAnyEntry(entryName);
        }
        return null;
    }
    public Entry getEntry(String qualifiers, String typeName, String entryName){
        ResConfig resConfig = new ResConfig();
        resConfig.parseQualifiers(qualifiers);
        return getEntry(resConfig, typeName, entryName);
    }
    public Entry getEntry(ResConfig resConfig, String typeName, String entryName){
        SpecTypePair specTypePair = getSpecTypePair(typeName);
        if(specTypePair != null){
            return specTypePair.getEntry(resConfig, entryName);
        }
        return null;
    }
    @Override
    public SpecTypePair newInstance() {
        return new SpecTypePair();
    }
    @Override
    public SpecTypePair[] newArrayInstance(int len) {
        return new SpecTypePair[len];
    }
    @Override
    protected void onRefreshed() {

    }
    @Override
    protected void onPreRefresh(){
        validateEntryCounts();
    }

    private void validateEntryCounts(){
        Map<Byte, Integer> entryCountMap=mapHighestEntryCount();
        for(Map.Entry<Byte, Integer> entry:entryCountMap.entrySet()){
            byte id=entry.getKey();
            int count=entry.getValue();
            SpecTypePair pair= getSpecTypePair(id);
            pair.getSpecBlock().setEntryCount(count);
            pair.getTypeBlockArray().setEntryCount(count);
        }
    }
    private Map<Byte, Integer> mapHighestEntryCount(){
        Map<Byte, Integer> results=new HashMap<>();
        Iterator<SpecTypePair> iterator = iterator();
        while (iterator.hasNext()){
            SpecTypePair typePair = iterator.next();
            int count = typePair.getHighestEntryCount();
            byte id = typePair.getTypeId();
            Integer exist = results.get(id);
            if(exist == null || count > exist){
                results.put(id, count);
            }
        }
        return results;
    }
    public int getSmallestTypeId(){
        int result = 0;
        boolean firstFound = false;
        Iterator<SpecTypePair> iterator = iterator();
        while (iterator.hasNext()){
            SpecTypePair typePair = iterator.next();
            int id = typePair.getId();
            if(!firstFound){
                result = id;
            }
            firstFound = true;
            if(id < result){
                result = id;
            }
        }
        return result;
    }
    public int getHighestTypeId(){
        int result = 0;
        Iterator<SpecTypePair> iterator = iterator();
        while (iterator.hasNext()){
            SpecTypePair typePair = iterator.next();
            int id = typePair.getId();
            if(id > result){
                result = id;
            }
        }
        return result;
    }
    private TypeString getOrCreateTypeString(String typeName){
        TypeStringPool typeStringPool = getTypeStringPool();
        if(typeStringPool == null){
            return null;
        }
        TypeString typeString = typeStringPool.getString(typeName);
        if(typeString != null){
            return typeString;
        }
        int id = typeStringPool.getLastId() + 1;
        return typeStringPool.getOrCreate(id, typeName);
    }
    private TypeStringPool getTypeStringPool(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            return packageBlock.getTypeStringPool();
        }
        return null;
    }
    private PackageBlock getPackageBlock(){
        return getParentInstance(PackageBlock.class);
    }
    @Override
    public JSONArray toJson() {
        return toJson(false);
    }
    @Override
    public void fromJson(JSONArray json) {
        if(json==null){
            return;
        }
        int length = json.length();
        for (int i=0;i<length;i++){
            JSONObject jsonObject = json.getJSONObject(i);
            int id = jsonObject.getJSONObject(SpecBlock.NAME_spec)
                    .getInt(TypeBlock.NAME_id);
            SpecTypePair specTypePair = getOrCreate((byte) id);
            specTypePair.fromJson(jsonObject);
        }
    }
    public JSONArray toJson(boolean specsOnly) {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(SpecTypePair specTypePair:listItems()){
            JSONObject jsonObject = specTypePair.toJson(specsOnly);
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        return jsonArray;
    }
    public void merge(SpecTypePairArray pairArray){
        if(pairArray==null || pairArray==this){
            return;
        }
        for(SpecTypePair typePair:pairArray.listItems()){
            if(typePair.isEmpty()){
                continue;
            }
            SpecTypePair exist=getOrCreate(typePair.getTypeId());
            exist.merge(typePair);
        }
    }
    /**
     * It is allowed to have duplicate type name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public SpecTypePair searchByTypeName(String typeName){
        if(typeName == null){
            return null;
        }
        Iterator<SpecTypePair> iterator = iterator();
        while (iterator.hasNext()){
            SpecTypePair specTypePair = iterator.next();
            if(typeName.equals(specTypePair.getTypeName())){
                return specTypePair;
            }
        }
        return null;
    }
    @Override
    public int compare(SpecTypePair typePair1, SpecTypePair typePair2) {
        return typePair1.compareTo(typePair2);
    }
}
