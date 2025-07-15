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
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream<T extends InputStream> extends InputStream {
    private final T inputStream;
    private final CRCDigest crc;
    private long size;
    private long mCheckSum;
    private boolean mFinished;
    public CountingInputStream(T inputStream, boolean disableCrc){
        this.inputStream = inputStream;
        CRCDigest crc32;
        if(disableCrc){
            crc32 = null;
        }else {
            crc32 = new CRCDigest();
        }
        this.crc = crc32;
    }
    public CountingInputStream(T inputStream){
        this(inputStream, false);
    }
    public T getInputStream() {
        return inputStream;
    }
    public long getSize() {
        return size;
    }
    public long getCrc() {
        return mCheckSum;
    }
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException{
        if(mFinished){
            return -1;
        }
        length = inputStream.read(bytes, offset, length);
        if(length < 0){
            onFinished();
            return length;
        }
        this.size += length;
        if(this.crc != null){
            this.crc.update(bytes, offset, length);
        }
        return length;
    }
    @Override
    public int read(byte[] bytes) throws IOException{
        return this.read(bytes, 0, bytes.length);
    }
    @Override
    public int read() throws IOException {
        throw new IOException("Why one byte ?");
    }
    @Override
    public long skip(long amount) throws IOException {
        if(mFinished){
            return 0;
        }
        if(amount <= 0){
            return amount;
        }
        InputStream inputStream = this.inputStream;
        if(inputStream instanceof CountingInputStream){
            return inputStream.skip(amount);
        }
        long remaining = amount;
        int len = 1024 * 1000;
        if(remaining < len){
            len = (int) remaining;
        }
        final byte[] buffer = new byte[len];
        int read;
        while (true){
            read = inputStream.read(buffer, 0, len);
            if(read < 0){
                onFinished();
                break;
            }
            remaining = remaining - read;
            if(remaining <= 0){
                break;
            }
            if(remaining < len){
                len = (int) remaining;
            }
        }
        return amount - remaining;
    }
    @Override
    public void close() throws IOException{
        if(!mFinished){
            onFinished();
        }
        inputStream.close();
    }
    private void onFinished(){
        this.mFinished = true;
        if(this.crc!=null){
            this.mCheckSum = this.crc.getValue();
        }
    }
    @Override
    public String toString(){
        if(!mFinished || crc==null){
            return "[" + size + "]: " + inputStream.getClass().getSimpleName();
        }
        return "[size=" + size +", crc=" + HexUtil.toHex8(mCheckSum) + "]: " + inputStream.getClass().getSimpleName();
    }
}
