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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IndirectInteger;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.DataKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class AnnotationsDirectory extends DataItem implements ModifiableKeyItem {

    private final Header header;


    private final DirectoryMap<FieldDef, AnnotationSet> fieldsAnnotationMap;
    private final DirectoryMap<MethodDef, AnnotationSet> methodsAnnotationMap;
    private final DirectoryMap<MethodDef, AnnotationGroup> parametersAnnotationMap;

    private final DataKey<AnnotationsDirectory> mKey;

    public AnnotationsDirectory() {
        super(4);
        this.header = new Header();

        this.fieldsAnnotationMap = new DirectoryMap<>(header.fieldCount, CREATOR_FIELDS);
        this.methodsAnnotationMap = new DirectoryMap<>(header.methodCount, CREATOR_METHODS);
        this.parametersAnnotationMap = new DirectoryMap<>(header.parameterCount, CREATOR_PARAMS);

        this.mKey = new DataKey<>(this);

        addChild(0, header);

        addChild(1, fieldsAnnotationMap);
        addChild(2, methodsAnnotationMap);
        addChild(3, parametersAnnotationMap);
    }

    @Override
    public DataKey<AnnotationsDirectory> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key){
        DataKey<AnnotationsDirectory> dataKey = (DataKey<AnnotationsDirectory>) key;
        merge(dataKey.getItem());
    }

    @Override
    public SectionType<AnnotationsDirectory> getSectionType() {
        return SectionType.ANNOTATION_DIRECTORY;
    }

    public AnnotationSet getOrCreateClassAnnotations(){
        return header.classAnnotation.getOrCreate();
    }
    public AnnotationSet getClassAnnotations(){
        return header.classAnnotation.getItem();
    }
    public void setClassAnnotations(AnnotationSet annotationSet){
        header.classAnnotation.setItem(annotationSet);
    }

    public boolean isEmpty(){
        return getClassAnnotations() == null ||
                fieldsAnnotationMap.isEmpty() ||
                methodsAnnotationMap.isEmpty() ||
                parametersAnnotationMap.isEmpty();
    }
    public int countField(){
        return  fieldsAnnotationMap.getCount() ;
    }
    public int countMethod(){
        return  methodsAnnotationMap.getCount() +
                parametersAnnotationMap.getCount();
    }


    public void sortFields() {
        fieldsAnnotationMap.sort();
    }
    public void sortMethods() {
        methodsAnnotationMap.sort();
        parametersAnnotationMap.sort();
    }
    public void link(Def<?> def) {
        if(def instanceof FieldDef){
            linkField((FieldDef) def);
        }else if(def instanceof MethodDef){
            linkMethod((MethodDef) def);
        }
    }
    public void linkField(FieldDef def) {
        fieldsAnnotationMap.link(def);
    }
    public void linkMethod(MethodDef def) {
        methodsAnnotationMap.link(def);
        parametersAnnotationMap.link(def);
    }
    public void remove(Def<?> def) {
        if(def instanceof FieldDef){
            removeField((FieldDef) def);
        }else if(def instanceof MethodDef){
            removeMethod((MethodDef) def);
        }
    }
    public void removeField(FieldDef def) {
        fieldsAnnotationMap.remove(def);
    }
    public void removeMethod(MethodDef def) {
        methodsAnnotationMap.remove(def);
        parametersAnnotationMap.remove(def);
    }
    public void addAnnotation(Def<?> def, AnnotationSet annotationSet){
        if(def instanceof FieldDef){
            addFieldAnnotation((FieldDef) def, annotationSet);
        }else if(def instanceof MethodDef){
            addMethodAnnotation((MethodDef) def, annotationSet);
        }
    }
    public void addFieldAnnotation(FieldDef fieldDef, AnnotationSet annotationSet){
        fieldsAnnotationMap.add(fieldDef, ensureSameContext(annotationSet));
    }
    public void addMethodAnnotation(MethodDef methodDef, AnnotationSet annotationSet){
        methodsAnnotationMap.add(methodDef, ensureSameContext(annotationSet));
    }
    private AnnotationSet ensureSameContext(AnnotationSet annotationSet){
        if(isSameContext(annotationSet)){
            return annotationSet;
        }
        AnnotationSet mySet = getOrCreateSection(SectionType.ANNOTATION_SET)
                .createItem();
        mySet.merge(annotationSet);
        return mySet;
    }
    public Iterator<AnnotationSet> getAnnotations(Def<?> def){
        if(def.getClass() == FieldDef.class){
            return getFieldsAnnotation((FieldDef) def);
        }
        if(def.getClass() == MethodDef.class){
            return getMethodAnnotation((MethodDef) def);
        }
        throw new IllegalArgumentException("Unknown class type: " + def.getClass());
    }
    public Iterator<AnnotationSet> getFieldsAnnotation(FieldDef fieldDef){
        return fieldsAnnotationMap.getValues(fieldDef);
    }
    public Iterator<AnnotationSet> getFieldsAnnotation(int index){
        return fieldsAnnotationMap.getValues(index);
    }
    public Iterator<AnnotationSet> getMethodAnnotation(int index){
        return methodsAnnotationMap.getValues(index);
    }
    public Iterator<AnnotationSet> getMethodAnnotation(MethodDef methodDef){
        return methodsAnnotationMap.getValues(methodDef);
    }
    public Iterator<AnnotationGroup> getParameterAnnotation(MethodDef methodDef){
        return parametersAnnotationMap.getValues(methodDef);
    }
    public Iterator<DirectoryEntry<MethodDef, AnnotationGroup>> getParameterEntries(MethodDef methodDef){
        return parametersAnnotationMap.getEntries(methodDef);
    }
    public Iterator<AnnotationGroup> getParameterAnnotation(int methodIndex){
        return parametersAnnotationMap.getValues(methodIndex);
    }
    public Iterator<AnnotationSet> getParameterAnnotation(MethodDef methodDef, int parameterIndex){
        return ComputeIterator.of(getParameterAnnotation(methodDef),
                annotationGroup -> annotationGroup.getItem(parameterIndex));
    }
    public Iterator<AnnotationSet> getParameterAnnotation(int methodIndex, int parameterIndex){
        return ComputeIterator.of(getParameterAnnotation(methodIndex),
                annotationGroup -> annotationGroup.getItem(parameterIndex));
    }
    public AnnotationSet getOrCreateParameterAnnotation(MethodDef methodDef, int parameterIndex){
        AnnotationGroup annotationGroup = getOrCreateParameterAnnotationGroup(methodDef);
        return annotationGroup.getOrCreateAt(parameterIndex);
    }
    private AnnotationGroup getOrCreateParameterAnnotationGroup(MethodDef methodDef){
        AnnotationGroup annotationGroup;
        Iterator<AnnotationGroup> iterator = parametersAnnotationMap.getValues(methodDef);
        if(iterator.hasNext()){
            annotationGroup = iterator.next();
        }else {
            annotationGroup = getOrCreateSection(SectionType.ANNOTATION_GROUP).createItem();
            this.parametersAnnotationMap.add(methodDef, annotationGroup);
        }
        return annotationGroup;
    }
    public AnnotationSet createNewParameterAnnotation(MethodDef methodDef, int parameterIndex){
        AnnotationGroup annotationGroup = getEmptyParameterAnnotationGroup(methodDef, parameterIndex);
        return annotationGroup.getOrCreateAt(parameterIndex);
    }
    private AnnotationGroup getEmptyParameterAnnotationGroup(MethodDef methodDef, int parameterIndex){
        Iterator<AnnotationGroup> iterator = parametersAnnotationMap.getValues(methodDef);
        while (iterator.hasNext()){
            AnnotationGroup group = iterator.next();
            if(group.getItem(parameterIndex) == null){
                return group;
            }
        }
        AnnotationGroup annotationGroup = getOrCreateSection(SectionType.ANNOTATION_GROUP).createItem();
        parametersAnnotationMap.add(methodDef, annotationGroup);
        return annotationGroup;
    }

    public void replaceKeys(Key search, Key replace){
        AnnotationSet set = getClassAnnotations();
        if(set != null){
            set.replaceKeys(search, replace);
        }
        Iterator<AnnotationSet> iterator = fieldsAnnotationMap.getValues();
        while (iterator.hasNext()){
            iterator.next().replaceKeys(search, replace);
        }
        iterator = methodsAnnotationMap.getValues();
        while (iterator.hasNext()){
            iterator.next().replaceKeys(search, replace);
        }
        Iterator<AnnotationGroup> groupIterator = parametersAnnotationMap.getValues();
        while (groupIterator.hasNext()){
            groupIterator.next().replaceKeys(search, replace);
        }
    }

    @Override
    public void editInternal(Block user) {
        this.header.editInternal(user);
    }

    public Iterator<IdItem> usedIds(){
        AnnotationSet classAnnotation = getClassAnnotations();
        Iterator<IdItem> iterator1;
        if(classAnnotation == null){
            iterator1 = EmptyIterator.of();
        }else {
            iterator1 = classAnnotation.usedIds();
        }
        Iterator<IdItem> iterator2 = new IterableIterator<AnnotationSet, IdItem>(fieldsAnnotationMap.getValues()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationSet element) {
                return element.usedIds();
            }
        };
        Iterator<IdItem> iterator3 = new IterableIterator<AnnotationSet, IdItem>(methodsAnnotationMap.getValues()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationSet element) {
                return element.usedIds();
            }
        };
        Iterator<IdItem> iterator4 = new IterableIterator<AnnotationGroup, IdItem>(parametersAnnotationMap.getValues()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationGroup element) {
                return element.usedIds();
            }
        };
        return CombiningIterator.four(
                iterator1,
                iterator2,
                iterator3,
                iterator4
        );
    }

    @Override
    protected void onPreRefresh() {
        header.refresh();
        super.onPreRefresh();
    }

    public void merge(AnnotationsDirectory directory){
        if(directory == this){
            return;
        }
        header.merge(directory.header);
        fieldsAnnotationMap.merge(directory.fieldsAnnotationMap);
        methodsAnnotationMap.merge(directory.methodsAnnotationMap);
        parametersAnnotationMap.merge(directory.parametersAnnotationMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnnotationsDirectory directory = (AnnotationsDirectory) obj;
        return Objects.equals(header, directory.header) &&
                Objects.equals(fieldsAnnotationMap, directory.fieldsAnnotationMap) &&
                Objects.equals(methodsAnnotationMap, directory.methodsAnnotationMap) &&
                Objects.equals(parametersAnnotationMap, directory.parametersAnnotationMap);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + header.hashCode();
        hash = hash * 31 + fieldsAnnotationMap.hashCode();
        hash = hash * 31 + methodsAnnotationMap.hashCode();
        hash = hash * 31 + parametersAnnotationMap.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return  header +
                ", fields=" + fieldsAnnotationMap +
                ", methods=" + methodsAnnotationMap +
                ", parameters=" + parametersAnnotationMap;
    }

    static class Header extends SectionItem implements BlockRefresh {

        final DataItemIndirectReference<AnnotationSet> classAnnotation;
        final IndirectInteger fieldCount;
        final IndirectInteger methodCount;
        final IndirectInteger parameterCount;

        public Header() {
            super(16);

            this.classAnnotation = new DataItemIndirectReference<>(SectionType.ANNOTATION_SET,
                    this, 0, UsageMarker.USAGE_ANNOTATION);
            this.fieldCount = new IndirectInteger(this, 4);
            this.methodCount = new IndirectInteger(this, 8);
            this.parameterCount = new IndirectInteger(this, 12);
        }

        public boolean isEmpty(){
            return classAnnotation.get() == 0
                    && fieldCount.get() == 0
                    && methodCount.get() == 0
                    && parameterCount.get() == 0;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            super.onReadBytes(reader);
            cacheItems();
        }
        private void cacheItems(){
            this.classAnnotation.pullItem();
        }

        @Override
        public void refresh() {
            this.classAnnotation.refresh();
        }

        public void merge(Header header){
            classAnnotation.setItem(header.classAnnotation.getKey());
        }

        @Override
        public void editInternal(Block user) {
            // TODO: make annotation items unique
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Header header = (Header) obj;
            return Objects.equals(classAnnotation.getItem(), header.classAnnotation.getItem());
        }

        @Override
        public int hashCode() {
            int hash = 1;
            Object obj = classAnnotation.getItem();
            hash = hash * 31;
            if(obj != null){
                hash = hash + obj.hashCode();
            }
            return hash;
        }

        @Override
        public String toString() {
            return  "class=" + classAnnotation +
                    ", fields=" + fieldCount +
                    ", methods=" + methodCount +
                    ", parameters=" + parameterCount;
        }

    }
    @SuppressWarnings("unchecked")
    private static final Creator<DirectoryEntry<FieldDef, AnnotationSet>> CREATOR_FIELDS = new Creator<DirectoryEntry<FieldDef, AnnotationSet>>() {
        @Override
        public DirectoryEntry<FieldDef, AnnotationSet>[] newArrayInstance(int length) {
            return new DirectoryEntry[length];
        }
        @Override
        public DirectoryEntry<FieldDef, AnnotationSet> newInstance() {
            return new DirectoryEntry<>(SectionType.ANNOTATION_SET);
        }
    };

    @SuppressWarnings("unchecked")
    private static final Creator<DirectoryEntry<MethodDef, AnnotationSet>> CREATOR_METHODS = new Creator<DirectoryEntry<MethodDef, AnnotationSet>>() {
        @Override
        public DirectoryEntry<MethodDef, AnnotationSet>[] newArrayInstance(int length) {
            return new DirectoryEntry[length];
        }
        @Override
        public DirectoryEntry<MethodDef, AnnotationSet> newInstance() {
            return new DirectoryEntry<>(SectionType.ANNOTATION_SET);
        }
    };

    @SuppressWarnings("unchecked")
    private static final Creator<DirectoryEntry<MethodDef, AnnotationGroup>> CREATOR_PARAMS = new Creator<DirectoryEntry<MethodDef, AnnotationGroup>>() {
        @Override
        public DirectoryEntry<MethodDef, AnnotationGroup>[] newArrayInstance(int length) {
            return new DirectoryEntry[length];
        }
        @Override
        public DirectoryEntry<MethodDef, AnnotationGroup> newInstance() {
            return new DirectoryEntry<>(SectionType.ANNOTATION_GROUP);
        }
    };
}
