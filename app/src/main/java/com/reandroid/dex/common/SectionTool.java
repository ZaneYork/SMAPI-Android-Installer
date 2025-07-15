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
package com.reandroid.dex.common;

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;

import java.util.Iterator;

public interface SectionTool {

    default<T1 extends SectionItem> T1 createSectionItem(SectionType<T1> sectionType) {
        Section<T1> section = getOrCreateSection(sectionType);
        if(section != null){
            return section.createItem();
        }
        return null;
    }
    default<T1 extends SectionItem> T1[] getSectionItem(SectionType<T1> sectionType, int[] indexes){
        if(indexes == null || indexes.length == 0){
            return null;
        }
        Section<T1> section = getSection(sectionType);
        if(section == null){
            return null;
        }
        return section.getSectionItems(indexes);
    }
    default<T1 extends SectionItem> T1 getSectionItem(SectionType<T1> sectionType, Key key){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getSectionItem(key);
        }
        return null;
    }
    default<T1 extends SectionItem> T1 getSectionItem(SectionType<T1> sectionType, int i){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getSectionItem(i);
        }
        return null;
    }
    default<T1 extends SectionItem> Section<T1> getSection(SectionType<T1> sectionType){
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            return sectionList.getSection(sectionType);
        }
        return null;
    }
    default boolean isReading(){
        SectionList sectionList = getSectionList();
        return sectionList == null || sectionList.isReading();
    }
    default SectionList getSectionList(){
        return ((Block) this).getParent(SectionList.class);
    }
    default<T1 extends SectionItem> Section<T1> getOrCreateSection(SectionType<T1> sectionType){
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            return sectionList.getOrCreateSection(sectionType);
        }
        return null;
    }
    default<T1 extends SectionItem> T1 getOrCreateSectionItem(SectionType<T1> sectionType, Key key){
        if(key == null){
            return null;
        }
        Section<T1> section = getOrCreateSection(sectionType);
        if(section == null){
            return null;
        }
        DexSectionPool<T1> pool = section.getPool();
        if(pool != null){
            return pool.getOrCreate(key);
        }
        return null;
    }
    default<T1 extends SectionItem> DexSectionPool<T1> getPool(SectionType<T1> sectionType){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getPool();
        }
        return null;
    }
    default<T1 extends SectionItem> DexSectionPool<T1> getLoadedPool(SectionType<T1> sectionType){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getLoadedPool();
        }
        return null;
    }
    default<T1 extends SectionItem> DexSectionPool<T1> getOrCreatePool(SectionType<T1> sectionType){
        Section<T1> section = getOrCreateSection(sectionType);
        if(section != null){
            return section.getPool();
        }
        return null;
    }
    static<T extends SectionItem> int compareIdx(Iterator<T> iterator1, Iterator<T> iterator2){
        while (iterator1.hasNext() && iterator2.hasNext()){
            int i = compareIdx(iterator1.next(), iterator2.next());
            if(i != 0){
                return i;
            }
        }
        if(iterator1.hasNext()){
            return 1;
        }
        if(iterator2.hasNext()){
            return -1;
        }
        return 0;
    }
    static<T extends SectionItem> int compareIdx(T item1, T item2){
        if(item1 == item2){
            return 0;
        }
        if(item1 == null){
            return 1;
        }
        if(item2 == null){
            return -1;
        }
        int i1 = item1.getIdx();
        int i2 = item2.getIdx();
        if(i1 == i2){
            return 0;
        }
        if(i1 > i2){
            return 1;
        }
        return -1;
    }
    static<T extends Block> int compareIndex(T item1, T item2){
        if(item1 == item2){
            return 0;
        }
        if(item1 == null){
            return 1;
        }
        if(item2 == null){
            return -1;
        }
        int i1 = item1.getIndex();
        int i2 = item2.getIndex();
        if(i1 == i2){
            return 0;
        }
        if(i1 > i2){
            return 1;
        }
        return -1;
    }
}
