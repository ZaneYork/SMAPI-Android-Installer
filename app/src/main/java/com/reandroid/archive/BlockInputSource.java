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
package com.reandroid.archive;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.utils.CRCDigest;

import java.io.IOException;
import java.io.OutputStream;

public class BlockInputSource<T extends Block> extends ByteInputSource {

    private final T mBlock;

    public BlockInputSource(String name, T block) {
        super(new byte[0], name);
        this.mBlock=block;
    }
    public BlockInputSource(InputSource inputSource, T block) {
        this(inputSource.getAlias(), block);
        setMethod(inputSource.getMethod());
        setSort(inputSource.getSort());
    }

    public T getBlock() {
        T block = this.mBlock;
        if(block instanceof BlockRefresh){
            ((BlockRefresh) block).refresh();
        }
        return block;
    }
    @Override
    public long getLength() throws IOException{
        Block block = getBlock();
        return block.countBytes();
    }
    @Override
    public long getCrc() throws IOException{
        Block block = getBlock();
        CRCDigest outputStream = new CRCDigest();
        block.writeBytes(outputStream);
        return outputStream.getValue();
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getBlock().writeBytes(outputStream);
    }
    @Override
    public byte[] getBytes() {
        return getBlock().getBytes();
    }
}
