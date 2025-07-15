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

import java.io.IOException;
import java.io.InputStream;

public class SlicedInputStream extends InputStream{
    private final InputStream inputStream;
    private final long mOffset;
    private final long mLength;
    private long mCount;
    private boolean mFinished;
    private boolean mStarted;
    public SlicedInputStream(InputStream inputStream, long offset, long length){
        this.inputStream = inputStream;
        this.mOffset = offset;
        this.mLength = length;
    }
    @Override
    public int read(byte[] bytes, int off, int len) throws IOException{
        if(mFinished){
            return -1;
        }
        checkStarted();
        long remain = mLength - mCount;
        if(remain <= 0){
            onFinished();
            return -1;
        }
        boolean finishNext = false;
        if(len > remain){
            len = (int) remain;
            finishNext = true;
        }
        int read = inputStream.read(bytes, off, len);
        mCount += read;
        if(finishNext){
            onFinished();
        }
        return read;
    }
    @Override
    public int read(byte[] bytes) throws IOException{
        return this.read(bytes, 0, bytes.length);
    }
    @Override
    public int read() throws IOException {
        if(mFinished){
            return -1;
        }
        checkStarted();
        long remain = mLength - mCount;
        if(remain <= 0){
            onFinished();
            return -1;
        }
        int result = inputStream.read();
        mCount = mCount + 1;
        if(remain == 1){
            onFinished();
        }
        return result;
    }
    @Override
    public long skip(long n) throws IOException{
        checkStarted();
        long amount = inputStream.skip(n);
        if(amount>0){
            mCount += amount;
        }
        return amount;
    }
    @Override
    public void close() throws IOException {
        onFinished();
    }
    private void onFinished() throws IOException {
        mFinished = true;
        inputStream.close();
    }
    private void checkStarted() throws IOException {
        if(mStarted){
            return;
        }
        mStarted = true;
        inputStream.skip(mOffset);
        mCount = 0;
    }
    @Override
    public String toString(){
        return "["+mOffset+","+mLength+"] "+mCount;
    }
}
