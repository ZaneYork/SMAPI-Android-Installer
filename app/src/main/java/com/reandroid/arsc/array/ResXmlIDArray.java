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
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResXmlIDArray extends BlockArray<ResXmlID>  {

    private final HeaderBlock mHeaderBlock;
    private final Map<Integer, ResXmlID> mResIdMap;
    private boolean mUpdated;

    public ResXmlIDArray(HeaderBlock headerBlock){
        super();
        this.mHeaderBlock = headerBlock;
        this.mResIdMap = new HashMap<>();
    }
    public void addResourceId(int index, int resId){
        if(index < 0){
            return;
        }
        ensureSize(index + 1);
        ResXmlID xmlID = get(index);
        if(xmlID != null){
            xmlID.set(resId);
        }
    }
    public ResXmlID getOrCreate(int resId){
        updateIdMap();
        ResXmlID xmlID = mResIdMap.get(resId);
        if(xmlID != null){
            return xmlID;
        }
        xmlID = new ResXmlID(resId);
        add(xmlID);
        mUpdated=true;
        mResIdMap.put(resId, xmlID);
        return xmlID;
    }
    public ResXmlID getByResourceId(int resId){
        updateIdMap();
        return mResIdMap.get(resId);
    }
    public void refreshIdMap(){
        mUpdated = false;
        updateIdMap();
    }
    private void updateIdMap(){
        if(mUpdated){
            return;
        }
        mUpdated = true;
        mResIdMap.clear();
        Iterator<ResXmlID> iterator = iterator();
        while (iterator.hasNext()){
            ResXmlID xmlID = iterator.next();
            mResIdMap.put(xmlID.get(), xmlID);
        }
    }
    @Override
    public ResXmlID newInstance() {
        mUpdated = false;
        return new ResXmlID();
    }
    @Override
    public ResXmlID[] newArrayInstance(int length) {
        mUpdated = false;
        return new ResXmlID[length];
    }

    @Override
    protected void onPreRefresh() {
        sort();
        super.onPreRefresh();
    }

    @Override
    protected void onRefreshed() {
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int count = (mHeaderBlock.getChunkSize() - mHeaderBlock.getHeaderSize()) / 4;
        setSize(count);
        super.onReadBytes(reader);
        updateIdMap();
    }
    public boolean sort() {
        return sort(CompareUtil.getComparableComparator());
    }
    @Override
    public boolean sort(Comparator<? super ResXmlID> comparator) {
        boolean sorted = super.sort(comparator);
        adjustIndexes();
        trimLastItems();
        return sorted;
    }
    private void trimLastItems() {
        trimLastIf(ResXmlID::isEmpty);
    }
    private void adjustIndexes() {
        Iterator<ResXmlID> iterator = clonedIterator();
        boolean adjusted = false;
        while (iterator.hasNext()) {
            ResXmlID xmlID = iterator.next();
            StringItem stringItem = xmlID.getResXmlString();
            if(stringItem != null) {
                int index = stringItem.getIndex();
                if(index != xmlID.getIndex()) {
                    moveTo(xmlID, index);
                    adjusted = true;
                }
            }
        }
        if(adjusted) {
            getParentInstance(ResXmlStringPool.class).linkResXmlIDMapInternal();
        }
    }
}
