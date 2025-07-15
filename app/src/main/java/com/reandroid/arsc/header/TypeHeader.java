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
package com.reandroid.arsc.header;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.arsc.value.ResConfig;

import java.io.IOException;

 public class TypeHeader extends HeaderBlock{
    private final ByteItem id;
    private final ByteItem flags;
    private final IntegerItem count;
    private final IntegerItem entriesStart;
    private final ResConfig config;
    public TypeHeader(boolean sparse, boolean offset16) {
        super(ChunkType.TYPE.ID);
        this.id = new ByteItem();
        this.flags = new ByteItem();
        ShortItem reserved = new ShortItem();
        this.count = new IntegerItem();
        this.entriesStart = new IntegerItem();
        this.config = new ResConfig();

        addChild(id);
        addChild(flags);
        addChild(reserved);
        addChild(count);
        addChild(entriesStart);
        addChild(config);
        setSparse(sparse);
        setOffset16(offset16);
    }
    @Deprecated
    public TypeHeader(boolean sparse) {
        this(sparse, false);
    }
    public boolean isSparse(){
        return (getFlags().getByte() & FLAG_SPARSE) == FLAG_SPARSE;
    }
    public void setSparse(boolean sparse){
        byte flag = getFlags().getByte();
        if(sparse){
            flag = (byte) (flag | FLAG_SPARSE);
        }else {
            flag = (byte) (flag & (~FLAG_SPARSE & 0xff));
        }
        getFlags().set(flag);
    }
    public boolean isOffset16(){
        return getFlags().getByte()  == FLAG_OFFSET16;
    }
    public void setOffset16(boolean offset16){
        byte flag = getFlags().getByte();
        if(offset16){
            flag = (byte) (flag | FLAG_OFFSET16);
        }else {
            flag = (byte) (flag & (~FLAG_OFFSET16 & 0xff));
        }
        getFlags().set(flag);
    }

    @Override
    public int getMinimumSize(){
        return TYPE_MIN_SIZE;
    }
    public ByteItem getId() {
        return id;
    }
    public ByteItem getFlags() {
        return flags;
    }
    public IntegerItem getCountItem() {
        return count;
    }
    public IntegerItem getEntriesStart() {
        return entriesStart;
    }
    public ResConfig getConfig() {
        return config;
    }

    @Override
    public String toString(){
        if(getChunkType()!=ChunkType.TYPE){
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {id="+getId().toHex()
                +", flags=" + getFlags().toHex()
                +", count=" + getCountItem()
                +", entriesStart=" + getEntriesStart()
                +", config=" + getConfig() + '}';
    }
    public static TypeHeader read(BlockReader reader) throws IOException {
        TypeHeader typeHeader = new TypeHeader(false, false);
        if(reader.available() < typeHeader.getMinimumSize()){
            throw new IOException("Too few bytes to read type header, available = " + reader.available());
        }
        int pos = reader.getPosition();
        typeHeader.readBytes(reader);
        reader.seek(pos);
        return typeHeader;
    }

    private static final byte FLAG_SPARSE = 0x1;
    private static final byte FLAG_OFFSET16 = 0x2;

    //typeHeader.countBytes() - getConfig().countBytes() + ResConfig.SIZE_16
    private static final int TYPE_MIN_SIZE = 36;
}
