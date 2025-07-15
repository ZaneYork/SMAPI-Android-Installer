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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.*;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;

public abstract class Def<T extends IdItem> extends FixedDexContainerWithTool implements
        IdDefinition<T>, EditableItem, Comparable<Def<T>>, SmaliRegion, DefIndex, IdUsageIterator {

    private final SectionType<T> sectionType;
    private final Ule128Item relativeId;
    private final Ule128Item accessFlags;
    private T mDefId;
    private int mCachedIndex;
    private boolean mCachedIndexUpdated;
    private HiddenApiFlagValue hiddenApiFlagValue;
    
    public Def(int childesCount, SectionType<T> sectionType) {
        super(childesCount + 2);
        this.sectionType = sectionType;
        this.relativeId = new Ule128Item(true);
        this.accessFlags = new Ule128Item();
        addChild(0, relativeId);
        addChild(1, accessFlags);
    }

    @Override
    public Iterator<? extends Modifier> getModifiers() {
        return CombiningIterator.two(getAccessFlags(), getHiddenApiFlags());
    }
    public Iterator<HiddenApiFlag> getHiddenApiFlags(){
        HiddenApiFlagValue flagValue = getHiddenApiFlagValue();
        if(flagValue != null){
            return flagValue.getFlags();
        }
        return EmptyIterator.of();
    }
    public HiddenApiFlagValue getHiddenApiFlagValue() {
        return hiddenApiFlagValue;
    }
    public void setHiddenApiFlagValue(HiddenApiFlagValue hiddenApiFlagValue) {
        this.hiddenApiFlagValue = hiddenApiFlagValue;
    }

    public void removeSelf(){
        DefArray<Def<T>> array = getParentArray();
        if(array != null){
            array.remove(this);
        }
    }
    void onRemove(){
        mCachedIndexUpdated = true;
        mDefId = null;
        relativeId.set(0);
    }
    @Override
    public Key getKey(){
        T item = getId();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    public void setKey(Key key){
        setItem(key);
    }

    public AnnotationSet getOrCreateAnnotationSet(){
        AnnotationSet annotationSet = CollectionUtil.getFirst(getAnnotations());
        if(annotationSet != null){
            return annotationSet;
        }
        AnnotationsDirectory directory = getOrCreateUniqueAnnotationsDirectory();
        annotationSet = directory.createSectionItem(SectionType.ANNOTATION_SET);
        directory.addAnnotation(this, annotationSet);
        return annotationSet;
    }
    public void addAnnotationSet(AnnotationSet annotationSet){
        AnnotationsDirectory directory = getOrCreateUniqueAnnotationsDirectory();
        addAnnotationSet(directory, annotationSet);
    }
    void addAnnotationSet(AnnotationsDirectory directory, AnnotationSet annotationSet){
        directory.addAnnotation(this, annotationSet);
    }
    public Iterator<AnnotationSet> getAnnotations() {
        return getAnnotations(false);
    }
    public Iterator<AnnotationSet> getAnnotations(boolean skipEmpty){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null) {
            return EmptyIterator.of();
        }
        Iterator<AnnotationSet> iterator = directory.getAnnotations(this);
        if(!skipEmpty || !iterator.hasNext()) {
            return iterator;
        }
        return FilterIterator.of(iterator, annotationSet -> !annotationSet.isEmpty());
    }
    public AnnotationsDirectory getAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getAnnotationsDirectory();
        }
        return null;
    }
    public AnnotationsDirectory getOrCreateUniqueAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getOrCreateUniqueAnnotationsDirectory();
        }
        return null;
    }
    public AnnotationsDirectory getUniqueAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getUniqueAnnotationsDirectory();
        }
        return null;
    }
    public ClassId getClassId() {
        DefArray<Def<T>> array = getParentArray();
        if(array != null){
            ClassId classId = array.getClassId();
            if(classId != null){
                return classId;
            }
        }
        SectionList sectionList = getSectionList();
        if(sectionList == null || sectionList.isReading()){
            return null;
        }
        TypeKey defining = getDefining();
        if(defining == null){
            return null;
        }
        DexSectionPool<ClassId> pool = getPool(SectionType.CLASS_ID);
        if(pool == null){
            return null;
        }
        ClassId classId = pool.get(defining);
        if(classId == null) {
            return null;
        }
        ClassData classData = getClassData();
        if(classData == null){
            return null;
        }
        classData.setClassId(classId);
        return classId;
    }
    public TypeKey getDefining(){
        Key key = getKey();
        if(key != null){
            return key.getDeclaring();
        }
        return null;
    }
    public int getRelativeIdValue() {
        return relativeId.get();
    }
    @Override
    public int getAccessFlagsValue() {
        return accessFlags.get();
    }
    public void setAccessFlagsValue(int value) {
        accessFlags.set(value);
    }
    public boolean isPrivate(){
        return AccessFlag.PRIVATE.isSet(getAccessFlagsValue());
    }
    public boolean isNative(){
        return AccessFlag.NATIVE.isSet(getAccessFlagsValue());
    }
    public boolean isStatic(){
        return AccessFlag.STATIC.isSet(getAccessFlagsValue());
    }
    public boolean isFinal(){
        return AccessFlag.FINAL.isSet(getAccessFlagsValue());
    }
    public boolean isConstructor(){
        return AccessFlag.CONSTRUCTOR.isSet(getAccessFlagsValue());
    }
    public boolean isDirect(){
        return false;
    }
    @Override
    public T getId(){
        return mDefId;
    }
    void setItem(Key key) {
        T item = getId();
        if(item != null && key.equals(item.getKey())){
            return;
        }
        item = getOrCreateSection(sectionType).getOrCreate(key);
        setItem(item);
    }
    void setItem(T item) {
        this.mDefId = item;
        updateIndex();
    }

    @Override
    public int getDefinitionIndex() {
        if(!mCachedIndexUpdated){
            mCachedIndexUpdated = true;
            mCachedIndex = calculateDefinitionIndex();
        }
        return mCachedIndex;
    }
    private int calculateDefinitionIndex(){
        DefArray<?> parentArray = getParentArray();
        if(parentArray != null){
            Def<?> previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return getRelativeIdValue() + previous.getDefinitionIndex();
            }
        }
        return relativeId.get();
    }
    private int getPreviousIdIndex() {
        DefArray<?> parentArray = getParentArray();
        if(parentArray != null){
            Def<?> previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return previous.getDefinitionIndex();
            }
        }
        return 0;
    }
    private ClassData getClassData(){
        DefArray<Def<T>> array = getParentArray();
        if(array != null){
            return array.getClassData();
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private DefArray<Def<T>> getParentArray() {
        return (DefArray<Def<T>>) getParent();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItem();
    }
    private void cacheItem(){
        this.mDefId = getSectionItem(sectionType, getDefinitionIndex());
        if(this.mDefId != null){
            this.mDefId.addUsageType(IdItem.USAGE_DEFINITION);
        }
    }
    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateIndex();
    }
    private void updateIndex(){
        resetIndex();
        T item = this.mDefId;
        item = item.getReplace();
        this.mDefId = item;
        int index = getPreviousIdIndex();
        index = item.getIndex() - index;
        relativeId.set(index);
        item.addUsageType(IdItem.USAGE_DEFINITION);
    }
    void resetIndex(){
        mCachedIndexUpdated = false;
    }

    public void replaceKeys(Key search, Key replace){
        Key key = getKey();
        Key key2 = key.replaceKey(search, replace);
        if(key != key2){
            setItem(key2);
        }
    }

    @Override
    public void editInternal(Block user) {

    }

    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(getId());
    }

    public void merge(Def<?> def){
        setItem(def.getKey());
        setAccessFlagsValue(def.getAccessFlagsValue());
    }
    @Override
    public int compareTo(Def<T> other) {
        if(other == null){
            return -1;
        }
        return SectionTool.compareIdx(getId(), other.getId());
    }
}
