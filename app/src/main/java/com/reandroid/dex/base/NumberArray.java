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
package com.reandroid.dex.base;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class NumberArray extends DexBlockItem {

    private final IntegerReference widthReference;
    private final IntegerReference itemCount;

    private List<Number> numberList;

    public NumberArray(IntegerReference widthReference, IntegerReference itemCount) {
        super(0);
        this.widthReference = widthReference;
        this.itemCount = itemCount;
    }

    public Iterator<IntegerReference> getReferences(){
        return new Iterator<IntegerReference>() {
            private int mIndex;
            @Override
            public boolean hasNext() {
                return mIndex < NumberArray.this.size();
            }
            @Override
            public IntegerReference next() {
                IntegerReference reference = NumberArray.this.getReference(mIndex);
                mIndex ++;
                return reference;
            }
        };
    }
    public IntegerReference getReference(int index) {
        if(index >= size()){
            return null;
        }
        return new Data(this, index);
    }
    public short[] getShortArray(){
        int width = getWidth();
        short[] results = new short[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getShort(bytes, i * width);
        }
        return results;
    }
    public int[] getByteUnsignedArray(){
        int[] results = new int[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = bytes[i] & 0xff;
        }
        return results;
    }
    public int[] getShortUnsignedArray(){
        int width = getWidth();
        int[] results = new int[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getShortUnsigned(bytes, i * width);
        }
        return results;
    }
    public int[] getIntArray(){
        int width = getWidth();
        int[] results = new int[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getInteger(bytes, i * width);
        }
        return results;
    }
    public long[] getLongArray(){
        int width = getWidth();
        long[] results = new long[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getLong(bytes, i * width);
        }
        return results;
    }

    public int getByteUnsigned(int index){
        return getBytesInternal()[index * getWidth()] & 0xff;
    }

    public void put(byte[] values){
        int index = size();
        int length = values.length;
        ensureSize(index + length);
        for(int i = 0; i < length; i++){
            put(index + i, values[i] & 0xff);
        }
    }
    public void put(short[] values){
        int index = size();
        int length = values.length;
        ensureSize(index + length);
        for(int i = 0; i < length; i++){
            put(index + i, values[i] & 0xffff);
        }
    }
    public void put(int[] values){
        int index = size();
        int length = values.length;
        ensureSize(index + length);
        for(int i = 0; i < length; i++){
            put(index + i, values[i]);
        }
    }
    public void put(int index, int value){
        validateValueRange(value);
        ensureSize(index + 1);
        int width = getWidth();
        index = index * width;
        byte[] bytes = getBytesInternal();
        if(width < 2){
            getBytesInternal()[index] = (byte) value;
        }else if(width < 4){
            putShort(bytes, index, value);
        }else if(width == 4){
            putInteger(bytes, index, value);
        }else {
            putLong(bytes, index, 0x00000000ffffffffL & value);
        }
    }
    private void validateValueRange(int value){
        int count = 0;
        int shift = value;
        while (shift != 0){
            shift = shift >>> 8;
            count ++;
        }
        if(count <= getWidth()){
            return;
        }
        throw new DexException("Value out of range width = " + getWidth()
                + ", value = " + HexUtil.toHex(value, 1));
    }
    public void putLong(long[] values){
        int index = size();
        int length = values.length;
        ensureSize(index + length);
        for(int i = 0; i < length; i++){
            putLong(index + i, values[i]);
        }
    }
    public void putLong(int index, long value){
        ensureSize(index + 1);
        int width = getWidth();
        index = index * width;
        putLong(getBytesInternal(), index, value);
    }
    public int getShortUnsigned(int index){
        return getShortUnsigned(getBytesInternal(), index * getWidth());
    }
    public byte getByte(int index){
        return getBytesInternal()[index * getWidth()];
    }
    public short getShort(int index){
        return getShort(getBytesInternal(), index * getWidth());
    }
    public int getInteger(int index){
        return getInteger(getBytesInternal(), index * getWidth());
    }
    public void setInteger(int index, int value){
        putInteger(getBytesInternal(), index * getWidth(), value);
    }
    public long getLong(int index){
        return getLong(getBytesInternal(), index * getWidth());
    }

    public List<Number> toList(){
        if(numberList == null){
            numberList = new AbstractList<Number>() {
                @Override
                public Number get(int i) {
                    return NumberArray.this.getNumber(i);
                }
                @Override
                public int size() {
                    return NumberArray.this.size();
                }
            };
        }
        return numberList;
    }
    public Number getNumber(int index){
        int width = getWidth();
        if(width == 1){
            return getBytesInternal()[index];
        }
        int offset = index * width;
        if(width == 2){
            return getShortUnsigned(getBytesInternal(), offset);
        }
        if(width == 4){
            return getInteger(getBytesInternal(), offset);
        }
        return getLong(getBytesInternal(), offset);
    }
    public int getAsInteger(int index){
        int width = getWidth();
        if(width < 2){
            return getByteUnsigned(index);
        }
        if(width < 4){
            return getShortUnsigned(index);
        }
        if(width == 4){
            return getInteger(index);
        }
        return (int) getLong(index);
    }
    public int[] getAsIntegers(){
        int size = size();
        int[] results = new int[size];
        for(int i = 0; i < size; i++){
            results[i] = getAsInteger(i);
        }
        return results;
    }
    public int size(){
        return countBytes() / getWidth();
    }
    public void ensureSize(int size){
        if(size > size()){
            setSize(size);
        }
    }
    public void setSize(int size){
        setBytesLength(size * getWidth(), false);
        itemCount.set(size);
    }
    public int getWidth(){
        int width = widthReference.get();
        if(width == 0){
            width = 1;
        }
        return width;
    }
    public void setWidth(int width){
        if(width == widthReference.get()){
            return;
        }
        if(size() == 0 ){
            widthReference.set(width);
        }else if(getWidth() > 4){
            changeWidthOfLong(width);
        }else {
            changeWidthOfInt(width);
        }
    }
    private void changeWidthOfInt(int width){
        int[] backup = getAsIntegers();
        this.setSize(0);
        this.widthReference.set(width);
        ensureSize(backup.length);
        put(backup);
    }
    private void changeWidthOfLong(int width){
        long[] backup = getLongArray();
        this.setSize(0);
        this.widthReference.set(width);
        ensureSize(backup.length);
        putLong(backup);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int count = itemCount.get();
        int width = widthReference.get();
        if(width == 0){
            width = 1;
            count = 0;
            itemCount.set(0);
        }
        setBytesLength(count * width, false);
        super.onReadBytes(reader);
    }
    public void merge(NumberArray array){
        setWidth(array.getWidth());
        setSize(array.size());
        byte[] coming = array.getBytesInternal();
        byte[] bytes = getBytesInternal();
        int length = coming.length;
        for(int i = 0; i < length; i++){
            bytes[i] = coming[i];
        }
    }

    @Override
    public String toString() {
        return "width=" + getWidth() + ", " + StringsUtil.toString(toList());
    }

    static class Data implements IntegerReference {

        private final NumberArray numberArray;
        private final int index;

        public Data(NumberArray numberArray, int index){
            this.numberArray = numberArray;
            this.index = index;
        }
        @Override
        public int get() {
            return numberArray.getAsInteger(index);
        }
        @Override
        public void set(int value) {
            numberArray.put(index, value);
        }
    }
}
