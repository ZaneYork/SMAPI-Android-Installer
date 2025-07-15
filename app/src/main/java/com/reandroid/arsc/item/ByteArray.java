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
package com.reandroid.arsc.item;

import java.util.AbstractList;
import java.util.List;

public class ByteArray extends BlockItem {
    public ByteArray(int bytesLength) {
        super(bytesLength);
    }
    public ByteArray() {
        this(0);
    }
    public final void clear(){
        setSize(0);
    }
    public final void add(byte[] values){
        if(values==null || values.length==0){
            return;
        }
        int old=size();
        int len=values.length;
        setBytesLength(old+len, false);
        byte[] bts = getBytesInternal();
        System.arraycopy(values, 0, bts, old, len);
    }
    public final void set(byte[] values){
        super.setBytesInternal(values);
    }
    public final byte[] toArray(){
        return getBytes();
    }
    public final void fill(byte value){
        byte[] bts=getBytesInternal();
        int max=bts.length;
        for(int i=0;i<max;i++){
            bts[i]=value;
        }
    }
    public final void ensureArraySize(int s){
        int sz=size();
        if(sz>=s){
            return;
        }
        setSize(s);
    }
    public final void setSize(int s){
        if(s<0){
            s=0;
        }
        setBytesLength(s);
    }
    public final int size(){
        return getBytesLength();
    }
    public byte get(int index){
        return getBytesInternal()[index];
    }
    public int getByteUnsigned(int index){
        return 0xff & get(index);
    }
    public final void putByte(int index, int byteValue){
        put(index, (byte) byteValue);
    }
    public final void put(int index, byte value){
        byte[] bts = getBytesInternal();
        bts[index]=value;
    }
    public boolean getBit(int byteOffset, int bitIndex){
        return getBit(getBytesInternal(), byteOffset, bitIndex);
    }
    public void putBit(int byteOffset, int bitIndex, boolean bit){
        putBit(getBytesInternal(), byteOffset, bitIndex, bit);
    }
    public final void putShort(int offset, int value){
        putShort(offset, (short) value);
    }
    public final void putShort(int offset, short val){
        byte[] bts = getBytesInternal();
        bts[offset+1]= (byte) (val >>> 8 & 0xff);
        bts[offset]= (byte) (val & 0xff);
    }
    public final int getShortUnsigned(int offset){
        return 0xffff & getShort(offset);
    }
    public final short getShort(int offset){
        byte[] bts = getBytesInternal();
        return (short) (bts[offset] & 0xff | (bts[offset+1] & 0xff) << 8);
    }
    public final void putInteger(int offset, int val){
        byte[] bts = getBytesInternal();
        if((offset+4)>bts.length){
            return;
        }
        bts[offset+3]= (byte) (val >>> 24 & 0xff);
        bts[offset+2]= (byte) (val >>> 16 & 0xff);
        bts[offset+1]= (byte) (val >>> 8 & 0xff);
        bts[offset]= (byte) (val & 0xff);
    }
    public final int getInteger(int offset){
        byte[] bts = getBytesInternal();
        if((offset+4)>bts.length){
            return 0;
        }
        return bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 |
                (bts[offset+2] & 0xff) << 16 |
                (bts[offset+3] & 0xff) << 24;
    }
    public final void putByteArray(int offset, byte[] val){
        byte[] bts = getBytesInternal();
        int avail = bts.length-offset;
        if(avail<=0){
            return;
        }
        int len = val.length;
        if(len>avail){
            len=avail;
        }
        System.arraycopy(val, 0, bts, offset, len);
    }
    public final byte[] getByteArray(int offset, int length){
        byte[] bts = getBytesInternal();
        byte[] result = new byte[length];
        if (result.length >= 0) {
            System.arraycopy(bts, offset, result, 0, result.length);
        }
        return result;
    }

    public final List<Byte> toByteList(){
        return new AbstractList<Byte>() {
            @Override
            public Byte get(int i) {
                return ByteArray.this.get(i);
            }
            @Override
            public int size() {
                return ByteArray.this.size();
            }
        };
    }
    public final List<Short> toShortList(){
        return new AbstractList<Short>() {
            @Override
            public Short get(int i) {
                return ByteArray.this.getShort(i);
            }
            @Override
            public int size() {
                return ByteArray.this.size()/2;
            }
        };
    }
    public final List<Integer> toIntegerList(){
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return ByteArray.this.getInteger(i);
            }
            @Override
            public int size() {
                return ByteArray.this.size()/4;
            }
        };
    }
    @Override
    public String toString(){
        return "size="+size();
    }

    public static byte[] trimTrailZeros(byte[] bts){
        if(bts==null){
            return new byte[0];
        }
        int len=0;
        for(int i=0;i<bts.length;i++){
            if(bts[i]!=0){
                len=i+1;
            }
        }
        byte[] result=new byte[len];
        if(result.length>0){
            System.arraycopy(bts, 0, result, 0, result.length);
        }
        return result;
    }
    public static boolean equals(byte[] bts1, byte[] bts2){
        if(bts1==bts2){
            return true;
        }
        if(bts1==null || bts1.length==0){
            return bts2==null || bts2.length==0;
        }
        if(bts2==null || bts2.length==0){
            return false;
        }
        if(bts1.length!=bts2.length){
            return false;
        }
        for(int i=0;i<bts1.length;i++){
            if(bts1[i]!=bts2[i]){
                return false;
            }
        }
        return true;
    }
    public static boolean equalsIgnoreTrailZero(byte[] bts1, byte[] bts2){
        if(bts1==bts2){
            return true;
        }
        return equals(trimTrailZeros(bts1), trimTrailZeros(bts2));
    }
}
