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
import com.reandroid.dex.data.EncodedArray;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.*;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class CallSiteId extends IdItem implements Comparable<CallSiteId> {

    private final DataItemIndirectReference<EncodedArray> encodedArrayReference;

    public CallSiteId() {
        super(4);
        this.encodedArrayReference = new DataItemIndirectReference<>(SectionType.ENCODED_ARRAY,
                this, 0, UsageMarker.USAGE_CALL_SITE);
    }

    @Override
    public CallSiteKey getKey() {
        return checkKey(new CallSiteKey(getMethodHandle(), getMethodNameKey(),
                getProto(), getArguments()));
    }
    @Override
    public void setKey(Key key) {
        CallSiteKey callSiteKey = (CallSiteKey) key;
        setMethodHandle(callSiteKey.getMethodHandle());
        setMethodName(callSiteKey.getName());
        setProto(callSiteKey.getProto());
        throw new RuntimeException("Method not implemented");
    }

    public String callSiteName() {
        return NAME_PREFIX + getIdx();
    }
    public MethodHandleKey getMethodHandle() {
        return getMethodHandleId().getKey();
    }
    public MethodHandleId getMethodHandleId(){
        return getValue(SectionType.METHOD_HANDLE, 0);
    }
    public void setMethodHandle(MethodHandleKey key){
        getOrCreateValue(SectionType.METHOD_HANDLE, 0, key);
    }
    public String getMethodName() {
        StringId stringId = getMethodNameId();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public StringKey getMethodNameKey(){
        StringId stringId = getMethodNameId();
        if (stringId != null) {
            return stringId.getKey();
        }
        return null;
    }
    public void setMethodName(String methodName){
        setMethodName(StringKey.create(methodName));
    }
    public void setMethodName(StringKey methodName){
        getOrCreateValue(SectionType.STRING_ID, 1, methodName);
    }
    public StringId getMethodNameId(){
        return getValue(SectionType.STRING_ID, 1);
    }
    public ProtoId getProtoId() {
        return getValue(SectionType.PROTO_ID, 2);
    }
    public ProtoKey getProto() {
        ProtoId protoId = getProtoId();
        if (protoId != null) {
            return protoId.getKey();
        }
        return null;
    }
    public void setProto(ProtoKey protoKey){
        getOrCreateValue(SectionType.METHOD_ID, 2, protoKey);
    }
    public ArrayKey getArguments() {
        int size = getArgumentsSize();
        Key[] results = new Key[size];
        for (int i = 0; i < size; i++) {
            results[i] = getArgument(i);
        }
        return new ArrayKey(results);
    }
    public Iterator<DexValueBlock<?>> getArgumentValues() {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            return encodedArray.iterator(3, encodedArray.size() - 3);
        }
        return EmptyIterator.of();
    }
    public Key getArgument(int i) {
        DexValueBlock<?> valueBlock = getArgumentValue(i);
        if (valueBlock != null) {
            return valueBlock.getKey();
        }
        return null;
    }
    public DexValueBlock<?> getArgumentValue(int i) {
        if (i >= 0) {
            EncodedArray encodedArray = getEncodedArray();
            if (encodedArray != null) {
                return encodedArray.get(i + 3);
            }
        }
        return null;
    }
    public int getArgumentsSize() {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            int size = encodedArray.size();
            if (size > 0) {
                return size - 3;
            }
        }
        return 0;
    }
    private<T1 extends IdItem> T1 getOrCreateValue(SectionType<T1> sectionType, int index, Key key){
        EncodedArray encodedArray = getOrCreateEncodedArray();
        SectionValue<T1> sectionValue = encodedArray.getOrCreate(sectionType, index);
        sectionValue.setItem(key);
        return sectionValue.getItem();
    }
    @SuppressWarnings("unchecked")
    private<T1 extends IdItem> T1 getValue(SectionType<T1> sectionType, int index){
        EncodedArray encodedArray = getEncodedArray();
        if(encodedArray == null){
            return null;
        }
        DexValueBlock<?> value = encodedArray.get(index);
        if(!(value instanceof SectionValue)){
            return null;
        }
        SectionValue<?> sectionValue = (SectionValue<?>) value;
        if(sectionValue.getSectionType() != sectionType){
            return null;
        }
        return ((SectionValue<T1>)value).getItem();
    }
    public EncodedArray getOrCreateEncodedArray(){
        return encodedArrayReference.getOrCreate();
    }
    public EncodedArray getEncodedArray(){
        return encodedArrayReference.getItem();
    }

    @Override
    void cacheItems() {
        encodedArrayReference.pullItem();
    }
    @Override
    public void refresh() {
        encodedArrayReference.refresh();
    }
    @Override
    public Iterator<IdItem> usedIds() {
        EncodedArray encodedArray = getEncodedArray();
        if(encodedArray == null){
            return EmptyIterator.of();
        }
        return encodedArray.usedIds();
    }

    @Override
    public SectionType<CallSiteId> getSectionType() {
        return SectionType.CALL_SITE_ID;
    }
    public void merge(CallSiteId callSiteId){
        if(callSiteId == this){
            return;
        }
        EncodedArray encodedArray = getOrCreateEncodedArray();
        encodedArray.merge(callSiteId.getEncodedArray());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(callSiteName());
        writer.append('(');
        getMethodNameId().append(writer);
        writer.append(", ");
        getProtoId().append(writer);
        Iterator<DexValueBlock<?>> iterator = getArgumentValues();
        while (iterator.hasNext()) {
            writer.append(", ");
            iterator.next().append(writer);
        }
        writer.append(')');
        getMethodHandle().append(writer, false);
    }

    @Override
    public int compareTo(CallSiteId callSiteId) {
        if(callSiteId == this){
            return 0;
        }
        if(callSiteId == null){
            return -1;
        }
        int i = CompareUtil.compare(getMethodHandleId(), callSiteId.getMethodHandleId());
        if(i != 0){
            return i;
        }
        i = CompareUtil.compare(getMethodNameId(), callSiteId.getMethodNameId());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getProtoId(), callSiteId.getProtoId());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CallSiteId callSiteId = (CallSiteId) o;
        return Objects.equals(getEncodedArray(), callSiteId.getEncodedArray());
    }
    @Override
    public int hashCode() {
        EncodedArray encodedArray = getEncodedArray();
        if(encodedArray != null){
            return encodedArray.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }

    public static final String NAME_PREFIX = ObjectsUtil.of("call_site_");
}
