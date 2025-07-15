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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerArrayBlock;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.common.IntegerArray;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.base.PositionedItem;

import java.io.IOException;

public class IntegerList extends DataItem implements
        IntegerArray, PositionedItem, OffsetSupplier, OffsetReceiver {

    private final DexPositionAlign positionAlign;
    private final IntegerReference itemCount;
    private final IntegerArray arrayBlock;

    IntegerList(int childesCount, IntegerArray arrayBlock, DexPositionAlign positionAlign){
        super(childesCount + 3);
        this.positionAlign = positionAlign;
        this.itemCount = new IntegerItem();
        this.arrayBlock = arrayBlock;
        addChild(0, (Block) itemCount);
        addChild(1, (Block) arrayBlock);
        addChild(2, positionAlign);
    }

    public IntegerList(IntegerReference itemCount){
        super(1);
        this.itemCount = itemCount;
        this.arrayBlock = new IntegerArrayBlock();
        addChild(0, (Block) arrayBlock);
        this.positionAlign = null;
    }
    public IntegerList(){
        this(0, new IntegerArrayBlock(), new DexPositionAlign());
    }
    public IntegerList(DexPositionAlign positionAlign){
        this(0, new IntegerArrayBlock(), positionAlign);
    }

    public DexPositionAlign getPositionAlign() {
        return positionAlign;
    }

    public int[] toArray(){
        return IntegerArray.toArray(arrayBlock);
    }
    public boolean removeValue(int value){
        return remove(indexOf(value));
    }
    public boolean remove(int index){
        if(index < 0){
            return false;
        }
        int size = size();
        if(index >= size){
            return false;
        }
        size = size - 1;
        for(int i = index; i < size; i++){
            put(i, get(i + 1));
        }
        setSize(size);
        return true;
    }
    public int indexOf(int value){
        int size = size();
        for(int i = 0; i < size; i++){
            if(value == get(i)){
                return i;
            }
        }
        return -1;
    }
    public boolean addIfAbsent(int value){
        if(indexOf(value) < 0){
            add(value);
            return true;
        }
        return false;
    }
    public void add(int value){
        int index = size();
        setSize(index + 1, false);
        put(index, value);
        onChanged();
    }
    public void put(int index, int value){
        arrayBlock.put(index, value);
    }
    @Override
    public int get(int i) {
        return arrayBlock.get(i);
    }
    @Override
    public int size() {
        return arrayBlock.size();
    }
    @Override
    public void setSize(int size){
        setSize(size, true);
    }
    void setSize(int size, boolean notify){
        int old = size();
        arrayBlock.setSize(size);
        itemCount.set(size);
        if(notify && old != size){
            onChanged();
        }
    }
    void ensureSize(int size){
        if(size > size()){
            setSize(size, false);
        }
    }
    void onChanged(){
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        IntegerReference itemCount = this.itemCount;
        if(itemCount instanceof Block){
            Block block = (Block) itemCount;
            if(block.getParent() == this){
                int position = reader.getPosition();
                block.readBytes(reader);
                reader.seek(position);
            }
        }
        arrayBlock.setSize(itemCount.get());
        super.onReadBytes(reader);
        onChanged();
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        IntegerReference ref = getOffsetReference();
        if(ref != null){
            builder.append("offset=");
            builder.append(ref.get());
            builder.append(", ");
        }
        if(arrayBlock.size() != itemCount.get()) {
            builder.append("count=");
            builder.append(itemCount);
            builder.append(", ");
        }
        builder.append(IntegerArray.toString(this));
        return builder.toString();
    }
}
