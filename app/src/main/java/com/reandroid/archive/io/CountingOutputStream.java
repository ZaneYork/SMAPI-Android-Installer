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

import com.reandroid.utils.CRCDigest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CountingOutputStream<T extends OutputStream> extends OutputStream {
    private final T outputStream;
    private CRCDigest crc;
    private long size;
    private boolean mClosed;
    public CountingOutputStream(T outputStream, boolean disableCrc){
        this.outputStream = outputStream;
        CRCDigest crc32;
        if(disableCrc){
            crc32 = null;
        }else {
            crc32 = new CRCDigest();
        }
        this.crc = crc32;
    }
    public CountingOutputStream(T outputStream){
        this(outputStream, false);
    }

    public void disableCrc(boolean disableCrc) {
        if(!disableCrc){
            if(crc == null){
                this.crc = new CRCDigest();
            }
        }else{
            this.crc = null;
        }
    }

    public void reset(){
        this.crc = new CRCDigest();
        this.size = 0L;
    }
    public T getOutputStream() {
        return outputStream;
    }
    public long getSize() {
        return size;
    }
    public long getCrc() {
        if(crc != null){
            return crc.getValue();
        }
        return 0;
    }
    public void write(InputStream inputStream) throws IOException {
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
    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException{
        if(length == 0){
            return;
        }
        outputStream.write(bytes, offset, length);
        this.size += length;
        if(this.crc != null){
            this.crc.update(bytes, offset, length);
        }
    }
    @Override
    public void write(byte[] bytes) throws IOException{
        this.write(bytes, 0, bytes.length);
    }
    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte) i}, 0, 1);
    }
    @Override
    public void close() throws IOException{
        outputStream.close();
        mClosed = true;
    }
    public boolean isOpen(){
        return !mClosed;
    }
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }
    @Override
    public String toString(){
        return "[" + size + "]: " + outputStream.getClass().getSimpleName();
    }
}
