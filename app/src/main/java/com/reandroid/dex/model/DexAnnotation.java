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
package com.reandroid.dex.model;

import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;

public class DexAnnotation extends Dex implements Iterable<DexAnnotationElement>{

    private final Dex declaring;
    private final AnnotationItem annotationItem;

    public DexAnnotation(Dex declaring, AnnotationItem annotationItem){
        super();
        this.declaring = declaring;
        this.annotationItem = annotationItem;
    }

    public TypeKey getType(){
        return getAnnotationItem().getTypeKey();
    }
    public void setType(TypeKey typeKey){
        getAnnotationItem().setType(typeKey);
    }

    @Override
    public boolean uses(Key key) {
        return getAnnotationItem().uses(key);
    }

    public boolean contains(String name){
        return getAnnotationItem().containsName(name);
    }
    public DexAnnotationElement get(String name){
        return DexAnnotationElement.create(this,
                getAnnotationItem().getElement(name));
    }
    public DexAnnotationElement get(int index){
        return DexAnnotationElement.create(this,
                getAnnotationItem().getElement(index));
    }
    public int size(){
        return getAnnotationItem().getElementsCount();
    }
    @Override
    public Iterator<DexAnnotationElement> iterator() {
        return ComputeIterator.of(
                getAnnotationItem().iterator(),
                element -> DexAnnotationElement.create(
                        DexAnnotation.this, element)
        );
    }
    public Iterator<DexAnnotationElement> clonedIterator() {
        return ComputeIterator.of(
                getAnnotationItem().clonedIterator(),
                element -> DexAnnotationElement.create(
                        DexAnnotation.this, element)
        );
    }
    public AnnotationVisibility getVisibility(){
        return getAnnotationItem().getVisibility();
    }
    public void setVisibility(AnnotationVisibility visibility){
        getAnnotationItem().setVisibility(visibility);
    }
    public DexAnnotationElement getOrCreate(String name){
        return DexAnnotationElement.create(this,
                getAnnotationItem().getOrCreateElement(name));
    }
    @Override
    public void removeSelf(){
        getAnnotationItem().removeSelf();
    }

    public AnnotationItem getAnnotationItem() {
        return annotationItem;
    }
    public Dex getDeclaring() {
        return declaring;
    }
    @Override
    public DexClassRepository getClassRepository() {
        return getDeclaring().getClassRepository();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getAnnotationItem().append(writer);
    }

    public static DexAnnotation create(Dex declaring, AnnotationItem annotationItem){
        if(declaring != null && annotationItem != null){
            return new DexAnnotation(declaring, annotationItem);
        }
        return null;
    }
}
