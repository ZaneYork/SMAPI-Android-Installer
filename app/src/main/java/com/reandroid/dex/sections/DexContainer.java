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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.key.Key;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Iterator;

/**
 * Unfinished work, this is for multi-dex layout as noticed in dex version 41
 * */

public class DexContainer extends BlockList<DexLayout> implements Iterable<DexLayout> {

    private DexLayout current;
    public DexContainer(){
        super();
        this.current = new DexLayout();
        add(current);
    }
    public DexLayout getDexLayout(){
        return current;
    }

    public <T1 extends SectionItem> Iterator<T1> getSectionItems(SectionType<T1> sectionType, Key key){
        return ComputeIterator.of(getSectionLists(),
                sectionList -> sectionList.getSectionItem(sectionType, key));
    }
    public <T1 extends SectionItem> Iterator<Section<T1>> getSections(SectionType<T1> sectionType){
        return ComputeIterator.of(getSectionLists(), sectionList -> sectionList.getSection(sectionType));
    }
    public <T1 extends SectionItem> Section<T1> getSection(SectionType<T1> sectionType){
        Iterator<SectionList> iterator = getSectionLists();
        while (iterator.hasNext()){
            Section<T1> section = iterator.next().getSection(sectionType);
            if(section != null){
                return section;
            }
        }
        return null;
    }
    public Iterator<SectionList> getSectionLists(){
        return ComputeIterator.of(iterator(), DexLayout::getSectionList);
    }
}
