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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliField;
import com.reandroid.dex.value.*;
import com.reandroid.utils.NumbersUtil;

import java.io.IOException;
import java.util.Iterator;


public class StaticFieldDefArray extends FieldDefArray {

    private boolean mValuesLinked;
    private EncodedArray mLinkedArray;

    public StaticFieldDefArray(IntegerReference itemCount) {
        super(itemCount);
    }

    @Override
    public void onPreRemove(FieldDef fieldDef) {
        linkUniqueStaticValues();
        DexValueBlock<?> value = fieldDef.getLinkedStaticInitialValue();
        EncodedArray encodedArray = getStaticValues();
        if(value != null && encodedArray != null) {
            encodedArray.remove(value);
        }
        super.onPreRemove(fieldDef);
    }

    @Override
    void onPreSort(){
        super.onPreSort();
        linkUniqueStaticValues();
    }
    @Override
    void onPostSort(){
        super.onPostSort();
        sortStaticValues();
    }
    private void sortStaticValues() {
        linkUniqueStaticValues();
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray == null){
            return;
        }
        encodedArray.removeAll();
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> valueBlock = def.getLinkedStaticInitialValue();
            if(valueBlock != null){
                ensureArraySize(encodedArray, i + 1);
                encodedArray.set(i, valueBlock);
            }
        }
        encodedArray.trimNull();
    }
    private void ensureArraySize(EncodedArray encodedArray, int size){
        int arraySize = encodedArray.size();
        if(size <= arraySize){
            return;
        }
        for(int i = arraySize; i < size; i++){
            FieldDef def = get(i);
            TypeKey typeKey = def.getKey().getType();
            encodedArray.add(createFor(typeKey));
        }
    }
    void linkUniqueStaticValues() {
        ClassId classId = getClassId();
        if(classId != null){
            classId.getUniqueStaticValues();
        }
        linkStaticValues();
    }
    void linkStaticValues() {
        initStaticValues();
        EncodedArray updatedArray = getStaticValues();
        EncodedArray linkedArray = this.mLinkedArray;
        if(updatedArray == linkedArray) {
            return;
        }
        this.mLinkedArray = linkedArray;
        replacePlaceHolder(linkedArray);
        if(updatedArray != null && linkedArray != null) {
            int count = getCount();
            for(int i = 0; i < count; i++){
                FieldDef def = get(i);
                DexValueBlock<?> linkedValue = def.getLinkedStaticInitialValue();
                DexValueBlock<?> updatedValue = null;
                if(linkedValue != null) {
                    updatedValue = updatedArray.get(linkedValue.getIndex());
                }
                def.holdStaticInitialValue(updatedValue);
            }
        } else if(updatedArray != null) {
            int count = getCount();
            for(int i = 0; i < count; i++){
                FieldDef def = get(i);
                DexValueBlock<?> updatedValue = updatedArray.get(i);
                def.holdStaticInitialValue(updatedValue);
            }
        } else {
            int count = getCount();
            for(int i = 0; i < count; i++){
                FieldDef def = get(i);
                def.holdStaticInitialValue(null);
            }
        }
    }
    private void replacePlaceHolder(EncodedArray encodedArray) {
        if(encodedArray == null) {
            return;
        }
        int size = NumbersUtil.min(encodedArray.size(), size());
        for(int i = 0; i < size; i++) {
            DexValueBlock<?> value = encodedArray.get(i);
            if(value == NullValue.PLACE_HOLDER) {
                FieldDef def = get(i);
                DexValueBlock<?> replace = createFor(def.getKey().getType());
                encodedArray.set(i, replace);
            }
        }
    }
    private void initStaticValues(){
        if(mValuesLinked) {
            return;
        }
        EncodedArray encodedArray = getStaticValues();
        replacePlaceHolder(encodedArray);
        this.mLinkedArray = encodedArray;
        if(encodedArray != null) {
            int count = getCount();
            for(int i = 0; i < count; i++){
                FieldDef def = get(i);
                DexValueBlock<?> valueBlock = encodedArray.get(i);
                def.holdStaticInitialValue(valueBlock);
            }
            mValuesLinked = encodedArray.size() != 0;
        }
    }
    private EncodedArray getStaticValues(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getStaticValues();
        }
        return null;
    }
    @Override
    public void setClassId(ClassId classId) {
        if(getClassId() != classId){
            mValuesLinked = false;
        }
        super.setClassId(classId);
        linkStaticValues();
    }
    private void validateValues() {
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray == null) {
            return;
        }
        encodedArray.refresh();
        Iterator<FieldDef> iterator = this.iterator();
        Iterator<DexValueBlock<?>> valuesIterator = encodedArray.iterator();
        while (iterator.hasNext() && valuesIterator.hasNext()) {
            FieldDef fieldDef = iterator.next();
            DexValueBlock<?> value = valuesIterator.next();
            TypeKey typeKey = fieldDef.getKey().getType();
            if(value != fieldDef.getLinkedStaticInitialValue()) {
                throw new DexException("Different value: " + fieldDef);
            }
            TypeKey expected = value.getDataTypeKey();
            boolean primitive = typeKey.isPrimitive();
            if(primitive != expected.isPrimitive() || primitive && !typeKey.equals(expected)) {
                throw new DexException("Mismatch on initial value type: " + typeKey + " vs " + expected
                        + ",\n " + fieldDef.getKey() + ",\n " + fieldDef);
            }
        }
        if(valuesIterator.hasNext()) {
            FieldKey fieldKey = iterator().next().getKey();
            throw new DexException("Too many values than fields: " + size() + ", values = " + encodedArray.size()
                    + ", at " + fieldKey);
        }
    }

    @Override
    public void merge(DefArray<?> defArray) {
        super.merge(defArray);
        linkStaticValues();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.setStateWritingFields(true);
        super.append(writer);
        writer.setStateWritingFields(false);
    }

    @Override
    public void fromSmali(Iterator<SmaliField> iterator) {
        super.fromSmali(iterator);
        linkStaticValues();
        validateValues();
    }
    @Override
    public void fromSmali(SmaliField smaliField) {
        super.fromSmali(smaliField);
        linkStaticValues();
        validateValues();
    }

    private static DexValueBlock<?> createFor(TypeKey typeKey){
        DexValueBlock<?> valueBlock;
        if(!typeKey.isPrimitive()){
            valueBlock = NullValue.PLACE_HOLDER;
        }else if(TypeKey.TYPE_I.equals(typeKey)){
            valueBlock = new IntValue();
        } else if(TypeKey.TYPE_J.equals(typeKey)){
            valueBlock = new LongValue();
        } else if(TypeKey.TYPE_D.equals(typeKey)){
            valueBlock = new DoubleValue();
        } else if(TypeKey.TYPE_F.equals(typeKey)){
            valueBlock = new FloatValue();
        } else if(TypeKey.TYPE_S.equals(typeKey)){
            valueBlock = new ShortValue();
        } else if(TypeKey.TYPE_B.equals(typeKey)){
            valueBlock = new ByteValue();
        } else if(TypeKey.TYPE_C.equals(typeKey)){
            valueBlock = new CharValue();
        } else if(TypeKey.TYPE_Z.equals(typeKey)){
            valueBlock = new BooleanValue();
        }else {
            throw new IllegalArgumentException("Undefined: " + typeKey);
        }
        valueBlock.setTemporary(true);
        return valueBlock;
    }
}
