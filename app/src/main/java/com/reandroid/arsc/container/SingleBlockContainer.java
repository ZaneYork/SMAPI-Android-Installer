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
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public class SingleBlockContainer<T extends Block> extends BlockContainer<T> {
    private T mItem;
    public SingleBlockContainer(){
        super();
    }
    @Override
    protected void refreshChildes(){
        T item = this.mItem;
        if(item instanceof BlockRefresh){
            ((BlockRefresh)item).refresh();
        }
    }
    @Override
    protected void onRefreshed() {
    }
    public T getItem() {
        return mItem;
    }
    public void setItem(T item) {
        if(item == null){
            T oldItem = this.mItem;
            if(oldItem != null){
                oldItem.setIndex(-1);
                oldItem.setParent(null);
            }
            this.mItem = null;
            return;
        }
        this.mItem = item;
        item.setIndex(getIndex());
        item.setParent(this);
    }
    public boolean hasItem(){
        return this.mItem != null;
    }
    @Override
    public byte[] getBytes() {
        if(mItem != null){
            return mItem.getBytes();
        }
        return null;
    }
    @Override
    public int countBytes() {
        if(mItem != null){
            return mItem.countBytes();
        }
        return 0;
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END == this){
            counter.FOUND = true;
            return;
        }
        if(mItem != null){
            mItem.onCountUpTo(counter);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        if(mItem != null){
            mItem.readBytes(reader);
        }
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        if(mItem != null){
            return mItem.writeBytes(stream);
        }
        return 0;
    }

    @Override
    public int getChildesCount() {
        return this.mItem == null ? 0 : 1;
    }

    @Override
    public T[] getChildes() {
        return null;
    }

    @Override
    public String toString(){
        if(mItem != null){
            return mItem.toString();
        }
        return getClass().getSimpleName() + ": EMPTY";
    }
}
