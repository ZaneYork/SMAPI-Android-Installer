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
package com.reandroid.dex.data;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class HiddenApiDataList extends SingleBlockContainer<BlockList<HiddenApiData>>
        implements Iterable<HiddenApiData>{

    private final BlockList<HiddenApiData> dataList;

    public HiddenApiDataList(){
        super();
        this.dataList = new BlockList<>();
        setItem(dataList);
    }
    public HiddenApiData get(int offset){
        BlockList<HiddenApiData> dataList = this.dataList;
        int size = dataList.size();
        for(int i = 0; i < size; i++){
            HiddenApiData data = dataList.get(i);
            if(offset == data.getOffset()){
                return data;
            }
        }
        return null;
    }
    @Override
    public Iterator<HiddenApiData> iterator() {
        return dataList.iterator();
    }
    HiddenApiData getLast(){
        return dataList.getLast();
    }
    private void add(HiddenApiData hiddenApiData){
        this.dataList.add(hiddenApiData);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateOffsets();
    }
    private void updateOffsets(){
        int offset = getOffsetStart();
        for(HiddenApiData apiData : this){
            apiData.setOffset(offset);
            offset += apiData.countBytes();
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HiddenApiIndexList hiddenApiIndexList = getHiddenApiList();
        Iterator<HiddenApiIndex> iterator = hiddenApiIndexList.getHiddenApis();
        while (iterator.hasNext()){
            HiddenApiIndex hiddenApiIndex = iterator.next();
            HiddenApiData hiddenApiData = new HiddenApiData();
            add(hiddenApiData);
            hiddenApiIndex.linkData(hiddenApiData);
            hiddenApiData.onReadBytes(reader);
        }
        Comparator<HiddenApiData> comparator = (data1, data2) ->
                CompareUtil.compare(data1.getOffset(), data2.getOffset());
        dataList.sort(comparator);
    }
    private int getOffsetStart(){
        HiddenApiRestrictions restrictions = getParentInstance(HiddenApiRestrictions.class);
        assert restrictions != null;
        return restrictions.getHiddenApiDataListOffset();
    }
    private HiddenApiIndexList getHiddenApiList(){
        HiddenApiRestrictions restrictions = getParentInstance(HiddenApiRestrictions.class);
        assert restrictions != null;
        return restrictions.getHiddenApiIndexList();
    }
}
