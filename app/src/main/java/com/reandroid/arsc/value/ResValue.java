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
package com.reandroid.arsc.value;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;

public class ResValue extends ValueItem  {

    public ResValue() {
        super(8, OFFSET_SIZE);
    }

    public boolean isCompact(){
        return getHeader().isCompact();
    }
    public void setCompact(boolean compact){
        EntryHeader header = getHeader();
        if(compact == header.isCompact()){
            return;
        }
        byte type = getType();
        int data = getData();
        updateBytesLength(compact);
        header.setCompact(compact);
        setType(type);
        setData(data);
        if(compact){
            setRes0((byte) 0);
        }
    }

    @Override
    public int getSize() {
        if(isCompact()){
            return 0;
        }
        return super.getSize();
    }
    @Override
    public void setSize(int size) {
        if(!isCompact()){
            super.setSize(size);
        }
    }
    @Override
    void updateSize() {
        if(!isCompact()){
            super.updateSize();
        }
    }
    private void updateBytesLength(boolean compact){
        int length;
        if(compact){
            length = 0;
        }else {
            length = 8;
        }
        setBytesLength(length, false);
    }
    private EntryHeader getHeader(){
        ResTableEntry resTableEntry = getParent(ResTableEntry.class);
        if(resTableEntry != null){
            return resTableEntry.getHeader();
        }
        throw new RuntimeException("Unreachable");
    }

    public Entry getEntry(){
        return getParent(Entry.class);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean compact = isCompact();
        updateBytesLength(compact);
        if(!compact){
            super.onReadBytes(reader);
        }
    }
    @Override
    public int getData() {
        if(isCompact()){
            return getHeader().getData();
        }
        return super.getData();
    }
    @Override
    void writeData(int data) {
        if(isCompact()){
            getHeader().setData(data);
        }else {
            super.writeData(data);
        }
    }

    @Override
    public byte getType() {
        if(isCompact()){
            return getHeader().getType();
        }
        return super.getType();
    }

    @Override
    public void setType(byte type) {
        if(isCompact()){
            getHeader().setType(type);
        }else {
            super.setType(type);
        }
    }

    @Override
    public PackageBlock getParentChunk(){
        Entry entry = getEntry();
        if(entry != null){
            return entry.getPackageBlock();
        }
        return null;
    }

    private static final int OFFSET_SIZE = 0;
}
