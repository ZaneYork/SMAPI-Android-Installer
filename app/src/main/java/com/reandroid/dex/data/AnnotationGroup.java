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

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.DataKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class AnnotationGroup extends IntegerDataItemList<AnnotationSet> implements ModifiableKeyItem {

    private final DataKey<AnnotationGroup> mKey;

    public AnnotationGroup() {
        super(SectionType.ANNOTATION_SET, UsageMarker.USAGE_ANNOTATION, null);
        this.mKey = new DataKey<>(this);
    }

    @Override
    public DataKey<AnnotationGroup> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key){
        DataKey<AnnotationGroup> dataKey = (DataKey<AnnotationGroup>) key;
        merge(dataKey.getItem());
    }
    @Override
    public SectionType<AnnotationGroup> getSectionType() {
        return SectionType.ANNOTATION_GROUP;
    }
    @Override
    void removeNulls() {
    }
    public void replaceKeys(Key search, Key replace){
        for(AnnotationSet annotationSet : this){
            annotationSet.replaceKeys(search, replace);
        }
    }
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<AnnotationSet, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationSet element) {
                return element.usedIds();
            }
        };
    }

    public void merge(AnnotationGroup annotationGroup){
        AnnotationSet[] comingSets = annotationGroup.getItems();
        if(comingSets == null){
            return;
        }
        for(AnnotationSet comingSet : comingSets){
            if(comingSet == null){
                addNull();
                continue;
            }
            addNew(comingSet.getKey());
        }
    }
}
