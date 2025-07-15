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
package com.reandroid.dex.base;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public class IndexAndOffsetArray extends DexBlockItem implements OffsetArray {
    private final IntegerReference itemCount;
    public IndexAndOffsetArray(IntegerReference itemCount) {
        super(0);
        this.itemCount = itemCount;
    }
    public int[] getOffsetsForIndex(int index){
        int size = size();
        int[] tmp = new int[size];
        int count = 0;
        for(int i = 0; i < size; i++){
            if(index == getIndexEntry(i)){
                tmp[count] = getOffset(i);
                count++;
            }
        }
        int[] results = new int[count];
        for(int i = 0; i < count; i++){
            results[i] = tmp[i];
        }
        return results;
    }
    public IntegerPair[] toIntegerPairArray(){
        int size = size();
        IntegerPair[] results = new IntegerPair[size];
        for(int i = 0; i < size; i++){
            results[i] = get(i);
        }
        return results;
    }
    public IntegerPair get(int i){
        return new IndirectBlockIntegerPair(this, i * 8);
    }

    private int getIndexEntry(int i) {
        return Block.getInteger(getBytesInternal(), i * 8);
    }
    public void setIndexEntry(int index, int value) {
        Block.putInteger(getBytesInternal(), index * 8, value);
    }
    @Override
    public int getOffset(int i) {
        return Block.getInteger(getBytesInternal(), i * 8 + 4);
    }
    @Override
    public void setOffset(int index, int value) {
        Block.putInteger(getBytesInternal(), index * 8 + 4, value);
    }
    @Override
    public int[] getOffsets() {
        int size = size();
        int[] results = new int[size];
        for(int i = 0; i < size; i++){
            results[i] = getOffset(i);
        }
        return results;
    }
    public int[] getIndexEntries() {
        int size = size();
        int[] results = new int[size];
        for(int i = 0; i < size; i++){
            results[i] = getIndexEntry(i);
        }
        return results;
    }

    public int size(){
        return countBytes() / 8;
    }
    public void setSize(int size){
        if(size < 0){
            size = 0;
        }
        setBytesLength(size * 8, false);
        itemCount.set(size);
    }
    @Override
    public void clear() {
        setSize(0);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setBytesLength(itemCount.get() * 8, false);
        super.onReadBytes(reader);
    }
    @Override
    public String toString() {
        return StringsUtil.toString(toIntegerPairArray());
    }
}
