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

import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.utils.HexUtil;

public class SpecFlag extends IndirectItem<SpecFlagsArray> {
    public SpecFlag(SpecFlagsArray specFlagsArray, int offset) {
        super(specFlagsArray, offset);
    }
    public byte getFlagByte(){
        return getBlockItem().getBytesInternal()[getOffset() + OFFSET_FLAG];
    }
    public void setFlagByte(byte flag){
        getBlockItem().getBytesInternal()[getOffset() + OFFSET_FLAG] = flag;
    }
    public void addFlagByte(byte flag){
        flag = (byte) ((getFlagByte() & 0xff) | (flag & 0xff));
        setFlagByte(flag);
    }
    public void addFlag(SpecBlock.Flag flag){
        addFlagByte(flag.getFlag());
    }
    public void setPublic(){
        addFlag(SpecBlock.Flag.SPEC_PUBLIC);
    }
    public boolean isPublic(){
        return SpecBlock.Flag.isPublic(getFlagByte());
    }
    public int getInteger(){
        return BlockItem.getInteger(this.getBlockItem().getBytesInternal(), this.getOffset());
    }
    public void setInteger(int value){
        if(value == getInteger()){
            return;
        }
        BlockItem.putInteger(this.getBlockItem().getBytesInternal(), this.getOffset(), value);
        this.getBlockItem().onBytesChanged();
    }
    @Override
    public String toString(){
        byte flag = getFlagByte();
        if(flag != 0){
            return SpecBlock.Flag.toString(getFlagByte());
        }
        int val = getInteger();
        if(val != 0){
            return HexUtil.toHex8(val);
        }
        return "";
    }

    private static final int OFFSET_FLAG = 3;

}
