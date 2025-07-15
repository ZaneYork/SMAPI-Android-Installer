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
package com.reandroid.arsc.model;

import android.text.TextUtils;

import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.common.FileChannelInputStream;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class FrameworkTable extends TableBlock {

    private String frameworkName;
    private int versionCode;
    private int mainPackageId;
    private boolean mOptimized;
    private boolean mOptimizeChecked;
    public FrameworkTable(){
        super();
    }

    public boolean isAndroid(){
        return "android".equals(getFrameworkName())
                && getMainPackageId() == 0x01;
    }

    public int getMainPackageId() {
        if(mainPackageId!=0){
            return mainPackageId;
        }
        PackageBlock packageBlock = pickOne();
        if(packageBlock!=null){
            mainPackageId = packageBlock.getId();
        }
        return mainPackageId;
    }

    @Override
    public void clear(){
        this.frameworkName = null;
        this.versionCode = 0;
        this.mainPackageId = 0;
        super.clear();
    }
    public int getVersionCode(){
        if(versionCode == 0 && isOptimized()){
            String version = loadProperty(PROP_VERSION_CODE);
            if(version!=null){
                try{
                    versionCode = Integer.parseInt(version);
                }catch (NumberFormatException ignored){
                }
            }
        }
        return versionCode;
    }
    public void setVersionCode(int value){
        versionCode = value;
        if(isOptimized()){
            writeVersionCode(value);
        }
    }
    public String getFrameworkName(){
        if(frameworkName == null){
            frameworkName = loadProperty(PROP_NAME);
        }
        if(frameworkName == null){
            PackageBlock packageBlock = pickOne();
            if(packageBlock!=null){
                String name = packageBlock.getName();
                if(!TextUtils.isEmpty(name)) frameworkName = name;
            }
        }
        return frameworkName;
    }
    public void setFrameworkName(String value){
        frameworkName = value;
        if(isOptimized()){
            writeProperty(PROP_NAME, value);
        }
    }
    public void optimize(String name, int version){
        mOptimizeChecked = true;
        mOptimized = false;
        ensureTypeBlockNonNullEntries();
        optimizeEntries();
        optimizeTableString();
        writeVersionCode(version);
        mOptimizeChecked = false;
        setFrameworkName(name);
        refresh();
    }

    private void ensureTypeBlockNonNullEntries(){
        Iterator<ResourceEntry> iterator = getResources();
        while (iterator.hasNext()){
            ensureNonNullDefaultEntry(iterator.next());
        }
    }
    private void ensureNonNullDefaultEntry(ResourceEntry resourceEntry){
        ResConfig resConfig = ResConfig.getDefault();
        Entry defEntry = resourceEntry.getOrCreate(resConfig);
        if(!defEntry.isNull()){
            return;
        }
        Entry entry = resourceEntry.any();
        if(entry == null){
            return;
        }
        defEntry.merge(entry);
        entry.setNull(true);
    }
    private void optimizeEntries(){
        removeExtraConfigEntries();
        for(PackageBlock pkg:listPackages()){
            removeEmptyBlocks(pkg);
        }
        for(PackageBlock pkg:listPackages()){
            pkg.removeEmpty();
            pkg.refresh();
        }
    }
    private void removeEmptyBlocks(PackageBlock pkg){
        SpecTypePairArray specTypePairArray = pkg.getSpecTypePairArray();
        specTypePairArray.sort();

        Iterator<SpecTypePair> iterator = specTypePairArray.clonedIterator();
        while (iterator.hasNext()){
            removeEmptyBlocks(iterator.next());
        }
    }
    private void removeEmptyBlocks(SpecTypePair specTypePair){
        TypeBlockArray typeBlockArray = specTypePair.getTypeBlockArray();
        if(typeBlockArray.size()<2){
            return;
        }
        typeBlockArray.removeEmptyBlocks();
    }
    private void optimizeTableString(){
        removeUnusedTableString();
        getStringPool().getStyleArray().clear();
        shrinkTableString();
        removeUnusedTableString();
    }
    private void removeUnusedTableString(){
        TableStringPool tableStringPool=getStringPool();
        tableStringPool.removeUnusedStrings();
        tableStringPool.refresh();
    }
    private void shrinkTableString(){
        TableStringPool tableStringPool=getStringPool();
        tableStringPool.getStringsArray().ensureSize(1);
        TableString title=tableStringPool.get(0);
        title.set(ARSCLib.getRepo());
        for(TableString tableString:tableStringPool.getStringsArray().listItems()){
            if(tableString==title){
                continue;
            }
            shrinkTableString(title, tableString);
        }
        tableStringPool.refresh();
    }
    private void shrinkTableString(TableString zero, TableString tableString){
        List<ReferenceItem> allRef = new ArrayCollection<>(tableString.getReferencedList());
        tableString.removeAllReference();
        for(ReferenceItem item:allRef){
            item.set(zero.getIndex());
        }
        zero.addReference(allRef);
    }
    private void removeExtraConfigEntries(){
        Iterator<ResourceEntry> iterator = getResources();
        while (iterator.hasNext()){
            removeExtraConfigEntries(iterator.next());
        }
    }
    private void removeExtraConfigEntries(ResourceEntry resourceEntry){
        Entry mainEntry = resourceEntry.get();
        if(mainEntry == null){
            return;
        }
        Iterator<Entry> itr = resourceEntry.iterator(true);
        while (itr.hasNext()){
            Entry entry = itr.next();
            if(entry == mainEntry){
                continue;
            }
            entry.setNull(true);
        }
    }
    private TableString writeProperty(String name, String value){
        if(!name.endsWith(":")){
            name=name+":";
        }
        if(value==null){
            value="";
        }
        if(!value.startsWith(name)){
            value=name+value;
        }
        TableString tableString=loadPropertyString(name);
        if(tableString!=null){
            tableString.set(value);
        }else {
            TableStringPool tableStringPool=getStringPool();
            tableString=tableStringPool.getOrCreate(value);
        }
        return tableString;
    }
    private String loadProperty(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableString tableString=loadPropertyString(name);
        if(tableString==null){
            return null;
        }
        String str=tableString.get().trim();
        return str.substring(name.length()).trim();
    }
    private TableString loadPropertyString(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableStringPool tableStringPool=getStringPool();
        int max=PROP_COUNT;
        for(int i=0;i<max;i++){
            TableString tableString=tableStringPool.get(i);
            if(tableString==null){
                break;
            }
            String str=tableString.get();
            if(str==null){
                continue;
            }
            str=str.trim();
            if(str.startsWith(name)){
                return tableString;
            }
        }
        return null;
    }
    public boolean isOptimized(){
        if(!mOptimizeChecked){
            mOptimizeChecked = true;
            String version = loadProperty(PROP_VERSION_CODE);
            if(version!=null){
                try{
                    int v = Integer.parseInt(version);
                    mOptimized = (v!=0);
                }catch (NumberFormatException ignored){
                }
            }
        }
        return mOptimized;
    }
    private void writeVersionCode(int value){
        writeProperty(PROP_VERSION_CODE, String.valueOf(value));
    }
    @Override
    public String toString(){
        HeaderBlock headerBlock=getHeaderBlock();
        if(headerBlock.getChunkType()!= ChunkType.TABLE){
            return super.toString();
        }
        if(!mOptimized){
            return "Unoptimized: "+super.toString();
        }
        return getFrameworkName()+'-'+getVersionCode();
    }
    public static FrameworkTable load(File file) throws IOException{
        return load(new FileChannelInputStream(file));
    }
    public static FrameworkTable load(InputStream inputStream) throws IOException{
        FrameworkTable frameworkTable=new FrameworkTable();
        frameworkTable.readBytes(inputStream);
        return frameworkTable;
    }

    private static final String PROP_NAME = "NAME";
    private static final String PROP_VERSION_CODE = "VERSION_CODE";
    private static final int PROP_COUNT=10;
}
