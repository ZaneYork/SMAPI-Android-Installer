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
package com.reandroid.dex.value;

import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.utils.HexUtil;

public class NumberValue extends DexBlockItem {
    public NumberValue() {
        super(1);
    }
    public int getSize(){
        return countBytes();
    }
    public void setSize(int size){
        setBytesLength(size, false);
    }

    public long getSignedNumber(){
        return getSignedNumber(getBytesInternal(), 0, getSize());
    }
    public long getUnsignedNumber(){
        return getUnsignedNumber(getBytesInternal(), 0, getSize());
    }
    public void setNumberValue(byte value){
        setSize(1);
        getBytesInternal()[0] = value;
    }
    public void setNumberValue(short value){
        setSignedNumberValue(value & 0xffffL, value < 0);
    }
    public void setNumberValue(int value){
        setSignedNumberValue(value & 0xffffffffL, value < 0);
    }
    public void setNumberValue(long value){
        setSignedNumberValue(value, value < 0);
    }
    public void setUnsignedNumber(long value){
        int size = calculateUnsignedSize(value);
        setSize(size);
        putNumber(getBytesInternal(), 0, size, value);
    }
    public void setNumber(long value, int size){
        setSize(size);
        putNumber(getBytesInternal(), 0, size, value);
    }
    private void setSignedNumberValue(long value, boolean negative){
        int size = calculateSignedSize(value, negative);
        setSize(size);
        putNumber(getBytesInternal(), 0, size, value);
    }

    public String toHex(){
        return HexUtil.toHex(getUnsignedNumber(), getSize());
    }

    public void merge(NumberValue value){
        byte[] coming = value.getBytes();
        int length = coming.length;
        setBytesLength(length, false);
        byte[] bytes = getBytesInternal();

        for(int i = 0; i < length; i++){
            bytes[i] = coming[i];
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getUnsignedNumber());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return getUnsignedNumber() == ((NumberValue) obj).getUnsignedNumber();
    }

    @Override
    public String toString() {
        return getSize() + ":" + toHex();
    }

    private static int calculateSignedSize(long l, boolean negative) {
        if(l == 0) {
            return 1;
        }
        int size = 0;
        int sign = 0;
        long value = l;
        while (value != 0) {
            sign = (int)value;
            value = value >>> 8;
            size ++;
        }
        if(!negative && sign >= 0x80) {
            size++;
        }else if (negative){
            byte[] bytes = new byte[size];
            putNumber(bytes, 0, size, l);
            int j = size - 1;
            while (j > 0) {
                int high = bytes[j] & 0xff;
                int next = bytes[j - 1] & 0xff;
                if(high == 0xff && next > 0x80) {
                    size --;
                }else {
                    break;
                }
                j--;
            }
        }
        return size;
    }
    private static int calculateUnsignedSize(long value) {
        if(value == 0) {
            return 1;
        }
        int i = 0;
        while (value != 0) {
            value = value >>> 8;
            i++;
        }
        return i;
    }
}
