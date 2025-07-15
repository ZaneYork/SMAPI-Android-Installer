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

public class IndirectBoolean implements BooleanReference{

    private final BlockItem blockItem;
    private final int byteOffset;
    private final int bitIndex;

    public IndirectBoolean(BlockItem blockItem, int byteOffset, int bitIndex) {
        this.blockItem = blockItem;
        this.byteOffset = byteOffset;
        this.bitIndex = bitIndex;
    }

    @Override
    public boolean get() {
        return BlockItem.getBit(blockItem.getBytesInternal(), byteOffset, bitIndex);
    }
    @Override
    public void set(boolean value) {
        BlockItem.putBit(blockItem.getBytesInternal(), byteOffset, bitIndex, value);
    }
    @Override
    public String toString() {
        return Boolean.toString(get());
    }
}
