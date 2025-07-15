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
package com.reandroid.archive.block;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.LongItem;

import java.io.IOException;

public class LengthPrefixedBlock extends ExpandableBlockContainer{
    private final Block numberBlock;
    public LengthPrefixedBlock(int childesCount, boolean is_long) {
        super(1 + childesCount);
        Block numberBlock;
        if(is_long){
            numberBlock = new LongItem();
        }else {
            numberBlock = new IntegerItem();
        }
        this.numberBlock = numberBlock;
        addChild(this.numberBlock);
    }
    public long getDataSize(){
        Block numberBlock = this.numberBlock;
        if(numberBlock instanceof LongItem){
            return ((LongItem)numberBlock).getLong();
        }
        return ((IntegerItem)numberBlock).get();
    }
    public void setDataSize(long dataSize){
        Block numberBlock = this.numberBlock;
        if(numberBlock instanceof LongItem){
            ((LongItem)numberBlock).set(dataSize);
        }else {
            ((IntegerItem)numberBlock).set((int) dataSize);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if(!reader.isAvailable()){
            return;
        }
        Block numberBlock = this.numberBlock;
        numberBlock.readBytes(reader);
        int dataSize = (int) getDataSize();
        if(dataSize <= 0){
            onSizeLoaded(0);
            return;
        }
        onSizeLoaded(dataSize);
        BlockReader chunkReader = reader.create(dataSize);
        Block[] childes = getChildes();
        for(int i=0;i<childes.length;i++){
            Block child = childes[i];
            if(child == numberBlock){
                continue;
            }
            child.readBytes(chunkReader);
        }
        reader.offset(dataSize);
    }
    protected void onSizeLoaded(int dataSize){
    }
    @Override
    protected void onRefreshed(){
        int size = countBytes() - numberBlock.countBytes();
        setDataSize(size);
    }
    @Override
    public String toString(){
        return "size=" + numberBlock;
    }
}
