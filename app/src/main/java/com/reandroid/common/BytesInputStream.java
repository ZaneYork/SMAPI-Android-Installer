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
package com.reandroid.common;

import java.io.IOException;
import java.io.InputStream;

public class BytesInputStream extends InputStream {
    private final byte[] array;
    private final int offset;
    private final int length;
    private int position;
    private int mark;
    public BytesInputStream(byte[] array, int offset, int length){
        if(offset >= array.length){
            offset = array.length - 1;
        }
        if(offset < 0){
            offset = 0;
        }
        int available = array.length - offset;
        if(length > available){
            length = available;
        }
        this.array = array;
        this.offset = offset;
        this.length = length;
    }
    public BytesInputStream(byte[] array){
        this(array, 0, array.length);
    }
    public int getOffset() {
        return offset;
    }
    public int getLength() {
        return length;
    }
    public byte[] toByteArray(){
        if(offset == 0 && position == 0 && length == array.length){
            return array;
        }
        int size = length - position;
        byte[] bytes = new byte[size];
        System.arraycopy(array, offset + position, bytes, 0, size);
        return bytes;
    }
    public byte[] getArray() {
        return array;
    }

    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public synchronized void mark(int readLimit){
        mark = readLimit;
    }
    @Override
    public void close() throws IOException {
        position = length;
    }
    @Override
    public void reset() throws IOException {
        position = mark;
    }
    @Override
    public long skip(long amount) throws IOException{
        if(amount <= 0){
            return amount;
        }
        int skip = (int) amount;
        int available = available();
        if(skip > available){
            skip = available;
        }
        position += skip;
        return skip;
    }
    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException{
        if(length == 0){
            return 0;
        }
        int available = available();
        if(available <= 0){
            return -1;
        }
        if(length > available){
            length = available;
        }
        System.arraycopy(array, this.offset + this.position, bytes, offset, length);
        this.position += length;
        return length;
    }
    @Override
    public int read() throws IOException {
        if(available() <= 0){
            return -1;
        }
        int i = this.array[offset + position];
        position++;
        return i & 0xff;
    }
    @Override
    public int available(){
        return length - position;
    }
}
