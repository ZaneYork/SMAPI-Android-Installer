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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.DataKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.NullValue;
import com.reandroid.dex.value.SectionValue;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.InstanceIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

public class EncodedArray extends DataItem implements ModifiableKeyItem, Iterable<DexValueBlock<?>> {

    private final Ule128Item valuesCountReference;
    private final BlockList<DexValueBlock<?>> valueList;
    private final DataKey<EncodedArray> itemKey;

    public EncodedArray() {
        super(2);
        this.valuesCountReference = new Ule128Item();
        this.valueList = new BlockList<>();
        this.valueList.setCreator(CREATOR);
        addChild(0, valuesCountReference);
        addChild(1, valueList);
        this.itemKey = new DataKey<>(this);
    }

    @Override
    public DataKey<EncodedArray> getKey() {
        return itemKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key) {
        DataKey<EncodedArray> other = (DataKey<EncodedArray>) key;
        merge(other.getItem());
    }

    @Override
    public SectionType<EncodedArray> getSectionType() {
        return SectionType.ENCODED_ARRAY;
    }

    public DexValueBlock<?> get(int i){
        return getValueList().get(i);
    }
    public<T1 extends IdItem> SectionValue<T1> getOrCreate(SectionType<T1> sectionType, int i){
        return getOrCreate(DexValueType.get(sectionType), i);
    }
    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getOrCreate(DexValueType<T1> valueType, int i){
        BlockList<DexValueBlock<?>> array = getValueList();
        array.ensureSize(i + 1);
        DexValueBlock<?> value = array.get(i);
        if(value == null || value == NullValue.PLACE_HOLDER || value.getValueType() != valueType){
            value = valueType.newInstance();
            array.set(i, value);
        }
        return (T1) value;
    }
    public int size(){
        return getValueList().getCount();
    }
    public void add(DexValueBlock<?> value){
       getValueList().add(value);
    }
    public boolean remove(int index){
        return getValueList().remove(index) != null;
    }
    public boolean remove(DexValueBlock<?> value){
        return getValueList().remove(value);
    }
    public boolean removeIf(Predicate<? super DexValueBlock<?>> filter){
        return getValueList().removeIf(filter);
    }
    public void set(int i, DexValueBlock<?> value){
        ensureSize(i + 1);
        getValueList().set(i, value);
    }
    public void clear(){
        getValueList().clearChildes();
    }
    public void removeAll(){
        getValueList().clearTemporarily();
    }
    public boolean sort(Comparator<? super DexValueBlock<?>> comparator){
        return getValueList().sort(comparator);
    }
    public void trimNull(){
        int size = size();
        int updatedSize = 0;
        for(int i = 0; i < size; i++){
            DexValueBlock<?> value = get(i);
            if(value != null && !value.is(DexValueType.NULL) && !value.isTemporary()) {
                updatedSize = i + 1;
            }
        }
        if(updatedSize != size){
            setSize(updatedSize);
        }
    }


    public void ensureSize(int size){
        if(size > size()){
            setSize(size);
        }
    }
    public void setSize(int size) {
        if(size < 0){
            throw new IndexOutOfBoundsException("Invalid size: " + size);
        }
        valuesCountReference.set(size);
        BlockList<DexValueBlock<?>> valueList = this.getValueList();
        if(size == 0){
            valueList.clearTemporarily();
            return;
        }
        int current = valueList.size();
        if(size <= current){
            if(size < current){
                valueList.setSize(size);
            }
            return;
        }
        NullValue placeHolder = NullValue.PLACE_HOLDER;
        int remain = size - current;
        valueList.ensureCapacity(remain);
        for(int i = 0; i < remain; i++){
            valueList.add(placeHolder);
        }
    }
    @Override
    public Iterator<DexValueBlock<?>> iterator(){
        return getValueList().iterator();
    }
    public<T1 extends DexValueBlock<?>> Iterator<T1> iterator(Class<T1> instance){
        return InstanceIterator.of(iterator(), instance);
    }
    public<T1 extends DexValueBlock<?>> Iterator<T1> iterator(Class<T1> instance, Predicate<? super T1> filter){
        return InstanceIterator.of(iterator(), instance, filter);
    }
    public Iterator<DexValueBlock<?>> iterator(int start, int length) {
        return getValueList().iterator(start, length);
    }
    public Iterator<DexValueBlock<?>> clonedIterator(){
        return getValueList().clonedIterator();
    }

    private BlockList<DexValueBlock<?>> getValueList() {
        return valueList;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        this.valuesCountReference.onReadBytes(reader);
        BlockList<DexValueBlock<?>> valueList = getValueList();
        int count = this.valuesCountReference.get();
        valueList.ensureCapacity(count);
        for(int i = 0; i < count; i++){
            DexValueBlock<?> dexValue = DexValueType.create(reader);
            valueList.add(dexValue);
            dexValue.onReadBytes(reader);
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        this.valuesCountReference.set(size());
    }

    public Iterator<IdItem> usedIds(){
        return new IterableIterator<DexValueBlock<?>, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(DexValueBlock<?> element) {
                return element.usedIds();
            }
        };
    }

    @Override
    public void copyFrom(DataItem item) {
        EncodedArray other = (EncodedArray) item;
        merge(other);
    }

    public void merge(EncodedArray array){
        int size = array.size();
        getValueList().ensureCapacity(size);
        for(int i = 0; i < size; i++){
            DexValueBlock<?> coming = array.get(i);
            DexValueBlock<?> valueBlock = getOrCreate(coming.getValueType(), i);
            valueBlock.merge(coming);
        }
    }
    @Override
    public int hashCode() {
        int hash = 1;
        int size = size();
        for(int i = 0; i < size; i++){
            hash = hash * 31 + get(i).hashCode();
        }
        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EncodedArray array = (EncodedArray) obj;
        int size = size();
        if(size != array.size()){
            return false;
        }
        for(int i = 0; i < size; i++){
            if(!ObjectsUtil.equals(get(i), array.get(i))){
                return false;
            }
        }
        return true;
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        Iterator<DexValueBlock<?>> iterator = iterator();
        boolean appendOnce = false;
        while (iterator.hasNext()){
            if(appendOnce){
                builder.append(", ");
            }
            builder.append(iterator.next());
            appendOnce = true;
        }
        builder.append(']');
        return builder.toString();
    }
    private static final Creator<DexValueBlock<?>> CREATOR = new Creator<DexValueBlock<?>>() {
        @Override
        public DexValueBlock<?>[] newArrayInstance(int length) {
            if(length == 0){
                return EncodedArray.EMPTY;
            }
            return new DexValueBlock[length];
        }
        @Override
        public DexValueBlock<?> newInstance() {
            return NullValue.PLACE_HOLDER;
        }
    };
    static final DexValueBlock<?>[] EMPTY = new DexValueBlock<?>[0];
}
