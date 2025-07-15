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

import java.util.AbstractList;
import java.util.List;

public class IntegerArrayBlock extends ShortArrayBlock{
    public IntegerArrayBlock() {
        super();
    }
    public final List<Integer> toList(){
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return IntegerArrayBlock.this.get(i);
            }
            @Override
            public int size() {
                return IntegerArrayBlock.this.size();
            }
        };
    }
    @Override
    public void setSize(int size){
        if(size < 0){
            size = 0;
        }
        int length = size * 4;
        if(length < 0){
            throw new IndexOutOfBoundsException("Huge integers size = " + size
                    + ", parent = " + getParent());
        }
        setBytesLength(length);
    }
    @Override
    public int get(int index){
        if(index < 0 || index >= size()){
            return 0;
        }
        return getInteger(getBytesInternal(), index * 4);
    }
    @Override
    public int size(){
        return getBytesLength() / 4;
    }
    @Override
    public void put(int index, int value){
        putInteger(getBytesInternal(), index * 4, value);
    }
}
