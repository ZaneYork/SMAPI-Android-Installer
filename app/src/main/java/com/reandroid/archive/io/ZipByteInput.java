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
package com.reandroid.archive.io;

import com.reandroid.common.BytesInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ZipByteInput extends ZipInput{
    private final byte[] array;
    private int position;
    private final int offset;
    private final int length;
    public ZipByteInput(byte[] array){
        this(array, 0, array.length);
    }
    public ZipByteInput(byte[] array, int offset, int length){
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
    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public void position(long pos) throws IOException {
        if(pos > getLength()){
            pos = getLength() - 1;
        }
        if(pos < 0){
            pos = 0;
        }
        this.position = (int) pos;
    }

    @Override
    public void close() throws IOException {
        position = length;
    }
    @Override
    public boolean isOpen() {
        return true;
    }
    @Override
    public long getLength() throws IOException {
        return length;
    }
    @Override
    public InputStream getInputStream(long offset, long length) throws IOException {
        return new BytesInputStream(this.array, (int)(offset + this.offset), (int)length);
    }
    @Override
    public byte[] getFooter(int minLength) {
        if(minLength <= 0){
            return new byte[0];
        }
        if(minLength > this.length){
            if(this.offset == 0){
                return array.clone();
            }
            minLength = this.length;
        }
        byte[] bytes = new byte[minLength];
        int offset = array.length - this.offset - minLength;
        System.arraycopy(array, offset, bytes, 0, minLength);
        return bytes;
    }
}
