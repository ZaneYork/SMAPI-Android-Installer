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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.array.ResXmlIDArray;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;

import java.util.Iterator;

public class ResXmlIDMap extends Chunk<HeaderBlock> implements Iterable<ResXmlID> {

    private final ResXmlIDArray mResXmlIDArray;

    public ResXmlIDMap() {
        super(new HeaderBlock(ChunkType.XML_RESOURCE_MAP), 1);
        this.mResXmlIDArray=new ResXmlIDArray(getHeaderBlock());
        addChild(mResXmlIDArray);
    }
    void removeSafely(ResXmlID resXmlID){
        if(resXmlID==null
                || resXmlID.getParent()==null
                || resXmlID.getIndex()<0
                || resXmlID.hasReference()){
            return;
        }
        ResXmlString xmlString = resXmlID.getResXmlString();
        if(xmlString == null
                || xmlString.getParent()==null
                || xmlString.getIndex()<0
                || xmlString.hasReference()){
            return;
        }
        ResXmlStringPool stringPool = getXmlStringPool();
        if(stringPool == null){
            return;
        }
        resXmlID.set(0);
        ResXmlIDArray idArray = getResXmlIDArray();
        idArray.remove(resXmlID);
        stringPool.removeString(xmlString);
    }
    public int size(){
        return getResXmlIDArray().size();
    }
    public ResXmlID get(int index){
        return getResXmlIDArray().get(index);
    }
    public void destroy(){
        getResXmlIDArray().clear();
    }
    public ResXmlIDArray getResXmlIDArray(){
        return mResXmlIDArray;
    }
    @Override
    public Iterator<ResXmlID> iterator(){
        return getResXmlIDArray().iterator();
    }
    public void addResourceId(int index, int resId){
        getResXmlIDArray().addResourceId(index, resId);
    }
    public ResXmlID getOrCreate(int resourceId){
        return getResXmlIDArray().getOrCreate(resourceId);
    }
    public ResXmlID getByResourceId(int resourceId){
        return getResXmlIDArray().getByResourceId(resourceId);
    }
    @Override
    protected void onChunkRefreshed() {

    }
    ResXmlStringPool getXmlStringPool(){
        ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
        if(resXmlDocument!=null){
            return resXmlDocument.getStringPool();
        }
        return null;
    }

    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        ResXmlStringPool resXmlStringPool = getXmlStringPool();
        if(resXmlStringPool != null) {
            resXmlStringPool.linkResXmlIDMapInternal();
        }
    }
}
