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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class SmaliValueAnnotation extends SmaliValue implements SmaliRegion {

    private SmaliAnnotationItem value;

    public SmaliValueAnnotation(){
        super();
    }

    public SmaliAnnotationItem getValue() {
        return value;
    }
    public void setValue(SmaliAnnotationItem value) {
        this.value = value;
        if(value != null){
            value.setParent(this);
        }
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.SUB_ANNOTATION;
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ANNOTATION;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendOptional(getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        SmaliAnnotationItem annotationItem = new SmaliAnnotationItem(true);
        setValue(annotationItem);
        annotationItem.parse(reader);
    }
}
