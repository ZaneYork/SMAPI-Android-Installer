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
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.common.EditableItem;
import com.reandroid.dex.common.IdUsageIterator;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public abstract class DefArray<T extends Def<?>> extends BlockList<T> implements
        Iterable<T>, EditableItem, SmaliFormat, IdUsageIterator {

    private final IntegerReference itemCount;

    private ClassId mClassId;

    public DefArray(IntegerReference itemCount, Creator<T> creator){
        super(creator);
        this.itemCount = itemCount;
    }

    @Override
    public void onPreRemove(T item) {
        AnnotationsDirectory directory = getUniqueAnnotationsDirectory();
        if(directory != null) {
            directory.remove(item);
        }
        resetIndex();
        super.onPreRemove(item);
    }

    @Override
    public final boolean sort(Comparator<? super T> comparator) {
        if(!needsSort(comparator)){
            sortAnnotations();
            return false;
        }
        onPreSort();
        boolean changed = super.sort(comparator);
        onPostSort();
        return changed;
    }
    void onPreSort(){
        ClassId classId = getClassId();
        if(classId != null){
            classId.getUniqueAnnotationsDirectory();
        }
        linkAnnotation();
    }
    void onPostSort(){
        resetIndex();
        sortAnnotations();
    }
    void sortAnnotations(){
    }

    public T getOrCreate(Key key) {
        T item = get(key);
        if(item != null){
            return item;
        }
        item = createNext();
        item.setKey(key);
        return item;
    }
    public T get(Key key) {
        for(T def : this){
            if(key.equals(def.getKey())){
                return def;
            }
        }
        return null;
    }

    @Override
    public T createNext() {
        T item = super.createNext();
        updateCount();
        return item;
    }

    private ClassId searchClassId() {
        ClassId classId = mClassId;
        if(classId != null){
            return classId;
        }
        Iterator<T> iterator = iterator();
        if(iterator.hasNext()){
            classId = iterator.next().getClassId();
        }
        return classId;
    }
    public ClassId getClassId() {
        return mClassId;
    }
    public void setClassId(ClassId classId) {
        if(mClassId == classId){
            return;
        }
        this.mClassId = classId;
        if(classId != null){
            linkAnnotation();
        }
    }
    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        linkAnnotation();
        boolean sorted = sort(CompareUtil.getComparatorUnchecked());
        if(!sorted){
            resetIndex();
        }
    }

    @Override
    protected void onRefreshed() {
        updateCount();
    }
    private void updateCount(){
        itemCount.set(getCount());
    }
    public ClassData getClassData(){
        return (ClassData) getParent();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setSize(itemCount.get());
        super.readChildes(reader);
    }
    private void linkAnnotation(){
        if(getCount() == 0){
            return;
        }
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return;
        }
        for(Def<?> def : this){
            directory.link(def);
        }
    }
    AnnotationsDirectory getAnnotationsDirectory(){
        ClassId classId = searchClassId();
        if(classId == null){
            return null;
        }
        return classId.getAnnotationsDirectory();
    }
    AnnotationsDirectory getUniqueAnnotationsDirectory(){
        ClassId classId = searchClassId();
        if(classId == null){
            return null;
        }
        return classId.getUniqueAnnotationsDirectory();
    }

    private void resetIndex(){
        for(T def : this){
            def.resetIndex();
        }
    }
    public void replaceKeys(Key search, Key replace){
        for(Def<?> def : this){
            def.replaceKeys(search, replace);
        }
    }

    @Override
    public void editInternal(Block user) {
        for(Def<?> def : this){
            def.editInternal(user);
        }
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<Def<?>, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(Def<?> element) {
                return element.usedIds();
            }
        };
    }
    public void merge(DefArray<?> defArray){
        int count = defArray.getCount();
        setSize(count);
        for(int i = 0; i < count; i++){
            Def<?> coming = defArray.get(i);
            get(i).merge(coming);
        }
        itemCount.set(count);
        linkAnnotation();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        for(Def<?> def : getChildes()){
            def.append(writer);
            writer.newLine();
        }
    }
}
