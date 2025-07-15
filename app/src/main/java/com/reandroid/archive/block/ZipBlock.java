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
package com.reandroid.archive.block;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;

import java.io.IOException;
import java.io.InputStream;

public abstract class ZipBlock extends BlockItem {
    public ZipBlock(int bytesLength) {
        super(bytesLength);
    }

    public void putBytes(byte[] bytes, int offset, int putOffset, int length){
        if(length<=0 || bytes.length==0){
            return;
        }
        int size = putOffset + length;
        if(size > countBytes()){
            setBytesLength(size, false);
        }
        System.arraycopy(bytes, offset, getBytesInternal(), putOffset, length);
    }
    @Override
    public abstract int readBytes(InputStream inputStream) throws IOException;

    // should not use this method
    @Override
    public void onReadBytes(BlockReader blockReader) throws IOException {
        this.readBytes((InputStream) blockReader);
    }

    byte[] getBytes(int offset, int length, boolean strict){
        byte[] bytes = getBytesInternal();
        if(strict){
            if(offset<0 || offset>=bytes.length || (offset + length)>bytes.length){
                return null;
            }
        }
        if(offset < 0){
            offset = 0;
        }
        int available = bytes.length - offset;
        if(length<=0 || available <=0){
            return new byte[0];
        }
        if(length > available){
            length = available;
        }
        byte[] result = new byte[length];
        System.arraycopy(getBytesInternal(), offset, result, 0, length);
        return result;
    }
    long getLong(int offset){
        return getLong(getBytesInternal(), offset);
    }
    void putLong(int offset, long value){
        putLong(getBytesInternal(), offset, value);
    }
    long getIntegerUnsigned(int offset){
        return getInteger(offset) & 0x00000000ffffffffL;
    }
    void putBit(int offset, int bitIndex, boolean bit){
        putBit(getBytesInternal(), offset, bitIndex, bit);
    }
    boolean getBit(int offset, int bitIndex){
        return getBit(getBytesInternal(), offset, bitIndex);
    }
    int getByteUnsigned(int offset){
        return getBytesInternal()[offset] & 0xff;
    }
    int getShortUnsigned(int offset){
        return getShort(getBytesInternal(), offset) & 0xffff;
    }
    int getInteger(int offset){
        return getInteger(getBytesInternal(), offset);
    }
    void putInteger(int offset, int value){
        putInteger(getBytesInternal(), offset, value);
    }
    void putInteger(int offset, long value){
        putInteger(getBytesInternal(), offset, (int) value);
    }
    void putShort(int offset, int value){
        putShort(getBytesInternal(), offset, (short) value);
    }

}
