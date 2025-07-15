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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Iterator;

public class HiddenApiIndexList extends BlockList<HiddenApiIndex> {

    public HiddenApiIndexList(){
        super();
        setCreator(new HiddenApiIndexCreator(this));
    }
    public HiddenApiFlagValue getFlagValue(Key key){
        HiddenApiIndex apiIndex = get(key.getDeclaring());
        if(apiIndex != null){
            return apiIndex.get(key);
        }
        return null;
    }
    public HiddenApiIndex get(TypeKey typeKey){
        Section<ClassId> section = getClassIdSection();
        if(section == null){
            return null;
        }
        if(section.getCount() != size()){
            sortItems();
        }
        ClassId classId = section.get(typeKey);
        if(classId == null || classId.isRemoved()){
            return null;
        }
        return get(classId.getIdx());
    }

    public Iterator<HiddenApiIndex> getHiddenApis() {
        return FilterIterator.of(iterator(), HiddenApiIndex::hasValidDataOffset);
    }

    @Override
    public int countBytes() {
        return size() * 4;
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        clearNullIndex();
        sortItems();
    }

    private void clearNullIndex() {
        removeIf(HiddenApiIndex::isNull);
    }
    private void sortItems() {
        sort(CompareUtil.getComparableComparator());
        if(ensureClassSectionSize()){
            sort(CompareUtil.getComparableComparator());
        }
    }
    private boolean ensureClassSectionSize() {
        Section<ClassId> section = getClassIdSection();
        if(section == null){
            return false;
        }
        boolean changed = false;
        for(int i = 0; i < size(); i++){
            HiddenApiIndex apiIndex = get(i);
            ClassId classId = apiIndex.getClassId();
            if(classId.getIndex() == i){
                continue;
            }
            ClassId update = section.get(i);
            HiddenApiIndex hiddenApiIndex = new HiddenApiIndex(update);
            add(i, hiddenApiIndex);
            changed = true;
            i --;
        }
        setSize(section.getCount());
        return changed;
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        setSize(getClassIdSectionCount());
        super.readChildes(reader);
    }

    private int getClassIdSectionCount(){
        Section<ClassId> section = getClassIdSection();
        if(section != null){
            return section.getCount();
        }
        return 0;
    }
    private Section<ClassId> getClassIdSection(){
        SectionList sectionList = getParentSectionList();
        if(sectionList != null){
            return sectionList.getSection(SectionType.CLASS_ID);
        }
        return null;
    }
    private SectionList getParentSectionList(){
        return getParentInstance(SectionList.class);
    }
    static class HiddenApiIndexCreator implements Creator<HiddenApiIndex> {
        private final HiddenApiIndexList hiddenApiIndexList;
        HiddenApiIndexCreator(HiddenApiIndexList hiddenApiIndexList){
            this.hiddenApiIndexList = hiddenApiIndexList;
        }
        @Override
        public HiddenApiIndex[] newArrayInstance(int length) {
            return new HiddenApiIndex[length];
        }
        @Override
        public HiddenApiIndex newInstance() {
            throw new RuntimeException("Must call newInstanceAt(index)");
        }
        @Override
        public HiddenApiIndex newInstanceAt(int index) {
            return new HiddenApiIndex(hiddenApiIndexList.getClassIdSection().get(index));
        }
    }
}
