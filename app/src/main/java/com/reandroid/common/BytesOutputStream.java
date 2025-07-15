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

import java.io.*;

public class BytesOutputStream extends ByteArrayOutputStream {
    private int mLastGrow;
    public BytesOutputStream(int initialCapacity){
        super(check(initialCapacity));
    }
    private static int check(int i){
        if(i < 0){
            throw new IllegalArgumentException("Negative: " + i);
        }
        return i;
    }
    public BytesOutputStream(){
        this(32);
    }

    public int position() {
        return size();
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if(len == 0){
            return;
        }
        this.ensureCapacity(this.count + len);
        System.arraycopy(b, off, this.buf, this.count, len);
        this.count += len;
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - this.buf.length > 0) {
            this.grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        if(mLastGrow == 0){
            mLastGrow = 1;
            mLastGrow = mLastGrow << 1;
        }
        mLastGrow = mLastGrow << 1;
        if(mLastGrow > 0xffff){
            mLastGrow = 0xffff;
        }
        byte[] buf = this.buf;
        int oldCapacity = buf.length;
        int newCapacity = minCapacity + mLastGrow;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        byte[] bytes = new byte[newCapacity];
        for(int i = 0; i < oldCapacity; i ++){
            bytes[i] = buf[i];
        }
        this.buf = bytes;
    }

    public void write(InputStream inputStream) throws IOException{
        if(inputStream instanceof BytesInputStream){
            write((BytesInputStream) inputStream);
            return;
        }
        int bufferStep = 500;
        int maxBuffer = 4096 * 20;
        int length;
        byte[] buffer = new byte[2048];
        while ((length = inputStream.read(buffer, 0, buffer.length)) >= 0){
            write(buffer, 0, length);
            if(buffer.length < maxBuffer){
                buffer = new byte[buffer.length + bufferStep];
            }
        }
        inputStream.close();
    }
    public void write(BytesInputStream bis) throws IOException {
        byte[] bytes = bis.toByteArray();
        write(bytes, 0, bytes.length);
    }

    @Override
    public synchronized byte[] toByteArray() {
        int count = this.count;
        byte[] buf = this.buf;
        if(count == buf.length){
            return buf;
        }
        if(count == 0){
            buf = new byte[0];
            this.buf = buf;
            return buf;
        }
        byte[] results = new byte[count];
        for(int i = 0; i < count; i++){
            results[i] = buf[i];
        }
        this.buf = results;
        return results;
    }

    @Override
    public void close() throws IOException {
        super.close();
        toByteArray();
    }

    @Override
    public String toString(){
        return "pos = " + size();
    }
}
