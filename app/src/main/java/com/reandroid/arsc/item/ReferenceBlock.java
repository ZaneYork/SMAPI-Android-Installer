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

public class ReferenceBlock<T extends Block> implements ReferenceItem{
    private final T block;
    private final int offset;
    public ReferenceBlock(T block, int offset){
        this.block = block;
        this.offset = offset;
    }
    public T getBlock(){
        return this.block;
    }
    @Override
    public void set(int val) {
        BlockItem.putInteger(this.block.getBytes(), this.offset, val);
    }
    @Override
    public int get() {
        return BlockItem.getInteger(this.block.getBytes(), this.offset);
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass){
        T block = getBlock();
        if(parentClass.isInstance(block)){
            return (T1) block;
        }
        return getBlock().getParentInstance(parentClass);
    }
    @Override
    public String toString(){
        return get()+":"+this.block;
    }
}
