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
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PackageArray extends BlockArray<PackageBlock>
        implements BlockLoad, JSONConvert<JSONArray>, Comparator<PackageBlock> {
    private final IntegerItem mPackageCount;
    public PackageArray(IntegerItem packageCount){
        this.mPackageCount=packageCount;
        mPackageCount.setBlockLoad(this);
    }
    public void destroy(){
        Iterator<PackageBlock> itr = iterator(true);
        while (itr.hasNext()){
            PackageBlock packageBlock=itr.next();
            packageBlock.destroy();
        }
        clear();
    }
    public PackageBlock pickOne(){
        return pickOne(getChildes(), 0);
    }
    public PackageBlock pickOne(int packageId){
        return pickOne(getChildes(), packageId);
    }
    private PackageBlock pickOne(List<PackageBlock> items, int packageId){
        if(items==null || items.size()==0){
            return null;
        }
        if(items.size() == 1 && packageId==0){
            return items.get(0);
        }
        PackageBlock largest=null;
        for(PackageBlock packageBlock:items){
            if(packageBlock == null){
                continue;
            }
            if(packageId !=0 && packageId != packageBlock.getId()){
                continue;
            }
            if(largest == null){
                largest = packageBlock;
            }else if(packageBlock.getHeaderBlock().getChunkSize() >
                    largest.getHeaderBlock().getChunkSize()){
                largest = packageBlock;
            }
        }
        return largest;
    }
    public void sort(){
        for(PackageBlock packageBlock:listItems()){
            packageBlock.sortTypes();
        }
        sort(this);
    }
    public PackageBlock getOrCreate(byte pkgId){
        return getOrCreate(0xff & pkgId);
    }
    public PackageBlock getOrCreate(int pkgId){
        PackageBlock packageBlock = getPackageBlockById(pkgId);
        if(packageBlock != null){
            return packageBlock;
        }
        packageBlock = createNext();
        packageBlock.setId(pkgId);
        packageBlock.setName("PACKAGE NAME");
        return packageBlock;
    }
    public PackageBlock getPackageBlockById(int pkgId){
        Iterator<PackageBlock> itr=iterator(true);
        while (itr.hasNext()){
            PackageBlock packageBlock=itr.next();
            if(packageBlock.getId()==pkgId){
                return packageBlock;
            }
        }
        return null;
    }
    public PackageBlock getPackageBlockByName(String name){
        Iterator<PackageBlock> itr=iterator(true);
        while (itr.hasNext()){
            PackageBlock packageBlock = itr.next();
            if(Objects.equals(name, packageBlock.getName())){
                return packageBlock;
            }
        }
        return null;
    }
    @Override
    public PackageBlock newInstance() {
        return new PackageBlock();
    }

    @Override
    public PackageBlock[] newArrayInstance(int len) {
        return new PackageBlock[len];
    }

    @Override
    protected void onRefreshed() {
        refreshPackageCount();
    }
    private void refreshPackageCount(){
        mPackageCount.set(size());
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender != mPackageCount){
            return;
        }
        setSize(mPackageCount.get());
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(PackageBlock packageBlock:listItems()){
            JSONObject jsonObject= packageBlock.toJson();
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
        int length= json.length();
        clear();
        ensureSize(length);
        for (int i=0;i<length;i++){
            JSONObject jsonObject=json.getJSONObject(i);
            PackageBlock packageBlock=get(i);
            packageBlock.fromJson(jsonObject);
        }
    }
    public void merge(PackageArray packageArray){
        if(packageArray==null||packageArray==this){
            return;
        }
        for(PackageBlock packageBlock:packageArray.listItems()){
            PackageBlock exist=getOrCreate(packageBlock.getId());
            exist.merge(packageBlock);
        }
    }
    @Override
    public int compare(PackageBlock p1, PackageBlock p2) {
        return p1.compareTo(p2);
    }
}
