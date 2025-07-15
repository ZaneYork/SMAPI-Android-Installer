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
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class TypeBlockArray extends BlockArray<TypeBlock>
        implements JSONConvert<JSONArray>, Comparator<TypeBlock> {
    private byte mTypeId;
    private Boolean mHasComplexEntry;

    public TypeBlockArray(){
        super();
    }

    public Boolean hasComplexEntry(){
        if(mHasComplexEntry != null){
            return mHasComplexEntry;
        }
        for(TypeBlock typeBlock : listItems(true)){
            Boolean hasComplex = typeBlock.getEntryArray().hasComplexEntry();
            if(hasComplex != null){
                mHasComplexEntry = hasComplex;
            }
        }
        return mHasComplexEntry;
    }
    public void destroy(){
        for(TypeBlock typeBlock:listItems()){
            if(typeBlock!=null){
                typeBlock.destroy();
            }
        }
        clear();
    }
    public void sort(){
        sort(this);
    }
    public boolean removeNullEntries(int startId){
        boolean result = true;
        for(TypeBlock typeBlock:listItems()){
            boolean removed = typeBlock.removeNullEntries(startId);
            result = result && removed;
        }
        return result;
    }
    public void removeEmptyBlocks(){
        removeIf(TypeBlock::isEmpty);
    }
    public Entry getOrCreateEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreate(qualifiers);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public Entry getOrCreateEntry(short entryId, ResConfig resConfig){
        TypeBlock typeBlock = getOrCreate(resConfig);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public boolean isEmpty(){
        for(TypeBlock typeBlock:listItems()){
            if(typeBlock!=null && !typeBlock.isEmpty()){
                return false;
            }
        }
        return true;
    }
    public Entry getEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getTypeBlock(qualifiers);
        if(typeBlock==null){
            return null;
        }
        return typeBlock.getEntry(entryId);
    }
    public Entry getEntry(ResConfig resConfig, String entryName){
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if(typeBlock != null){
            return typeBlock.getEntry(entryName);
        }
        return null;
    }
    public TypeBlock getOrCreate(ResConfig resConfig){
        return getOrCreate(resConfig, false);
    }
    public TypeBlock getOrCreate(ResConfig resConfig, boolean sparse){
        return getOrCreate(resConfig, sparse, false);
    }
    public TypeBlock getOrCreate(ResConfig resConfig, boolean sparse, boolean offset16){
        TypeBlock typeBlock = getTypeBlock(resConfig, sparse);
        if(typeBlock != null){
            return typeBlock;
        }
        byte id = getTypeId();
        typeBlock = createNext(sparse, offset16);
        typeBlock.setTypeId(id);
        ResConfig config = typeBlock.getResConfig();
        config.copyFrom(resConfig);
        return typeBlock;
    }
    public TypeBlock getOrCreate(String qualifiers){
        TypeBlock typeBlock=getTypeBlock(qualifiers);
        if(typeBlock!=null){
            return typeBlock;
        }
        int count = getHighestEntryCount();
        typeBlock = createNext();
        typeBlock.ensureEntriesCount(count);
        ResConfig config=typeBlock.getResConfig();
        config.parseQualifiers(qualifiers);
        return typeBlock;
    }
    public TypeBlock getTypeBlock(String qualifiers){
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(typeBlock.getResConfig().isEqualQualifiers(qualifiers)){
                return typeBlock;
            }
        }
        return null;
    }
    public TypeBlock getTypeBlock(ResConfig config){
        if(config == null){
            return null;
        }
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(typeBlock == null){
                continue;
            }
            if(config.equals(typeBlock.getResConfig())){
                return typeBlock;
            }
        }
        return null;
    }
    public TypeBlock getTypeBlock(ResConfig config, boolean sparse){
        if(config == null){
            return null;
        }
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(typeBlock == null || sparse != typeBlock.isSparse()){
                continue;
            }
            if(config.equals(typeBlock.getResConfig())){
                return typeBlock;
            }
        }
        return null;
    }
    public void setTypeId(byte id){
        this.mTypeId = id;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            typeBlock.setTypeId(id);
        }
    }
    public byte getTypeId(){
        SpecBlock specBlock=getSpecBlock();
        if(specBlock != null){
            byte id = specBlock.getTypeId();
            if(id != 0){
                return id;
            }
        }
        if(mTypeId != 0){
            return mTypeId;
        }
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(typeBlock == null){
                continue;
            }
            byte id = typeBlock.getTypeId();
            if(id == 0){
                continue;
            }
            if(specBlock != null){
                specBlock.setTypeId(id);
            }
            mTypeId = id;
            return id;
        }
        return 0;
    }
    public Set<ResConfig> listResConfig(){
        Set<ResConfig> unique = new HashSet<>();
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            unique.add(typeBlock.getResConfig());
        }
        return unique;
    }
    public Iterator<ResConfig> getResConfigs(){
        return new ComputeIterator<>(super.iterator(true),
                TypeBlock::getResConfig);
    }
    public Iterator<TypeBlock> iteratorNonEmpty(){
        return super.iterator(NON_EMPTY_TESTER);
    }
    public Iterator<TypeBlock> iterator(ResConfig resConfig){
        return iterator(new Predicate<TypeBlock>() {
            @Override
            public boolean test(TypeBlock typeBlock) {
                return typeBlock.getResConfig().equals(resConfig);
            }
        });
    }
    public boolean hasDuplicateResConfig(boolean ignoreEmpty){
        Set<Integer> uniqueHashSet = new HashSet<>();
        Iterator<TypeBlock> itr;
        if(ignoreEmpty){
            itr = iteratorNonEmpty();
        }else {
            itr = iterator(true);
        }
        while (itr.hasNext()){
            Integer hash = itr.next()
                    .getResConfig().hashCode();
            if(uniqueHashSet.contains(hash)){
                return true;
            }
            uniqueHashSet.add(hash);
        }
        return false;
    }
    private SpecBlock getSpecBlock(){
        SpecTypePair parent = getParent(SpecTypePair.class);
        if(parent != null){
            return parent.getSpecBlock();
        }
        return null;
    }
    @Override
    public TypeBlock newInstance() {
        byte id = getTypeId();
        TypeBlock typeBlock = new TypeBlock(false, false);
        typeBlock.setTypeId(id);
        return typeBlock;
    }
    @Override
    public TypeBlock[] newArrayInstance(int len) {
        return new TypeBlock[len];
    }
    public TypeBlock createNext(boolean sparse, boolean offset16){
        byte id = getTypeId();
        TypeBlock typeBlock = new TypeBlock(sparse, offset16);
        typeBlock.setTypeId(id);
        add(typeBlock);
        return typeBlock;
    }
    @Override
    protected void onRefreshed() {

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean readOk=true;
        while (readOk){
            readOk=readTypeBlockArray(reader);
        }
    }
    private boolean readTypeBlockArray(BlockReader reader) throws IOException{
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType!=ChunkType.TYPE){
            return false;
        }
        TypeHeader typeHeader = TypeHeader.read(reader);
        int id = getTypeId();
        if(id!=0 && typeHeader.getId().get() != id){
            return false;
        }
        int pos=reader.getPosition();
        TypeBlock typeBlock=createNext();
        typeBlock.readBytes(reader);
        return reader.getPosition()>pos;
    }
    public int getHighestEntryId(){
        int result = -1;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            int high = typeBlock.getEntryArray().getHighestEntryId();
            if(high > result){
                result = high;
            }
        }
        return result;
    }
    public int getHighestEntryCount(){
        int result = 0;
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            int count = typeBlock.getEntryArray().size();
            if(count > result){
                result = count;
            }
        }
        return result;
    }
    public void setEntryCount(int count){
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            if(!typeBlock.isSparse()){
                typeBlock.setEntryCount(count);
            }
        }
    }
    public TypeString getTypeString(){
        Iterator<TypeBlock> iterator = iterator();
        while (iterator.hasNext()){
            TypeBlock typeBlock = iterator.next();
            TypeString typeString = typeBlock.getTypeString();
            if(typeString != null){
                return typeString;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(TypeBlock typeBlock:listItems()){
            JSONObject jsonObject= typeBlock.toJson();
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
        if(json == null){
            return;
        }
        int length = json.length();
        for(int i = 0; i < length; i++){
            JSONObject jsonObject = json.getJSONObject(i);
            TypeBlock typeBlock = createNext(
                    jsonObject.optBoolean(TypeBlock.NAME_is_sparse, false),
                    jsonObject.optBoolean(TypeBlock.NAME_is_offset16, false));
            typeBlock.fromJson(jsonObject);
        }
    }
    public void merge(TypeBlockArray typeBlockArray){
        if(typeBlockArray == null || typeBlockArray == this){
            return;
        }
        for(TypeBlock typeBlock:typeBlockArray.listItems()){
            TypeBlock exist = getOrCreate(
                    typeBlock.getResConfig(), typeBlock.isSparse());
            exist.merge(typeBlock);
        }
    }
    @Override
    public int compare(TypeBlock typeBlock1, TypeBlock typeBlock2) {
        return typeBlock1.compareTo(typeBlock2);
    }

    private static final Predicate<TypeBlock> NON_EMPTY_TESTER = new Predicate<TypeBlock>() {
        @Override
        public boolean test(TypeBlock typeBlock) {
            if(typeBlock == null || typeBlock.isNull()){
                return false;
            }
            return !typeBlock.isEmpty();
        }
    };
}
