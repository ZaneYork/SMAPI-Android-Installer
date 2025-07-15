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
package com.reandroid.arsc.container;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockContainer;

public  class ExpandableBlockContainer extends BlockContainer<Block> {
    private Block[] mChildes;
    private int mCursor;

    public ExpandableBlockContainer(int initialSize){
        super();
        this.mChildes=new Block[initialSize];
    }

    public final void addChild(Block block){
        if(block == null){
            return;
        }
        int index = mCursor;
        ensureCount(index + 1);
        mChildes[index] = block;
        block.setIndex(index);
        block.setParent(this);
        mCursor++;
    }
    private void ensureCount(int count){
        if(count<= getChildesCount()){
            return;
        }
        Block[] old = mChildes;
        mChildes = new Block[count];
        for(int i = 0; i < old.length; i++){
            mChildes[i] = old[i];
        }
    }
    @Override
    protected void onRefreshed() {

    }

    @Override
    public final int getChildesCount() {
        return mChildes.length;
    }
    @Override
    public final Block[] getChildes() {
        return mChildes;
    }
}
