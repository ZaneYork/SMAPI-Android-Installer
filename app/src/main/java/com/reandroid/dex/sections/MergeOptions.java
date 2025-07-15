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
package com.reandroid.dex.sections;

import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.TypeKey;

public interface MergeOptions {

    boolean skipMerging(ClassId classId, TypeKey typeKey);
    void onDuplicate(ClassId classId);
    void onMergeError(DexLayout dexLayout, ClassId classId, String message);
    void onMergeError(DexLayout dexLayout, SectionList sectionList, String message);
    void onDexFull(DexLayout dexLayout, ClassId classId);
    void onMergeSuccess(ClassId classId, TypeKey key);
    boolean relocateClass();
    default int getMergeStartDexFile(){
        return 0;
    }
    default void setMergeStartDexFile(int startDexFile){
    }
    DexLayout onCreateNext(DexLayout last);
    default boolean isEmptyDexFile(DexLayout dexLayout){
        if(dexLayout == null || dexLayout.isEmpty()){
            return true;
        }
        Section<ClassId> section = dexLayout.get(SectionType.CLASS_ID);
        for(ClassId classId : section){
            if(!skipMerging(classId, classId.getKey())){
                return false;
            }
        }
        return true;
    }

    MergeOptions DEFAULT = new MergeOptions() {
        @Override
        public boolean skipMerging(ClassId classId, TypeKey typeKey) {
            return false;
        }
        @Override
        public void onDuplicate(ClassId classId) {
            classId.removeSelf();
        }
        @Override
        public void onMergeError(DexLayout dexLayout, ClassId classId, String message) {
        }
        @Override
        public void onMergeError(DexLayout dexLayout, SectionList sectionList, String message) {
        }
        @Override
        public void onDexFull(DexLayout dexLayout, ClassId classId) {
        }
        @Override
        public void onMergeSuccess(ClassId classId, TypeKey key) {
        }
        @Override
        public boolean relocateClass() {
            return true;
        }
        @Override
        public DexLayout onCreateNext(DexLayout last) {
            return null;
        }
    };

}
