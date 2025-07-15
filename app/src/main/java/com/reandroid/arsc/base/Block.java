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
package com.reandroid.arsc.base;

import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Block {
    private int mIndex=-1;
    private Block mParent;
    private boolean mNull;
    private BlockLoad mBlockLoad;
    public abstract byte[] getBytes();
    public abstract int countBytes();
    public final int countUpTo(Block block){
        BlockCounter counter=new BlockCounter(block);
        onCountUpTo(counter);
        return counter.getCountValue();
    }
    public final BlockLocator.Result locateBlock(int bytePosition){
        BlockLocator locator = new BlockLocator(bytePosition);
        onCountUpTo(locator);
        return locator.getResult();
    }
    public abstract void onCountUpTo(BlockCounter counter);
    public final void readBytes(BlockReader reader) throws IOException{
        onReadBytes(reader);
        notifyBlockLoad(reader);
    }
    public final void setBlockLoad(BlockLoad blockLoad){
        mBlockLoad=blockLoad;
    }
    public void notifyBlockLoad() throws IOException {
        notifyBlockLoad(null);
    }
    private void notifyBlockLoad(BlockReader reader) throws IOException{
        BlockLoad blockLoad=mBlockLoad;
        if(blockLoad!=null){
            blockLoad.onBlockLoaded(reader, this);
        }
    }
    protected void onReadBytes(BlockReader reader) throws IOException{
    }
    public final int writeBytes(OutputStream stream) throws IOException{
        if(isNull()){
            return 0;
        }
        return onWriteBytes(stream);
    }
    protected abstract int onWriteBytes(OutputStream stream) throws IOException;
    public boolean isNull(){
        return mNull;
    }
    public void setNull(boolean is_null){
        mNull=is_null;
    }
    public final int getIndex(){
        return mIndex;
    }
    public final void setIndex(int index){
        int old=mIndex;
        if(index==old){
            return;
        }
        mIndex=index;
        if(old!=-1 && index!=-1){
            onIndexChanged(old, index);
        }
    }
    public void onIndexChanged(int oldIndex, int newIndex){

    }
    public final void setParent(Block parent){
        if(parent==this){
            return;
        }
        mParent=parent;
    }
    public final Block getParent(){
        return mParent;
    }
    @SuppressWarnings("unchecked")
    public final <T> T getParent(Class<T> parentClass){
        Block parent = getParent();
        while (parent!=null){
            if(parent.getClass() == parentClass){
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return ObjectsUtil.cast(null);
    }
    @SuppressWarnings("unchecked")
    public final <T> T getParentInstance(Class<T> parentClass){
        Block parent = getParent();
        while (parent != null){
            if(parentClass.isInstance(parent)){
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return ObjectsUtil.cast(null);
    }


    public static int getInteger(byte[] bytes, int offset){
        if((offset + 4) > bytes.length){
            return 0;
        }
        return bytes[offset] & 0xff |
                (bytes[offset + 1] & 0xff) << 8 |
                (bytes[offset + 2] & 0xff) << 16 |
                (bytes[offset + 3] & 0xff) << 24;
    }
    public static short getShort(byte[] bytes, int offset){
        if((offset + 2) > bytes.length){
            return 0;
        }
        return (short) (bytes[offset] & 0xff
                | (bytes[offset + 1] & 0xff) << 8);
    }
    public static int getShortUnsigned(byte[] bytes, int offset){
        if((offset + 2) > bytes.length){
            return 0;
        }
        return (bytes[offset] & 0xff
                | (bytes[offset + 1] & 0xff) << 8);
    }
    public static void putInteger(byte[] bytes, int offset, int val){
        if((offset + 4) > bytes.length){
            return;
        }
        bytes[offset + 3]= (byte) (val >>> 24 & 0xff);
        bytes[offset + 2]= (byte) (val >>> 16 & 0xff);
        bytes[offset + 1]= (byte) (val >>> 8 & 0xff);
        bytes[offset]= (byte) (val & 0xff);
    }
    public static void putShort(byte[] bytes, int offset, short val){
        bytes[offset + 1]= (byte) (val >>> 8 & 0xff);
        bytes[offset]= (byte) (val & 0xff);
    }
    public static void putShort(byte[] bytes, int offset, int value){
        bytes[offset + 1]= (byte) (value >>> 8 & 0xff);
        bytes[offset]= (byte) (value & 0xff);
    }

    public static int getNibbleUnsigned(byte[] bytes, int index){
        int i = bytes[(index / 2)] & 0xff;
        int shift = (index % 2) * 4;
        return (i >> shift) & 0x0f;
    }
    public static void putNibbleUnsigned(byte[] bytes, int index, int value){
        if((value & 0x0f) != value){
            throw new IllegalArgumentException("Nibble value out of range "
                    + HexUtil.toHex(value, 1) + " > 0xf");
        }
        int i = index / 2;
        int half = bytes[i] & 0xff;
        int shift = (index % 2) * 4;
        int mask = 0x0f;
        if(shift == 0){
            mask = 0xf0;
        }
        int result = (value << shift) | (half & mask);
        bytes[i] = (byte) result;
    }
    public static boolean getBit(byte[] bytes, int byteOffset, int bitIndex){
        return (((bytes[byteOffset] & 0xff) >>bitIndex) & 0x1) == 1;
    }
    public static void putBit(byte[] bytes, int byteOffset, int bitIndex, boolean bit){
        int mask = 1 << bitIndex;
        int add = bit ? mask : 0;
        mask = (~mask) & 0xff;
        int value = (bytes[byteOffset] & mask) | add;
        bytes[byteOffset] = (byte) value;
    }
    public static long getLong(byte[] bytes, int offset){
        if((offset + 8) > bytes.length){
            return 0;
        }
        long result = 0;
        int index = offset + 7;
        while (index >= offset){
            result = result << 8;
            result |= (bytes[index] & 0xff);
            index --;
        }
        return result;
    }
    public static void putLong(byte[] bytes, int offset, long value){
        if((offset + 8) > bytes.length){
            return;
        }
        int index = offset;
        offset = index + 8;
        while (index < offset){
            bytes[index] = (byte) (value & 0xff);
            value = value >>> 8;
            index++;
        }
    }
    public static int getBigEndianShort(byte[] bytes, int offset) {
        if((offset + 2) > bytes.length) {
            return 0;
        }
        return (bytes[offset] & 0xff) << 8 | bytes[offset + 1] & 0xff;
    }
    public static void putBigEndianShort(byte[] bytes, int offset, int value) {
        bytes[offset]= (byte) (value >>> 8 & 0xff);
        bytes[offset + 1]= (byte) (value & 0xff);
    }
    public static int getBigEndianInteger(byte[] bytes, int offset) {
        if((offset + 4) > bytes.length) {
            return 0;
        }
        return bytes[offset + 3] & 0xff |
                (bytes[offset + 2] & 0xff) << 8 |
                (bytes[offset + 1] & 0xff) << 16 |
                (bytes[offset] & 0xff) << 24;
    }
    public static void putBigEndianInteger(byte[] bytes, int offset, int value) {
        if((offset + 4) > bytes.length) {
            return;
        }
        bytes[offset]= (byte) (value >>> 24 & 0xff);
        bytes[offset + 1]= (byte) (value >>> 16 & 0xff);
        bytes[offset + 2]= (byte) (value >>> 8 & 0xff);
        bytes[offset + 3]= (byte) (value & 0xff);
    }
    public static long getBigEndianLong(byte[] bytes, int offset){
        if((offset + 8) > bytes.length){
            return 0;
        }
        long result = 0;
        int index = offset;
        offset = offset + 8;
        while (index < offset){
            result = result << 8;
            result |= (bytes[index] & 0xff);
            index ++;
        }
        return result;
    }
    public static void putBigEndianLong(byte[] bytes, int offset, long value){
        if((offset + 8) > bytes.length){
            return;
        }
        int index = offset + 7;
        while (index >= offset) {
            bytes[index] = (byte) (value & 0xff);
            value = value >>> 8;
            index --;
        }
    }
    public static byte[] getBytes(byte[] bytes, int offset, int length){
        if(bytes.length == 0){
            return new byte[0];
        }
        int available = bytes.length - offset;
        if(available < 0){
            available = 0;
        }
        if(length > available){
            length = available;
        }
        byte[] result = new byte[length];
        System.arraycopy(bytes, offset, result, 0, length);
        return result;
    }
    protected static byte[] addBytes(byte[] bytes1, byte[] bytes2){
        boolean empty1 = (bytes1 == null || bytes1.length == 0);
        boolean empty2 = (bytes2 == null || bytes2.length == 0);
        if(empty1 && empty2){
            return null;
        }
        if(empty1){
            return bytes2;
        }
        if(empty2){
            return bytes1;
        }
        int length = bytes1.length + bytes2.length;
        byte[] result = new byte[length];
        int start = bytes1.length;
        System.arraycopy(bytes1, 0, result, 0, start);
        System.arraycopy(bytes2, 0, result, start, bytes2.length);
        return result;
    }

    public static boolean areEqual(Block[] blocks1, Block[] blocks2){
        if(blocks1 == blocks2){
            return true;
        }
        if(blocks1 == null){
            return blocks2.length == 0;
        }
        int length = blocks1.length;
        if(length != blocks2.length){
            return false;
        }
        for(int i = 0; i < length; i++){
            if(!areEqual(blocks1[i], blocks2[i])){
                return false;
            }
        }
        return true;
    }
    public static boolean areEqual(Block block1, Block block2){
        if(block1 == block2){
            return true;
        }
        byte[] bytes1 = block1 == null ? null : block1.getBytes();
        byte[] bytes2 = block2 == null ? null : block2.getBytes();
        return areEqual(bytes1, bytes2);
    }
    public static boolean areEqual(byte[] bytes1, byte[] bytes2){
        if(bytes1 == bytes2){
            return true;
        }
        if(bytes1 == null){
            return bytes2.length == 0;
        }
        int length = bytes1.length;
        if(length != bytes2.length){
            return false;
        }
        for(int i = 0; i < length; i++){
            if(bytes1[i] != bytes2[i]){
                return false;
            }
        }
        return true;
    }
    public static int hashCodeOf(Block[] blocks){
        if(blocks == null){
            return 0;
        }
        return hashCodeOf(blocks, 0, blocks.length);
    }
    public static int hashCodeOf(Block[] blocks, int start, int length){
        if(blocks == null || blocks.length == 0){
            return 0;
        }
        int hash = 1;
        for(int i = start; i < length; i++){
            hash = hash * 31 + hashCodeOf(blocks[i]);
        }
        return hash;
    }
    public static int hashCodeOf(Block block){
        if(block == null){
            return 0;
        }
        return hashCodeOf(block.getBytes());
    }
    public static int hashCodeOf(byte[] bytes){
        if(bytes == null){
            return 0;
        }
        int length = bytes.length;
        if(length == 0){
            return 0;
        }
        int hash = 1;
        for(int i = 0; i < length; i++){
            hash = hash * 31 + (bytes[i] & 0xff);
        }
        return hash;
    }
    public static long longHashCode(byte[] bytes){
        if(bytes == null){
            return 0;
        }
        int length = bytes.length;
        if(length == 0){
            return 0;
        }
        long hash = 1;
        for(int i = 0; i < length; i++){
            hash = hash * 31 + (bytes[i] & 0xff);
        }
        return hash;
    }
}
