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

import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliAnnotationItem;
import com.reandroid.dex.smali.model.SmaliAnnotationSet;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationSet extends IntegerDataItemList<AnnotationItem>
        implements ModifiableKeyItem, SmaliFormat, PositionAlignedItem, FullRefresh {

    private final DataKey<AnnotationSet> mKey;

    public AnnotationSet(){
        super(SectionType.ANNOTATION_ITEM, UsageMarker.USAGE_ANNOTATION, new DexPositionAlign());
        this.mKey = new DataKey<>(this);
    }

    @Override
    public DataKey<AnnotationSet> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key){
        DataKey<AnnotationSet> dataKey = (DataKey<AnnotationSet>) key;
        merge(dataKey.getItem());
    }
    @Override
    public SectionType<AnnotationSet> getSectionType() {
        return SectionType.ANNOTATION_SET;
    }

    public DexValueBlock<?> getValue(TypeKey typeKey, String name){
        AnnotationElement element = getElement(typeKey, name);
        if(element != null){
            return element.getValue();
        }
        return null;
    }
    public AnnotationElement getElement(TypeKey typeKey, String name){
        AnnotationItem annotationItem = get(typeKey);
        if(annotationItem != null){
            return annotationItem.getElement(name);
        }
        return null;
    }
    public AnnotationItem get(TypeKey typeKey) {
        for(AnnotationItem item : this){
            if(typeKey.equals(item.getTypeKey())){
                return item;
            }
        }
        return null;
    }
    public Iterator<AnnotationItem> getAll(TypeKey typeKey) {
        return FilterIterator.of(iterator(), item -> typeKey.equals(item.getTypeKey()));
    }
    public boolean contains(String typeName) {
        for(AnnotationItem item : this){
            if(typeName.equals(item.getTypeName())){
                return true;
            }
        }
        return false;
    }
    public boolean contains(TypeKey typeKey) {
        for(AnnotationItem item : this){
            if(typeKey.equals(item.getTypeKey())){
                return true;
            }
        }
        return false;
    }
    public AnnotationItem getOrCreate(TypeKey typeKey){
        AnnotationItem item = get(typeKey);
        if(item != null){
            return item;
        }
        return addNewItem(typeKey);
    }
    public AnnotationItem getOrCreate(TypeKey type, String name){
        AnnotationItem item = get(type, name);
        if(item != null){
            return item;
        }
        return addNew(type, name);
    }
    public AnnotationItem addNewItem(TypeKey typeKey){
        AnnotationItem item = addNew();
        item.setType(typeKey);
        return item;
    }
    public AnnotationItem addNew(TypeKey type, String name){
        AnnotationItem item = addNew();
        item.setType(type);
        item.getOrCreateElement(name);
        return item;
    }
    public AnnotationItem get(TypeKey type, String name){
        for (AnnotationItem item : this) {
            if (type.equals(item.getTypeKey())
                    && item.containsName(name)) {
                return item;
            }
        }
        return null;
    }

    public void replaceKeys(Key search, Key replace){
        for(AnnotationItem annotationItem : this){
            annotationItem.replaceKeys(search, replace);
        }
    }
    @Override
    public void refreshFull(){
        sort();
    }
    public boolean sort(){
        return super.sort(CollectionUtil.getComparator());
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<AnnotationItem, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationItem element) {
                return element.usedIds();
            }
        };
    }
    public void merge(AnnotationSet annotationSet){
        if(annotationSet == this){
            return;
        }
        for(AnnotationItem coming : annotationSet){
            addNew(coming.getKey());
        }
    }
    public void fromSmali(SmaliAnnotationSet smaliAnnotationSet){
        Iterator<SmaliAnnotationItem> iterator = smaliAnnotationSet.iterator();
        while (iterator.hasNext()){
            SmaliAnnotationItem smaliAnnotationItem = iterator.next();
            AnnotationItem annotationItem = addNewItem(smaliAnnotationItem.getType());
            annotationItem.fromSmali(smaliAnnotationItem);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAllWithDoubleNewLine(iterator());
    }
    @Override
    public String toString() {
        if(getOffsetReference() == null){
            return super.toString();
        }
        int size = size();
        if(size == 0){
            return "EMPTY";
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce = false;
        for(AnnotationItem item : this){
            if(appendOnce){
                builder.append(',');
            }
            builder.append(item);
            appendOnce = true;
        }
        return builder.toString();
    }
    public static class EmptyAnnotationSet extends AnnotationSet{

        public EmptyAnnotationSet(){
            super();
            addUsageType(UsageMarker.USAGE_ANNOTATION);
        }
        @Override
        protected void onRefreshed() {
            super.onRefreshed();
            Section<AnnotationSet> section = getSection(getSectionType());
            if(section.getCount() == 1){
                addUsageType(UsageMarker.USAGE_ANNOTATION);
            }
        }

        @Override
        public void clearUsageType() {
            Section<AnnotationSet> section = getSection(getSectionType());
            if(section.getCount() != 1){
                super.clearUsageType();
            }
        }
    }
}
