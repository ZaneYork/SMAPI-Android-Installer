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
package com.reandroid.dex.model;

import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.DexLayout;
import com.reandroid.dex.sections.MergeOptions;
import com.reandroid.dex.sections.SectionList;

import java.util.HashSet;
import java.util.Set;

public class DexMergeOptions implements MergeOptions {

    private final boolean relocate;
    private final Set<TypeKey> mergedSet;
    private int mergeStartDexFile;

    public DexMergeOptions(boolean relocate){
        this.relocate = relocate;
        this.mergedSet = new HashSet<>();
    }
    public DexMergeOptions(){
        this(true);
    }

    @Override
    public boolean skipMerging(ClassId classId, TypeKey typeKey) {
        return mergedSet.contains(typeKey);
    }
    @Override
    public void onDuplicate(ClassId classId) {
        if(relocate){
            classId.removeSelf();
        }
        mergedSet.add(classId.getKey());
    }
    @Override
    public void onMergeError(DexLayout dexLayout, ClassId classId, String message) {
    }
    @Override
    public void onMergeError(DexLayout dexLayout, SectionList sectionList, String message) {
    }
    @Override
    public void onDexFull(DexLayout dexLayout, ClassId classId) {
        DexFile coming = DexFile.findDexFile(classId);
        if(coming == null){
            return;
        }
        DexFile dexFile = DexFile.findDexFile(dexLayout);
        if(dexFile == null){
            return;
        }
        DexDirectory directory = dexFile.getDexDirectory();
        if(directory == null || directory == coming.getDexDirectory()){
            return;
        }
        onCreateNext(dexLayout);
    }
    @Override
    public void onMergeSuccess(ClassId classId, TypeKey key) {
        if(!relocate){
            mergedSet.add(key);
        }
    }
    @Override
    public boolean relocateClass() {
        return relocate;
    }
    @Override
    public int getMergeStartDexFile() {
        return mergeStartDexFile;
    }
    @Override
    public void setMergeStartDexFile(int mergeStartDexFile) {
        this.mergeStartDexFile = mergeStartDexFile;
    }

    @Override
    public DexLayout onCreateNext(DexLayout last) {
        DexFile dexFile = DexFile.findDexFile(last);
        if(dexFile == null){
            return null;
        }
        DexDirectory directory = dexFile.getDexDirectory();
        if(directory == null || directory.getLast() != dexFile){
            return null;
        }
        dexFile = directory.createDefault();
        setMergeStartDexFile(dexFile.getIndex());
        return dexFile.getDexLayout();
    }
}
