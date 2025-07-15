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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;

import java.io.IOException;

public abstract class Chunk<T extends HeaderBlock> extends ExpandableBlockContainer {

    private final T mHeaderBlock;
    protected final SingleBlockContainer<Block> firstPlaceHolder;
    private AlignItem alignItem;

    protected Chunk(T headerBlock, int initialChildesCount) {
        super(initialChildesCount + 3);
        this.mHeaderBlock = headerBlock;
        this.firstPlaceHolder = new SingleBlockContainer<>();
        addChild(headerBlock);
        addChild(firstPlaceHolder);
    }

    public AlignItem getAlignItem() {
        AlignItem alignItem = this.alignItem;
        if(alignItem == null) {
            alignItem = new AlignItem();
            addChild(alignItem);
            this.alignItem = alignItem;
        }
        return alignItem;
    }

    public SingleBlockContainer<Block> getFirstPlaceHolder() {
        return firstPlaceHolder;
    }
    void setHeaderLoaded(HeaderBlock.HeaderLoaded headerLoaded){
        getHeaderBlock().setHeaderLoaded(headerLoaded);
    }
    public final T getHeaderBlock(){
        return mHeaderBlock;
    }
    @Override
    protected final void onRefreshed() {
        updateAlign();
        getHeaderBlock().refreshHeader();
        onChunkRefreshed();
    }

    private void updateAlign() {
        AlignItem alignItem = getAlignItem();
        alignItem.setSize(0);
        alignItem.align(this);
    }
    protected abstract void onChunkRefreshed();
    public void onChunkLoaded(){

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        checkInvalidChunk(headerBlock);
        BlockReader chunkReader = reader.create(headerBlock.getChunkSize());
        onReadChildes(chunkReader);
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    protected void onReadChildes(BlockReader reader) throws IOException{
        AlignItem alignItem = getAlignItem();
        alignItem.setSize(0);
        super.onReadBytes(reader);
        alignItem.alignSafe(reader);
    }
    void checkInvalidChunk(HeaderBlock headerBlock) throws IOException {
        ChunkType chunkType = headerBlock.getChunkType();
        if(chunkType==null || chunkType==ChunkType.NULL){
            throw new IOException("Invalid chunk: "+headerBlock);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        builder.append(getHeaderBlock());
        return builder.toString();
    }
}
