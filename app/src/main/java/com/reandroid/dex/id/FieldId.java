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
package com.reandroid.dex.id;

import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.IdItemIndirectShortReference;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class FieldId extends IdItem implements Comparable<FieldId>{
    private final IdItemIndirectReference<TypeId> defining;
    private final IdItemIndirectReference<TypeId> fieldType;
    private final IndirectStringReference nameReference;

    public FieldId() {
        super(8);
        this.defining = new IdItemIndirectShortReference<>(SectionType.TYPE_ID, this, 0, USAGE_FIELD_CLASS);
        this.fieldType = new IdItemIndirectShortReference<>(SectionType.TYPE_ID, this, 2, USAGE_FIELD_TYPE);
        this.nameReference = new IndirectStringReference( this, 4, USAGE_FIELD_NAME);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.singleThree(
                this,
                SingleIterator.of(defining.getItem()),
                SingleIterator.of(nameReference.getItem()),
                SingleIterator.of(fieldType.getItem())
        );
    }
    public String getName() {
        return nameReference.getString();
    }
    public void setName(String name){
        this.nameReference.setString(name);
    }
    public StringId getNameId(){
        return nameReference.getItem();
    }
    IndirectStringReference getNameReference() {
        return nameReference;
    }
    public String getClassName(){
        TypeId typeId = getDefiningId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeKey getDefining(){
        return (TypeKey) defining.getKey();
    }
    public void setDefining(TypeKey typeKey){
        defining.setItem(typeKey);
    }
    public TypeId getDefiningId(){
        return defining.getItem();
    }
    public void setDefining(TypeId typeId){
        defining.setItem(typeId);
    }

    public TypeKey getFieldType(){
        return (TypeKey) fieldType.getKey();
    }
    public void setFieldType(TypeKey typeKey) {
        fieldType.setItem(typeKey);
    }
    public TypeId getFieldTypeId(){
        return fieldType.getItem();
    }
    public void setFieldType(TypeId typeId) {
        fieldType.setItem(typeId);
    }

    @Override
    public void refresh() {
        defining.refresh();
        fieldType.refresh();
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        defining.pullItem();
        fieldType.pullItem();
        nameReference.pullItem();
    }

    @Override
    public SectionType<FieldId> getSectionType(){
        return SectionType.FIELD_ID;
    }
    @Override
    public FieldKey getKey(){
        return checkKey(FieldKey.create(this));
    }

    @Override
    public void setKey(Key key){
        setKey((FieldKey) key);
    }
    public void setKey(FieldKey key){
        FieldKey old = getKey();
        if(Objects.equals(key, old)){
            return;
        }
        defining.setItem(key.getDeclaring());
        nameReference.setString(key.getName());
        fieldType.setItem(key.getType());
        keyChanged(old);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, true);
    }
    public void append(SmaliWriter writer, boolean appendDefining) throws IOException {
        if(appendDefining){
            writer.appendRequired(getDefiningId());
            writer.append("->");
        }
        writer.append(getName());
        writer.append(':');
        writer.appendRequired(getFieldTypeId());
    }

    @Override
    public int compareTo(FieldId fieldId) {
        if(fieldId == null){
            return -1;
        }
        int i = CompareUtil.compare(getDefiningId(), fieldId.getDefiningId());
        if(i != 0){
            return i;
        }
        i = CompareUtil.compare(getNameReference(), fieldId.getNameReference());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getFieldTypeId(), fieldId.getFieldTypeId());
    }
    @Override
    public String toString(){
        FieldKey key = getKey();
        if(key != null){
            return key.toString();
        }
        return getDefiningId() + "->" + getNameId() + ":" + getFieldTypeId();
    }
    public static boolean equals(FieldId fieldId1, FieldId fieldId2) {
        return equals(false, fieldId1, fieldId2);
    }
    public static boolean equals(boolean ignoreClass, FieldId fieldId1, FieldId fieldId2) {
        if(fieldId1 == fieldId2) {
            return true;
        }
        if(fieldId1 == null) {
            return false;
        }
        if(!IndirectStringReference.equals(fieldId1.getNameReference(), fieldId2.getNameReference())){
            return false;
        }
        return ignoreClass || TypeId.equals(fieldId1.getDefiningId(), fieldId2.getDefiningId());
    }
}
