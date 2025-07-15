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

import com.reandroid.dex.data.TypeList;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.IdItemIndirectShortReference;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodId extends IdItem implements Comparable<MethodId> {

    private final IdItemIndirectReference<TypeId> defining;
    private final IdItemIndirectReference<ProtoId> proto;
    private final IndirectStringReference nameReference;

    public MethodId() {
        super(SIZE);
        this.defining = new IdItemIndirectShortReference<>(SectionType.TYPE_ID, this, 0, USAGE_METHOD);
        this.proto = new IdItemIndirectShortReference<>(SectionType.PROTO_ID, this, 2, USAGE_METHOD);
        this.nameReference = new IndirectStringReference(this, 4, StringId.USAGE_METHOD_NAME);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.singleThree(
                this,
                SingleIterator.of(defining.getItem()),
                SingleIterator.of(nameReference.getItem()),
                proto.getItem().usedIds()
        );
    }
    public String getName(){
        return nameReference.getString();
    }
    public void setName(String name){
        nameReference.setString(name);
    }
    public void setName(StringKey key){
        nameReference.setItem(key);
    }

    IndirectStringReference getNameReference(){
        return nameReference;
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
    public int getParametersCount() {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParametersCount();
        }
        return 0;
    }
    public int getParameterRegistersCount() {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParameterRegistersCount();
        }
        return 0;
    }
    public TypeId getParameter(int index) {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParameter(index);
        }
        return null;
    }
    public String[] getParameterNames(){
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getParameterNames();
        }
        return null;
    }
    public TypeList getParameterTypes(){
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getTypeList();
        }
        return null;
    }
    public ProtoId getProto(){
        return proto.getItem();
    }
    public void setProto(ProtoId protoId) {
        proto.setItem(protoId);
    }

    public String getReturnTypeName() {
        TypeKey typeKey = getReturnType();
        if(typeKey != null){
            return typeKey.getTypeName();
        }
        return null;
    }
    public TypeKey getReturnType() {
        ProtoId protoId = getProto();
        if(protoId != null){
            return protoId.getReturnType();
        }
        return null;
    }

    @Override
    public SectionType<MethodId> getSectionType(){
        return SectionType.METHOD_ID;
    }
    @Override
    public MethodKey getKey() {
        return checkKey(MethodKey.create(this));
    }
    @Override
    public void setKey(Key key){
        setKey((MethodKey) key);
    }
    public void setKey(MethodKey key){
        MethodKey old = getKey();
        if(key.equals(old)){
            return;
        }
        defining.setItem(key.getDeclaring());
        nameReference.setString(key.getName());
        proto.setItem(key.getProtoKey());
        keyChanged(old);
    }
    @Override
    public void refresh() {
        defining.refresh();
        proto.refresh();
        nameReference.refresh();
    }
    @Override
    void cacheItems(){
        defining.pullItem();
        proto.pullItem();
        nameReference.pullItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, true);
    }
    public void append(SmaliWriter writer, boolean appendDefining) throws IOException {
        if(appendDefining){
            getDefiningId().append(writer);
            writer.append("->");
        }
        writer.append(getName());
        writer.appendRequired(getProto());
    }

    @Override
    public int compareTo(MethodId methodId) {
        if(methodId == null){
            return -1;
        }
        int i = defining.compareTo(methodId.defining);
        if(i != 0){
            return i;
        }
        i = nameReference.compareTo(methodId.nameReference);
        if(i != 0){
            return i;
        }
        return proto.compareTo(methodId.proto);
    }

    @Override
    public String toString() {
        return getDefiningId() + "->" + getName() + getProto();
    }

    public static boolean equals(MethodId methodId, MethodId other) {
        return equals(false, methodId, other);
    }
    public static boolean equals(boolean ignoreClass, MethodId methodId, MethodId other) {
        if(methodId == other){
            return true;
        }
        if(methodId == null){
            return false;
        }
        if(!IndirectStringReference.equals(methodId.getNameReference(), other.getNameReference())){
            return false;
        }
        if(!ignoreClass) {
            if(!TypeId.equals(methodId.getDefiningId(), other.getDefiningId())){
                return false;
            }
        }
        return TypeList.equals(methodId.getParameterTypes(), other.getParameterTypes());
    }

    private static final int SIZE = 8;

}
