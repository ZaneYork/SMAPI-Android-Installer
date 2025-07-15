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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.IndirectItem;
import com.reandroid.arsc.item.IntegerReference;

public class IndirectShort  extends IndirectItem<BlockItem> implements IntegerReference {
    public IndirectShort(BlockItem blockItem, int offset) {
        super(blockItem, offset);
    }
    @Override
    public int get(){
        return Block.getShortUnsigned(getBytesInternal(), getOffset());
    }
    @Override
    public void set(int value){
        Block.putShort(getBytesInternal(), getOffset(), value);
    }
    public boolean isNull(){
        return (getBytesInternal().length - getOffset()) < 2;
    }
    @Override
    public String toString(){
        if(isNull()){
            return "NULL";
        }
        return Integer.toString(get());
    }
}
