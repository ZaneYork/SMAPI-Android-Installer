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

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.*;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;

public class TypeList extends ShortList implements ModifiableKeyItem,
        SmaliFormat, Iterable<TypeId>, Comparable<TypeList> {

    private TypeId[] typeIds;

    public TypeList() {
        super();
    }

    @Override
    public TypeListKey getKey(){
        return checkKey(TypeListKey.create(this));
    }
    @Override
    public void setKey(Key key){
        setKey((TypeListKey) key);
    }
    public void setKey(TypeListKey key){
        TypeListKey old = getKey();
        if(key.equals(old)){
            return;
        }
        String[] names = key.getParameterNames();;
        if(names == null){
            setSize(0);
            return;
        }
        setSize(0);
        DexSectionPool<TypeId> pool = getOrCreatePool(SectionType.TYPE_ID);
        for(String name : names){
            TypeId typeId = pool.getOrCreate(TypeKey.create(name));
            add(typeId);
        }
        keyChanged(old);
    }

    @Override
    public SectionType<TypeList> getSectionType() {
        return SectionType.TYPE_LIST;
    }
    public void addAll(Iterator<String> iterator) {
        while (iterator.hasNext()){
            add(iterator.next());
        }
    }
    public void add(Iterator<TypeKey> iterator) {
        while (iterator.hasNext()){
            add(iterator.next());
        }
    }
    public void add(String typeName) {
        add(TypeKey.create(typeName));
    }
    public void add(TypeKey typeKey) {
        if(typeKey != null){
            TypeId typeId = getOrCreateSectionItem(SectionType.TYPE_ID, typeKey);
            add(typeId);
        }
    }
    public void add(TypeId typeId){
        if(typeId != null) {
            add(typeId.getIdx());
        }else {
            add(0);
        }
    }
    public boolean remove(TypeId typeId){
        if(typeId != null){
            return remove(indexOf(typeId.getIdx()));
        }
        return false;
    }
    public boolean remove(TypeKey typeKey){
        return remove(get(typeKey));
    }
    public Iterator<TypeKey> getTypeKeys() {
        return ComputeIterator.of(iterator(), TypeId::getKey);
    }
    public Iterator<String> getTypeNames() {
        return ComputeIterator.of(iterator(), TypeId::getName);
    }
    @Override
    public Iterator<TypeId> iterator() {
        return ArrayIterator.of(getTypeIds());
    }
    @Override
    public int size() {
        return super.size();
    }
    public boolean contains(TypeKey typeKey){
        if(typeKey != null){
            for(TypeId typeId : this){
                if(typeKey.equals(typeId.getKey())){
                    return true;
                }
            }
        }
        return false;
    }
    public TypeId get(TypeKey typeKey){
        if(typeKey != null){
            for(TypeId typeId : this){
                if(typeKey.equals(typeId.getKey())){
                    return typeId;
                }
            }
        }
        return null;
    }

    public TypeId[] getTypeIds(){
        return typeIds;
    }
    public String[] getNames() {
        TypeId[] typeIds = getTypeIds();
        if(typeIds == null){
            return null;
        }
        int length = typeIds.length;
        String[] results = new String[length];
        for(int i = 0; i < length; i++){
            results[i] = typeIds[i].getName();
        }
        return results;
    }
    public TypeId getTypeId(int index){
        TypeId[] typeIds = getTypeIds();
        if(typeIds != null && index >= 0 && index < typeIds.length){
            return typeIds[index];
        }
        return null;
    }
    public TypeId getTypeIdForRegister(int register){
        TypeId[] typeIds = getTypeIds();
        if(typeIds == null){
            return null;
        }
        int count = 0;
        for (TypeId typeId : typeIds) {
            if (count == register) {
                return typeId;
            }
            count++;
            if (typeId.isWide()) {
                count++;
            }
        }
        return null;
    }
    public int getParameterRegistersCount(){
        TypeId[] typeIds = getTypeIds();
        int count = 0;
        if(typeIds != null){
            for (TypeId typeId : typeIds) {
                count++;
                if (typeId.isWide()) {
                    count++;
                }
            }
        }
        return count;
    }
    @Override
    void onChanged(){
        cacheTypeIds();
    }
    private void cacheTypeIds(){
        typeIds = getSectionItem(SectionType.TYPE_ID, toArray());
        if(typeIds == null){
            return;
        }
        for(TypeId typeId : typeIds){
            typeId.addUsageType(UsageMarker.USAGE_INTERFACE);
        }
    }

    @Override
    protected void onPreRefresh() {
        refreshTypeIds();
    }
    private void refreshTypeIds() {
        TypeId[] typeIds = getTypeIds();
        if(typeIds == null){
            setSize(0);
            return;
        }
        int length = typeIds.length;
        setSize(length, false);
        for(int i = 0; i < length; i++){
            TypeId typeId = typeIds[i].getReplace();
            typeIds[i] = typeId;
            typeId.addUsageType(UsageMarker.USAGE_INTERFACE);
            put(i, typeIds[i].getIdx());
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        for(TypeId typeId : this){
            typeId.append(writer);
        }
    }
    public void appendInterfaces(SmaliWriter writer) throws IOException {
        SmaliDirective smaliDirective = null;
        for(TypeId typeId : this){
            writer.newLine();
            if(smaliDirective == null){
                writer.appendComment("interfaces");
                writer.newLine();
                smaliDirective = SmaliDirective.IMPLEMENTS;
            }
            smaliDirective.append(writer);
            typeId.append(writer);
        }
        if(smaliDirective != null){
            writer.newLine();
        }
    }

    @Override
    public int compareTo(TypeList typeList) {
        if(typeList == null){
            return -1;
        }
        return SectionTool.compareIdx(iterator(), typeList.iterator());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(TypeId typeId : this){
            builder.append(typeId);
        }
        return builder.toString();
    }

    public static boolean equals(TypeList typeList1, TypeList typeList2) {
        if(typeList1 == typeList2) {
            return true;
        }
        if(typeList1 == null){
            return false;
        }
        return CompareUtil.compare(typeList1.getNames(), typeList2.getNames()) == 0;
    }
}
