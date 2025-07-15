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
package com.reandroid.dex.model;

import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.value.ArrayValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

public class DexValueArray extends DexValue implements Iterable<DexValue> {

    public DexValueArray(Dex declaring, ArrayValue arrayValue) {
        super(declaring, arrayValue);
    }

    @Override
    public Iterator<DexValue> iterator() {
        return ComputeIterator.of(getDexValueBlock().iterator(),
                valueBlock -> DexValue.create(DexValueArray.this, valueBlock));
    }
    public Iterator<DexValue> clonedIterator() {
        return ComputeIterator.of(getDexValueBlock().clonedIterator(),
                valueBlock -> DexValueArray.create(DexValueArray.this, valueBlock));
    }

    public DexValue get(int index){
        return createValue(getDexValueBlock().get(index));
    }
    public int size(){
        return getDexValueBlock().size();
    }
    public boolean remove(int index){
        return getDexValueBlock().remove(index);
    }
    public boolean remove(DexValue dexValue){
        if(dexValue == null){
            return false;
        }
        return getDexValueBlock().remove(dexValue.getDexValueBlock());
    }
    public boolean removeIf(Predicate<? super DexValue> filter){
        return getDexValueBlock().removeIf(
                dexValueBlock -> filter.test(DexValueArray.this.createValue(dexValueBlock)));
    }
    public void clear(){
        getDexValueBlock().clear();
    }
    public boolean isEmpty(){
        return size() == 0;
    }
    public boolean sort(Comparator<DexValue> comparator){
        Comparator<DexValueBlock<?>> valueComparator =
                (value1, value2) -> comparator.compare(
                        DexValueArray.create(DexValueArray.this, value1),
                        DexValueArray.create(DexValueArray.this, value2)
                );
        return getDexValueBlock().sort(valueComparator);
    }

    public boolean isFirstType(DexValueType<?> valueType){
        return valueType != null && valueType == getFirstType();
    }
    public DexValueType<?> getFirstType() {
        DexValueBlock<?> valueBlock = getDexValueBlock().get(0);
        if(valueBlock != null){
            return valueBlock.getValueType();
        }
        return null;
    }

    public DexValue createNext(DexValueType<?> valueType){
        return DexValue.create(this,
                getDexValueBlock().createNext(valueType));
    }

    public Iterator<String> getStrings(){
        return ComputeIterator.of(iterator(), DexValue::getString);
    }
    public Iterator<TypeKey> getTypeKeys(){
        return ComputeIterator.of(iterator(), DexValue::getTypeKey);
    }
    public Iterator<Number> getNumbers(){
        return ComputeIterator.of(iterator(), DexValue::getNumber);
    }

    public Iterator<Character> getCharacters(){
        return ComputeIterator.of(iterator(), DexValue::getCharacter);
    }
    public Iterator<Integer> getIntegers(){
        return ComputeIterator.of(iterator(), DexValue::getInteger);
    }
    public Iterator<Key> getKeys(){
        return ComputeIterator.of(iterator(), DexValue::getKey);
    }
    public Iterator<DexAnnotation> getAnnotations(){
        return ComputeIterator.of(iterator(), DexValue::getAnnotation);
    }

    public void add(String str){
        createNext(DexValueType.STRING).setString(str);
    }
    public void add(Key key){
        if(key instanceof TypeKey){
            createNext(DexValueType.TYPE).setKey(key);
        }else if(key instanceof FieldKey){
            createNext(DexValueType.ENUM).setKey(key);
        }else if(key instanceof MethodKey){
            createNext(DexValueType.METHOD).setKey(key);
        }else {
            throw new RuntimeException("Unimplemented key-type: " + key.getClass());
        }
    }

    public void add(byte b){
        createNext(DexValueType.BYTE).setByte(b);
    }
    public void add(short s){
        createNext(DexValueType.SHORT).setShort(s);
    }
    public void add(int i){
        createNext(DexValueType.INT).setInteger(i);
    }
    public void add(float f){
        createNext(DexValueType.FLOAT).setFloat(f);
    }
    public void add(long l){
        createNext(DexValueType.LONG).setLong(l);
    }
    public void add(double d){
        createNext(DexValueType.DOUBLE).setDouble(d);
    }
    public void add(char c){
        createNext(DexValueType.CHAR).setCharacter(c);
    }

    @Override
    public ArrayValue getDexValueBlock() {
        return (ArrayValue) super.getDexValueBlock();
    }
    DexValue createValue(DexValueBlock<?> valueBlock) {
        return DexValue.create(getDeclaring(), valueBlock);
    }
}
