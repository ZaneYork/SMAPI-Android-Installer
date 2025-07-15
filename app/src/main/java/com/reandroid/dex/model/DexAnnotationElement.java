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

import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class DexAnnotationElement extends Dex {

    private final DexAnnotation dexAnnotation;
    private final AnnotationElement annotationElement;

    public DexAnnotationElement(DexAnnotation dexAnnotation, AnnotationElement annotationElement){
        super();
        this.dexAnnotation = dexAnnotation;
        this.annotationElement = annotationElement;
    }

    public String getName(){
        return getAnnotationElement().getName();
    }
    public void setName(String name){
        getAnnotationElement().setName(name);
    }

    public DexValue getValue(){
        return DexValue.create(this,
                getAnnotationElement().getValue());
    }
    public DexValue getOrCreateValue(DexValueType<?> valueType){
        return DexValue.create(this,
                getAnnotationElement().getOrCreateValue(valueType));
    }

    @Override
    public void removeSelf(){
        getAnnotationElement().removeSelf();
    }

    public AnnotationElement getAnnotationElement() {
        return annotationElement;
    }
    public DexAnnotation getDexAnnotation() {
        return dexAnnotation;
    }

    @Override
    public boolean uses(Key key) {
        return getAnnotationElement().uses(key);
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexAnnotation().getClassRepository();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getAnnotationElement().append(writer);
    }

    public static DexAnnotationElement create(DexAnnotation dexAnnotation, AnnotationElement annotationElement){
        if(dexAnnotation != null && annotationElement != null){
            return new DexAnnotationElement(dexAnnotation, annotationElement);
        }
        return null;
    }
}
