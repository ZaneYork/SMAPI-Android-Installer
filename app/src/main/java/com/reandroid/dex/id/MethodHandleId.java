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

import com.reandroid.dex.base.IndirectShort;
import com.reandroid.dex.common.MethodHandleType;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodHandleKey;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.MethodHandleTypeReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodHandleId extends IdItem implements Comparable<MethodHandleId> {

    private final MethodHandleTypeReference handleType;
    private final IndirectShort unused1;
    private final IdItemIndirectReference<IdItem> member;
    private final IndirectShort unused2;

    public MethodHandleId() {
        super(8);
        MethodHandleTypeReference handleType = new MethodHandleTypeReference(this, 0);
        this.handleType = handleType;
        this.unused1 = new IndirectShort(this, 2);
        this.member = new IdItemIndirectReference<IdItem>(null, this, 4) {
            @Override
            public SectionType<IdItem> getSectionType() {
                return handleType.getSectionType();
            }
        };
        this.unused2 = new IndirectShort(this, 6);
    }

    @Override
    public MethodHandleKey getKey() {
        return checkKey(new MethodHandleKey(getHandleType(), getMember()));
    }
    @Override
    public void setKey(Key key) {
        MethodHandleKey methodHandleKey = (MethodHandleKey) key;
        setMethodHandleType(methodHandleKey.getHandleType());
        setMember(methodHandleKey.getMember());
    }

    public MethodHandleType getHandleType() {
        return handleType.getHandleType();
    }
    public void setMethodHandleType(MethodHandleType handleType) {
        this.handleType.setMethodHandleType(handleType);
    }
    public IdItem getMemberId(){
        return member.getItem();
    }
    public Key getMember(){
        return member.getKey();
    }
    public void setMember(IdItem idItem){
        member.setItem(idItem);
    }
    public void setMember(Key key){
        member.setItem(key);
    }
    public void set(MethodHandleType type, Key key) {
        setMethodHandleType(type);
        setMember(key);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.singleOne(
                this,
                getMemberId().usedIds()
        );
    }
    @Override
    public SectionType<MethodHandleId> getSectionType(){
        return SectionType.METHOD_HANDLE;
    }
    public SectionType<?> getMemberSectionType() {
        return this.handleType.getSectionType();
    }

    @Override
    public void refresh() {
        member.refresh();
    }
    @Override
    void cacheItems() {
        member.pullItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, true);
    }
    public void append(SmaliWriter writer, boolean appendType) throws IOException {
        if (appendType) {
            MethodHandleType handleType = getHandleType();
            if (handleType == null) {
                writer.append("unknown handle = ");
                writer.append(this.handleType.get());
            } else {
                handleType.append(writer);
            }
        }
        writer.append('@');
        IdItem id = getMemberId();
        if(id == null){
            writer.append("error id = ");
            writer.appendInteger(this.member.get());
        }else {
            id.append(writer);
        }
    }

    @Override
    public int compareTo(MethodHandleId methodHandleId) {
        if (methodHandleId == null) {
            return -1;
        }
        int i = CompareUtil.compare(getHandleType(), methodHandleId.getHandleType());
        if (i != 0) {
            return i;
        }
        IdItem item1 = this.getMemberId();
        if (item1 instanceof FieldId) {
            return CompareUtil.compare((FieldId) item1, (FieldId) methodHandleId.getMemberId());
        }
        return CompareUtil.compare((MethodId) item1, (MethodId) methodHandleId.getMemberId());
    }

    @Override
    public String toString() {
        String smali = SmaliWriter.toStringSafe(this);
        if (this.unused1.get() != 0 || this.unused2.get() != 0) {
            smali = "unused1 = " + unused1.get() +
                    ", unused2 = " + unused2.get() + ": " + smali;
        }
        return smali;
    }
}
