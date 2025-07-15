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

import com.reandroid.arsc.base.BlockContainer;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.HexBytesWriter;
import com.reandroid.utils.HexUtil;

import java.io.*;
import java.util.List;

public class HeaderBlock extends ExpandableBlockContainer implements BlockLoad {
    private final ShortItem mType;
    private final ShortItem mHeaderSize;
    private final IntegerItem mChunkSize;
    private HeaderLoaded mHeaderLoaded;
    private final ByteArray extraBytes;
    public HeaderBlock(short type){
        super(3);
        this.mType=new ShortItem(type);
        this.mHeaderSize=new ShortItem();
        this.mChunkSize=new IntegerItem();
        this.extraBytes=new ByteArray();
        addChild(mType);
        addChild(mHeaderSize);
        addChild(mChunkSize);
        this.mType.setBlockLoad(this);
        this.mHeaderSize.setBlockLoad(this);
        this.mChunkSize.setBlockLoad(this);
    }
    public HeaderBlock(ChunkType chunkType){
        this(chunkType.ID);
    }
    public int getMinimumSize(){
        return countBytes();
    }
    public ByteArray getExtraBytes() {
        return extraBytes;
    }
    public void setHeaderLoaded(HeaderLoaded headerLoaded){
        this.mHeaderLoaded=headerLoaded;
    }
    public ChunkType getChunkType(){
        return ChunkType.get(mType.getShort());
    }
    public short getType(){
        return mType.getShort();
    }
    public void setType(ChunkType chunkType){
        short type;
        if(chunkType==null){
            type=0;
        }else {
            type=chunkType.ID;
        }
        setType(type);
    }
    public void setType(short type){
        mType.set(type);
    }

    public int getHeaderSize(){
        return mHeaderSize.unsignedInt();
    }
    public void setHeaderSize(short headerSize){
        mHeaderSize.set(headerSize);
    }
    public int getChunkSize(){
        return mChunkSize.get();
    }
    public void setChunkSize(int chunkSize){
        mChunkSize.set(chunkSize);
    }

    public final void refreshHeader(){
        refreshHeaderSize();
        refreshChunkSize();
    }
    private void refreshHeaderSize(){
        setHeaderSize((short)countBytes());
    }
    private void refreshChunkSize(){
        Block parent=getParent();
        if(parent==null){
            return;
        }
        int count=parent.countBytes();
        setChunkSize(count);
    }
    /**Non buffering reader*/
    public int readBytes(InputStream inputStream) throws IOException{
        int result = onReadBytes(inputStream);
        super.notifyBlockLoad();
        return result;
    }
    private int onReadBytes(InputStream inputStream) throws IOException {
        int readCount = readBytes(inputStream, this);
        int difference = getHeaderSize() - readCount;
        initExtraBytes(this.extraBytes, difference);
        if(this.extraBytes.size()>0){
            readCount += extraBytes.readBytes(inputStream);
        }
        return readCount;
    }
    private int readBytes(InputStream inputStream, Block block) throws IOException{
        int result=0;
        if(block instanceof BlockItem){
            result = ((BlockItem)block).readBytes(inputStream);
        }else if(block instanceof BlockList){
            List<? extends Block> childes=
                    ((BlockList<? extends Block>) block).getChildes();
            for(Block child:childes){
                result+=readBytes(inputStream, child);
            }
        }else if(block instanceof BlockContainer){
            Block[] childes =
                    ((BlockContainer<? extends Block>) block).getChildes();
            for(Block child:childes){
                result+=readBytes(inputStream, child);
            }
        }else {
            throw new IOException("Can not read block type: "+block.getClass());
        }
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int start=reader.getPosition();
        super.onReadBytes(reader);
        int readActual=reader.getPosition() - start;
        int difference=getHeaderSize()-readActual;
        initExtraBytes(this.extraBytes, difference);
        if(this.extraBytes.size()>0){
            this.extraBytes.readBytes(reader);
        }
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==this.mType){
            onChunkTypeLoaded(mType.getShort());
        }else if(sender==this.mHeaderSize){
            onHeaderSizeLoaded(mHeaderSize.unsignedInt());
        }else if(sender==this.mChunkSize){
            onChunkSizeLoaded(mHeaderSize.unsignedInt(),
                    mChunkSize.get());
        }
    }

    @Override
    protected void onRefreshed() {
        // Not required, the parent should call refreshHeader()
    }
    @Override
    protected void refreshChildes(){
        // Not required
    }
    void initExtraBytes(ByteArray extraBytes, int difference){
        if(difference==0){
            return;
        }
        if(extraBytes.getParent()==null){
            addChild(extraBytes);
        }
        extraBytes.setSize(difference);
    }
    void onChunkTypeLoaded(short chunkType){
        HeaderLoaded headerLoaded = mHeaderLoaded;
        if(headerLoaded!=null){
            headerLoaded.onChunkTypeLoaded(chunkType);
        }
    }
    void onHeaderSizeLoaded(int size){
        HeaderLoaded headerLoaded = mHeaderLoaded;
        if(headerLoaded!=null){
            headerLoaded.onHeaderSizeLoaded(size);
        }
    }
    void onChunkSizeLoaded(int headerSize, int chunkSize){
        HeaderLoaded headerLoaded = mHeaderLoaded;
        if(headerLoaded!=null){
            headerLoaded.onChunkSizeLoaded(headerSize, chunkSize);
        }
    }
    /**
     * Prints bytes in hex for debug/testing
     * */
    public String toHex(){
        return HexBytesWriter.toHex(getBytes());
    }
    @Override
    public String toString(){
        short t = getType();
        ChunkType type = ChunkType.get(t);
        StringBuilder builder = new StringBuilder();
        if(type!=null){
            builder.append(type.toString());
        }else {
            builder.append("Unknown type=");
            builder.append(HexUtil.toHex4(t));
        }
        builder.append("{ValueHeader=");
        builder.append(getHeaderSize());
        builder.append(", Chunk=");
        builder.append(getChunkSize());
        builder.append("}");
        return builder.toString();
    }

    public interface HeaderLoaded{
        void onChunkTypeLoaded(short type);
        void onHeaderSizeLoaded(int headerSize);
        void onChunkSizeLoaded(int headerSize, int chunkSize);
    }
}
