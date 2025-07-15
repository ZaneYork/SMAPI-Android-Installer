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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Arrays;

public class AlignItem extends BlockItem{
    private byte fill;
    private int alignment;
    public AlignItem() {
        super(0);
        this.alignment = ALIGNMENT;
    }
    public void align(Block block){
        clear();
        if(getAlignment() <= 0){
            return;
        }
        align(block.countBytes());
    }
    public void align(int count){
        int alignment = getAlignment();
        if(alignment <= 0){
            return;
        }
        int size = (alignment - (count % alignment)) % alignment;
        setSize(size);
    }
    public void alignSafe(BlockReader reader) throws IOException {
        align(reader.getPosition());
        int size = size();
        int available = reader.available();
        if(size != 0 && available >= size) {
            super.onReadBytes(reader);
        }
    }
    public void clear(){
        setBytesLength(0, false);
    }
    public int size(){
        return countBytes();
    }
    public void ensureSize(int size){
        if(size > size()){
            setSize(size);
        }
    }
    public void setSize(int size){
        setBytesLength(size, false);
        setFill(this.fill);
    }
    public int getAlignment() {
        return alignment;
    }
    public void setAlignment(int alignment) {
        this.alignment = alignment;
        if(alignment <= 0){
            setBytesLength(0, false);
        }
    }
    public void setFill(byte fill) {
        this.fill = fill;
        byte[] bytes = getBytesInternal();
        Arrays.fill(bytes, fill);
    }

    @Override
    public String toString() {
        int alignment = getAlignment();
        if(alignment <= 0){
            return "OFF";
        }
        int size = size();
        byte fill = this.fill;
        StringBuilder builder = new StringBuilder();
        if(alignment != ALIGNMENT){
            builder.append("alignment=");
            builder.append(alignment);
            builder.append(", ");
        }
        if(fill != 0){
            builder.append("fill=");
            builder.append(HexUtil.toHex2(fill));
            builder.append(", ");
        }
        builder.append("align=");
        builder.append(size);
        return builder.toString();
    }

    private static final int ALIGNMENT = 4;
}
