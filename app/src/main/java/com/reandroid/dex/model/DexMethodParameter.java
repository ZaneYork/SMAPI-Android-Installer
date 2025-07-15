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

import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Iterator;

public class DexMethodParameter extends Dex implements AnnotatedDex{

    private final DexMethod dexMethod;
    private final MethodDef.Parameter parameter;

    public DexMethodParameter(DexMethod dexMethod, MethodDef.Parameter parameter){
        this.dexMethod = dexMethod;
        this.parameter = parameter;
    }

    @Override
    public Iterator<DexAnnotation> getAnnotations(){
        return ComputeIterator.of(getParameter().getAnnotationItems(),
                annotationItem -> DexAnnotation.create(
                        DexMethodParameter.this, annotationItem));
    }
    @Override
    public Iterator<DexAnnotation> getAnnotations(TypeKey typeKey){
        return FilterIterator.of(getAnnotations(),
                dexAnnotation -> typeKey.equals(dexAnnotation.getType()));
    }
    @Override
    public DexAnnotation getAnnotation(TypeKey typeKey){
        return CollectionUtil.getFirst(getAnnotations(typeKey));
    }
    @Override
    public DexAnnotation getOrCreateAnnotation(TypeKey typeKey){
        return DexAnnotation.create(this,
                getParameter().getOrCreateAnnotationItem(typeKey));
    }
    @Override
    public DexAnnotation newAnnotation(TypeKey typeKey){
        return DexAnnotation.create(this,
                getParameter().addAnnotationItem(typeKey));
    }

    public String getDebugName(){
        return getParameter().getDebugName();
    }
    public void removeDebugName(){
        getParameter().setDebugName(null);
    }
    public void setDebugName(String name){
        getParameter().setDebugName(name);
    }
    public void clearAnnotations(){
        getParameter().clearAnnotations();
    }
    public DexClass getTypeClass(){
        return getClassRepository().getDexClass(getType());
    }
    public TypeKey getType(){
        return getParameter().getType();
    }
    public int getIndex(){
        return getParameter().getDefinitionIndex();
    }
    public DexMethod getDexMethod() {
        return dexMethod;
    }
    public MethodDef.Parameter getParameter() {
        return parameter;
    }

    @Override
    public boolean uses(Key key) {
        if(ObjectsUtil.equals(getType(), key)) {
            return true;
        }
        Iterator<DexAnnotation> iterator = getAnnotations();
        while (iterator.hasNext()){
            DexAnnotation dexAnnotation = iterator.next();
            if(dexAnnotation.uses(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexMethod().getClassRepository();
    }

    @Override
    public void removeSelf() {
        getParameter().remove();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getParameter().append(writer);
    }
    @Override
    public ElementType getElementType(){
        return ElementType.PARAMETER;
    }

    public static DexMethodParameter create(DexMethod dexMethod, MethodDef.Parameter parameter){
        if(dexMethod != null && parameter != null){
            return new DexMethodParameter(dexMethod, parameter);
        }
        return null;
    }
}
