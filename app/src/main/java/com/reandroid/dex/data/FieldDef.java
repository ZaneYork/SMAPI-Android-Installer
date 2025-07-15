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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.model.SmaliField;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class FieldDef extends Def<FieldId> {

    private DexValueBlock<?> staticInitialValue;

    public FieldDef() {
        super(0, SectionType.FIELD_ID);
    }

    @Override
    public FieldKey getKey() {
        return (FieldKey) super.getKey();
    }

    public DexValueBlock<?> getStaticInitialValue() {
        return getStaticInitialValue(false);
    }
    public DexValueBlock<?> getStaticInitialValue(boolean forEditing) {
        DexValueBlock<?> valueBlock = getLinkedStaticInitialValue();
        if(valueBlock != null) {
            StaticFieldDefArray array = getParentInstance(StaticFieldDefArray.class);
            if(array != null) {
                if(forEditing) {
                    array.linkUniqueStaticValues();
                } else {
                    array.linkStaticValues();
                }
                valueBlock = getLinkedStaticInitialValue();
            }
        }
        return valueBlock;
    }
    DexValueBlock<?> getLinkedStaticInitialValue() {
        return staticInitialValue;
    }

    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getOrCreateStaticValue(DexValueType<T1> valueType){
        DexValueBlock<?> valueBlock = this.staticInitialValue;
        if(valueBlock == null || !valueBlock.is(valueType)){
            valueBlock =  getClassId().getOrCreateStaticValue(valueType, getIndex());
            holdStaticInitialValue(valueBlock);
        }
        return (T1) valueBlock;
    }

    @Override
    public Iterator<? extends Modifier> getAccessFlags(){
        return AccessFlag.valuesOfField(getAccessFlagsValue());
    }

    void holdStaticInitialValue(DexValueBlock<?> staticInitialValue) {
        this.staticInitialValue = staticInitialValue;
    }


    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();

        getSmaliDirective().append(writer);
        writer.appendModifiers(getModifiers());
        getId().append(writer, false);

        DexValueBlock<?> value = getStaticInitialValue();
        if(value != null){
            writer.append(" = ");
            value.append(writer);
        }
        Iterator<AnnotationSet> annotations = getAnnotations(true);
        if(!annotations.hasNext()){
            return;
        }
        writer.indentPlus();
        writer.appendAllWithDoubleNewLine(annotations);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(getId());
    }
    public void fromSmali(SmaliField smaliField){
        setKey(smaliField.getKey());
        setAccessFlagsValue(smaliField.getAccessFlagsValue());
        if(smaliField.hasAnnotation()){
            AnnotationSet annotationSet = getOrCreateSection(SectionType.ANNOTATION_SET).createItem();
            annotationSet.fromSmali(smaliField.getAnnotation());
            addAnnotationSet(annotationSet);
        }
        SmaliValue smaliValue = smaliField.getValue();
        if(smaliValue != null){
            getOrCreateStaticValue(smaliValue.getValueType()).fromSmali(smaliValue);
        }
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.FIELD;
    }
    @Override
    public String toString() {
        if(isReading()){
            return getSmaliDirective() + " " + getKey();
        }
        FieldId fieldId = getId();
        if(fieldId != null){
            return SmaliWriter.toStringSafe(this);
        }
        return getSmaliDirective() + " " + Modifier.toString(getAccessFlags())
                + " " + getRelativeIdValue();
    }
}
