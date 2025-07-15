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

import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.DexSectionPool;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassIdSectionArray extends IdSectionArray<ClassId> {

    private boolean mDefinitionSortDisabled;

    public ClassIdSectionArray(IntegerPair countAndOffset) {
        super(countAndOffset, SectionType.CLASS_ID.getCreator());
    }

    @Override
    public boolean sort(Comparator<? super ClassId> comparator) {
        Section<ClassId> section = getParentSection();
        if(section == null){
            return false;
        }
        SectionList sectionList = section.getSectionList();
        if(sectionList == null){
            return false;
        }
        DexHeader dexHeader = sectionList.getHeader();
        if(dexHeader.isClassDefinitionOrderEnforced()){
            return sortDefinition();
        }else {
            return comparatorSort(comparator);
        }
    }

    public boolean comparatorSort(Comparator<? super ClassId> comparator) {
        return super.sort(comparator);
    }
    public boolean sortDefinition() {
        if(mDefinitionSortDisabled){
            return false;
        }
        boolean sorted = false;
        DexSectionPool<ClassId> pool = getParentSection().getPool();
        Iterator<ClassId> iterator = this.iterator();
        while (iterator.hasNext()){
            ClassId classId = iterator.next();
            if(sortDefinition(null, pool, classId)){
                sorted = true;
            }
        }
        return sorted && !mDefinitionSortDisabled;
    }
    private boolean sortDefinition(Set<TypeKey> cyclicSet, DexSectionPool<ClassId> pool, ClassId classId) {
        boolean sorted = false;
        TypeKey key = classId.getKey();
        Iterator<TypeKey> superKeys = classId.getInstanceKeys();
        while (superKeys.hasNext()){
            TypeKey typeKey = superKeys.next();
            ClassId other = pool.get(typeKey);
            if(other == null){
                continue;
            }
            int i1 = classId.getIndex();
            int i2 = other.getIndex();
            if(i1 > i2){
                continue;
            }
            if(i1 == i2){
                onCyclicInheritance(classId);
                return false;
            }
            moveTo(other, i1);
            if(cyclicSet == null){
                cyclicSet = new HashSet<>();
                cyclicSet.add(key);
            }else if(cyclicSet.contains(typeKey)){
                onCyclicInheritance(classId);
                return false;
            }
            cyclicSet.add(typeKey);
            sortDefinition(cyclicSet, pool, other);
            sorted = true;
        }
        return sorted;
    }
    private void onCyclicInheritance(ClassId classId){
        // TODO: Should throw?
        mDefinitionSortDisabled = true;
        String message = "WARN: Cyclic inheritance involving '" + classId.getKey() + "'";
        System.err.println(message);
    }
}
