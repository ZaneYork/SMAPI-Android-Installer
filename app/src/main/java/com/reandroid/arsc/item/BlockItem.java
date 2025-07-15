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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BlockItem extends Block {

    private byte[] mBytes;

    public BlockItem(int bytesLength){
        super();
        if(bytesLength == 0){
            mBytes = EMPTY;
        }else {
            mBytes = new byte[bytesLength];
        }
    }
    protected void onBytesChanged(){
    }
    protected byte[] getBytesInternal() {
        return mBytes;
    }
    void setBytesInternal(byte[] bytes){
        setBytesInternal(bytes, true);
    }
    void setBytesInternal(byte[] bytes, boolean notify){
        if(bytes == null || bytes.length == 0){
            bytes = EMPTY;
        }
        if(bytes == mBytes){
            return;
        }
        mBytes = bytes;
        if(notify){
            onBytesChanged();
        }
    }
    final void setBytesLength(int length){
        setBytesLength(length, true);
    }
    protected final void setBytesLength(int length, boolean notify){
        if(length < 0){
            length = 0;
        }
        if(length == 0){
            mBytes = EMPTY;
            if(notify){
                onBytesChanged();
            }
            return;
        }
        int old = mBytes.length;
        if(length == old){
            return;
        }
        byte[] bytes = new byte[length];
        if(length < old){
            old = length;
        }
        System.arraycopy(mBytes, 0, bytes, 0, old);
        mBytes = bytes;
        if(notify){
            onBytesChanged();
        }
    }
    int getBytesLength(){
        return mBytes.length;
    }

    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return getBytesInternal().length;
    }
    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return getBytesInternal();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        counter.setCurrent(this);
        counter.addCount(countBytes());
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        byte[] bytes = getBytesInternal();
        int length = bytes.length;
        if(length == 0){
            return;
        }
        reader.readFully(bytes);
        onBytesChanged();
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        byte[] bytes = getBytesInternal();
        int length = bytes.length;
        if(length == 0){
            return 0;
        }
        stream.write(bytes, 0, length);
        return length;
    }

    public int readBytes(InputStream inputStream) throws IOException {
        byte[] bytes=getBytesInternal();
        if(bytes == null || bytes.length==0){
            return 0;
        }
        int length = bytes.length;
        int offset = 0;
        int read = length;
        while (length > 0 && read > 0){
            read = inputStream.read(bytes, offset, length);
            length -= read;
            offset += read;
        }
        onBytesChanged();
        super.notifyBlockLoad();
        return offset;
    }
    public void setBytes(BlockItem blockItem){
        if(blockItem != this) {
            byte[] coming = blockItem.getBytesInternal();
            setBytesInternal(coming.clone());
        }
    }

    private static final byte[] EMPTY = new byte[0];
}
