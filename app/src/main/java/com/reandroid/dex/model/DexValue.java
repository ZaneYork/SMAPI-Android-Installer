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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.*;

import java.io.IOException;

public class DexValue extends Dex {

    private final Dex declaring;
    private final DexValueBlock<?> dexValueBlock;

    public DexValue(Dex declaring, DexValueBlock<?> dexValueBlock) {
        this.declaring = declaring;
        this.dexValueBlock = dexValueBlock;
    }

    public Key getKey(){
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof SectionValue){
            return ((SectionValue<?>)value).getKey();
        }
        return null;
    }
    public void setKey(Key key){
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof SectionValue){
            ((SectionValue<?>)value).setItem(key);
        }
    }
    public TypeKey getTypeKey(){
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof TypeValue){
            return ((TypeValue)value).getKey();
        }
        return null;
    }
    public String getString() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof StringValue){
            return ((StringValue)value).getString();
        }
        return null;
    }
    public void setString(String str) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof StringValue){
            ((StringValue)value).setString(str);
        }
    }
    public Integer getInteger() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof IntValue){
            return ((IntValue)value).get();
        }
        return null;
    }
    public void setInteger(int i) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof IntValue){
            ((IntValue)value).set(i);
        }
    }

    public Byte getByte() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof ByteValue){
            return ((ByteValue)value).get();
        }
        return null;
    }
    public void setByte(byte b) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof ByteValue){
            ((ByteValue)value).set(b);
        }
    }
    public Short getShort() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof ShortValue){
            return ((ShortValue)value).get();
        }
        return null;
    }
    public void setShort(short s) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof ShortValue){
            ((ShortValue)value).set(s);
        }
    }
    public Character getCharacter() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof CharValue){
            return ((CharValue)value).get();
        }
        return null;
    }
    public void setCharacter(char c) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof CharValue){
            ((CharValue)value).set(c);
        }
    }
    public Long getLong() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof LongValue){
            return ((LongValue)value).get();
        }
        return null;
    }
    public void setLong(long l) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof LongValue){
            ((LongValue)value).set(l);
        }
    }
    public Double getDouble() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof DoubleValue){
            return ((DoubleValue)value).get();
        }
        return null;
    }
    public void setDouble(double d) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof DoubleValue){
            ((DoubleValue)value).set(d);
        }
    }
    public Float getFloat() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof FloatValue){
            return ((FloatValue)value).get();
        }
        return null;
    }
    public void setFloat(float f) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof FloatValue){
            ((FloatValue)value).set(f);
        }
    }
    public Number getNumber() {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof PrimitiveValueBlock){
            return ((PrimitiveValueBlock)value).getData();
        }
        return null;
    }
    public void setNumber(Number number) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof PrimitiveValueBlock){
            ((PrimitiveValueBlock)value).setData(number);
        }
    }
    public Object getData() {
        return getDexValueBlock().getData();
    }
    public void setData(Object data) {
        getDexValueBlock().setData(data);
    }
    public DexAnnotation getAnnotation(){
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof AnnotationValue){
            AnnotationValue annotationValue = (AnnotationValue) value;
            return DexAnnotation.create(getDeclaring(), annotationValue.get());
        }
        return null;
    }


    public String getAsString() {
        return getDexValueBlock().getAsString();
    }
    public int getAsInteger() {
        return getAsInteger(0);
    }
    public int getAsInteger(int def) {
        DexValueBlock<?> value = getDexValueBlock();
        if(value instanceof PrimitiveValueBlock){
            return (int) ((PrimitiveValueBlock)value).getSignedValue();
        }
        return def;
    }
    public DexValueType<?> getValueType(){
        return getDexValueBlock().getValueType();
    }
    public DexValueBlock<?> getDexValueBlock() {
        return dexValueBlock;
    }

    public Dex getDeclaring() {
        return declaring;
    }

    @Override
    public boolean uses(Key key) {
        Key valueKey = getKey();
        if(valueKey != null){
            return valueKey.uses(key);
        }
        return false;
    }
    @Override
    public DexClassRepository getClassRepository() {
        return getDeclaring().getClassRepository();
    }

    @Override
    public void removeSelf() {
        throw new RuntimeException("Method not implemented");
    }

    public int getIndex(){
        return getDexValueBlock().getIndex();
    }
    public boolean is(DexValueType<?> valueType){
        return getValueType() == valueType;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getDexValueBlock().append(writer);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DexValue dexValue = (DexValue) obj;
        return getDexValueBlock() == dexValue.getDexValueBlock();
    }

    @Override
    public int hashCode() {
        return getDexValueBlock().hashCode();
    }
    @Override
    public String toString() {
        return getAsString();
    }

    public static DexValue create(Dex declaring, DexValueBlock<?> valueBlock){
        if(declaring != null && valueBlock != null){
            if(valueBlock instanceof ArrayValue){
                return new DexValueArray(declaring, (ArrayValue) valueBlock);
            }
            return new DexValue(declaring, valueBlock);
        }
        return null;
    }
}
