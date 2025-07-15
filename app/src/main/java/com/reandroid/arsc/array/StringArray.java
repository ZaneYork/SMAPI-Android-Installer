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

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;

import java.util.Comparator;
import java.util.Iterator;

public abstract class StringArray<T extends StringItem> extends OffsetBlockArray<T> implements JSONConvert<JSONArray> {

    private boolean mUtf8;

    public StringArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart);
        this.mUtf8 = is_utf8;
    }

    public void setUtf8(boolean is_utf8){
        if(mUtf8 == is_utf8){
            return;
        }
        mUtf8 = is_utf8;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            T item = iterator.next();
            if(item != null){
                item.setUtf8(is_utf8);
            }
        }
    }
    public boolean isUtf8() {
        return mUtf8;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPreRemove(T block) {
        StringPool<T> stringPool = getParentInstance(StringPool.class);
        stringPool.onStringRemoved(block);
        block.onRemoved();
        super.onPreRemove(block);
    }

    private StyleArray getStyleArray(){
        StringPool<?> stringPool = getParentInstance(StringPool.class);
        if(stringPool != null){
            return stringPool.getStyleArray();
        }
        return ObjectsUtil.cast(null);
    }
    public void sort() {
        sort(CompareUtil.getComparableComparator());
    }
    @Override
    public boolean sort(Comparator<? super T> comparator) {
        boolean sorted = super.sort(comparator);
        getStyleArray().sort();
        return sorted;
    }
    @Override
    protected void onPreRefresh() {
        sort();
        super.onPreRefresh();
    }

    // Only styled strings
    @Override
    public JSONArray toJson() {
        return toJson(true);
    }
    public JSONArray toJson(boolean styledOnly) {
        if(size()==0){
            return null;
        }
        JSONArray jsonArray=new JSONArray();
        int i=0;
        Iterator<T> itr = iterator(true);
        while (itr.hasNext()){
            T item = itr.next();
            if(styledOnly && !item.hasStyle()){
                continue;
            }
            JSONObject jsonObject= item.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        if(i==0){
            return null;
        }
        return jsonArray;
    }
    // Only styled strings
    @Override
    public void fromJson(JSONArray json) {
        int length;
        if(json != null){
            length = json.length();
        }else {
            length = 0;
        }
        ensureSize(length);
        if(length == 0){
            return;
        }
        for(int i = 0; i < length; i++){
            JSONObject jsonObject = json.getJSONObject(i);
            StringItem stringItem = get(i);
            stringItem.fromJson(jsonObject);
        }
    }
}
