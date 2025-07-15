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
import com.reandroid.arsc.base.BlockCreator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.LongItem;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class LengthPrefixedList<T extends Block> extends FixedBlockContainer
        implements BlockCreator<T>, Iterable<T> {
    private final Block numberBlock;
    private final BlockList<T> elements;
    private final SingleBlockContainer<Block> bottomContainer;
    public LengthPrefixedList(boolean is_long){
        super(3);
        Block numberBlock;
        if(is_long){
            numberBlock = new LongItem();
        }else {
            numberBlock = new IntegerItem();
        }
        this.numberBlock = numberBlock;
        this.elements = new BlockList<>();
        this.bottomContainer = new SingleBlockContainer<>();
        addChild(0, this.numberBlock);
        addChild(1, this.elements);
        addChild(2, this.bottomContainer);
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
    public int size(){
        return elements.size();
    }
    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }
    public List<T> getElements() {
        return elements.getChildes();
    }
    public T add(T element){
        this.elements.add(element);
        return element;
    }
    public boolean remove(T element){
        return this.elements.remove(element);
    }
    public void sort(Comparator<T> comparator){
        this.elements.sort(comparator);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        if(!reader.isAvailable()){
            return;
        }
        numberBlock.readBytes(reader);
        int totalSize = (int) getDataSize();
        if(totalSize <= 0){
            return;
        }
        BlockReader chunkReader = reader.create(totalSize);
        readElements(chunkReader);
        bottomContainer.readBytes(chunkReader);
        reader.offset(totalSize);
    }
    private void readElements(BlockReader reader) throws IOException{
        int preserve = bottomContainer.countBytes() + 4;
        while (reader.available() > preserve){
            int position = reader.getPosition();
            T element = newInstance();
            element = add(element);
            element.readBytes(reader);
            if(position == reader.getPosition()){
                break;
            }
        }
    }
    @Override
    protected void onRefreshed(){
        int size = countBytes() - numberBlock.countBytes();
        setDataSize(size);
    }
    public Block getBottomBlock(){
        return bottomContainer.getItem();
    }
    public void setBottomBlock(Block block){
        bottomContainer.setItem(block);
    }


    @Override
    public String toString(){
        return "size=" + numberBlock + ", count=" + size();
    }
}
