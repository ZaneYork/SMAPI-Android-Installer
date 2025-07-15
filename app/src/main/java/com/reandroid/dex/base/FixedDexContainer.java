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
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public class FixedDexContainer extends FixedBlockContainer {
    public FixedDexContainer(int childesCount) {
        super(childesCount);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        Block[] childes = getChildes();
        if(childes == null){
            return;
        }
        int length = childes.length;
        if(skipReading(this, reader)){
            return;
        }
        for(int i = 0; i < length; i++){
            Block block = childes[i];
            if(block == null){
                continue;
            }
            if(skipReading(block, reader)){
                continue;
            }
            block.readBytes(reader);
        }
    }
    protected void nonCheckRead(BlockReader reader) throws IOException {
        Block[] childes = getChildes();
        if(childes == null){
            return;
        }
        int length = childes.length;
        for(int i = 0; i < length; i++){
            Block block = childes[i];
            if(block != null){
                block.readBytes(reader);
            }
        }
    }
    private boolean skipReading(Block block, BlockReader reader){
        if(!(block instanceof OffsetSupplier)){
            return false;
        }
        OffsetSupplier offsetSupplier = (OffsetSupplier) block;
        IntegerReference reference = offsetSupplier.getOffsetReference();
        if(reference != null){
            int offset = reference.get();
            if(!isValidOffset(offset)){
                return true;
            }
            reader.seek(offset);
        }
        return false;
    }
    protected boolean isValidOffset(int offset){
        return offset > 0;
    }
}
