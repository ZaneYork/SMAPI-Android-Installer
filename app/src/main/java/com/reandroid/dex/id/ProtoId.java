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

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.data.TypeList;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.reference.TypeListReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class ProtoId extends IdItem implements Comparable<ProtoId> {

    private final IndirectStringReference shorty;
    private final IdItemIndirectReference<TypeId> returnType;
    private final TypeListReference parameters;

    public ProtoId() {
        super(SIZE);

        this.shorty = new IndirectStringReference(this, 0, UsageMarker.USAGE_SHORTY);
        this.returnType = new IdItemIndirectReference<>(SectionType.TYPE_ID, this, 4, UsageMarker.USAGE_PROTO);
        this.parameters = new TypeListReference(this, 8, UsageMarker.USAGE_PROTO);
    }


    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.singleThree(
                this,
                SingleIterator.of(returnType.getItem()),
                getParameterIds(),
                SingleIterator.of(shorty.getItem())
        );
    }
    @Override
    public SectionType<ProtoId> getSectionType(){
        return SectionType.PROTO_ID;
    }
    @Override
    public ProtoKey getKey(){
        return checkKey(ProtoKey.create(this));
    }
    @Override
    public void setKey(Key key){
        setKey((ProtoKey) key);
    }
    public void setKey(ProtoKey key){
        ProtoKey old = getKey();
        if(key.equals(old)){
            return;
        }
        shorty.setString(key.getShorty());
        returnType.setItem(key.getReturnType());
        parameters.setItem(key.getParameterListKey());
        keyChanged(old);
    }
    public int getParameterRegistersCount(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.getParameterRegistersCount();
        }
        return 0;
    }

    public int getParametersCount(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.size();
        }
        return 0;
    }
    public TypeId getParameter(int index) {
        return parameters.get(index);
    }
    public TypeId getForRegister(int register) {
        return parameters.getForRegister(register);
    }
    public String[] getParameterNames(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.getNames();
        }
        return null;
    }
    public Iterator<TypeId> getParameterIds(){
        TypeList typeList = getTypeList();
        if(typeList != null){
            return typeList.iterator();
        }
        return EmptyIterator.of();
    }
    public TypeList getTypeList() {
        return parameters.getItem();
    }
    public TypeListReference getParametersReference(){
        return parameters;
    }
    public void setParameters(TypeListKey key){
        parameters.setItem(key);
    }
    public TypeListKey getParameters(){
        return parameters.getKey();
    }
    public TypeKey getReturnType(){
        return (TypeKey) returnType.getKey();
    }
    public TypeId getReturnTypeId(){
        return returnType.getItem();
    }
    public String getShorty(){
        return shorty.getString();
    }
    public void setShorty(StringKey key){
        shorty.setItem(key);
    }
    public void setShorty(String shortyString){
        shorty.setString(shortyString);
    }
    public void rebuildShorty(){
        shorty.setString(getKey().getShorty());
    }

    @Override
    public void refresh() {
        shorty.refresh();
        returnType.refresh();
        parameters.refresh();
    }
    @Override
    void cacheItems(){
        shorty.pullItem();
        returnType.pullItem();
        parameters.pullItem();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('(');
        Iterator<TypeId> iterator = getParameterIds();
        while (iterator.hasNext()){
            iterator.next().append(writer);
        }
        writer.append(')');
        writer.appendRequired(getReturnTypeId());
    }
    @Override
    public int compareTo(ProtoId protoId) {
        if(protoId == this){
            return 0;
        }
        if(protoId == null) {
            return -1;
        }

        int i = SectionTool.compareIdx(getReturnTypeId(), protoId.getReturnTypeId());
        if(i != 0){
            return i;
        }
        return SectionTool.compareIdx(getParameterIds(), protoId.getParameterIds());
    }

    @Override
    public String toString() {
        ProtoKey key = getKey();
        if(key != null){
            return key.toString();
        }
        return "(" + getTypeList() + ")" + getReturnTypeId();
    }

    private static final int SIZE = 12;

}
