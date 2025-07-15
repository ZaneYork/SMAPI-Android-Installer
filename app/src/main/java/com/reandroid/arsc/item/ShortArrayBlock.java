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

import com.reandroid.common.IntegerArray;

public class ShortArrayBlock extends BlockItem implements IntegerArray {
    public ShortArrayBlock() {
        super(0);
    }
    public boolean contains(int value){
        int size = size();
        for(int i = 0; i < size; i++){
            if(value == get(i)){
                return true;
            }
        }
        return false;
    }
    public void clear(){
        setSize(0);
    }
    public void add(int value){
        int current = size();
        setSize(current + 1);
        put(current, value);
    }
    public void add(IntegerArray integerArray){
        if(IntegerArray.isEmpty(integerArray)){
            return;
        }
        int old = size();
        int size = old + integerArray.size();
        setSize(size);
        for(int i = 0;  i < size; i++){
            put(old + i, integerArray.get(i));
        }
    }
    public void add(int[] values){
        if(IntegerArray.isEmpty(values)){
            return;
        }
        int old = size();
        int size = old + values.length;
        setSize(size);
        for(int i = 0;  i < size; i++){
            put(old + i, values[i]);
        }
    }
    public void set(int[] values){
        if(IntegerArray.isEmpty(values)){
            setSize(0);
            return;
        }
        int size = values.length;
        setSize(size);
        for(int i =0; i < size; i++){
            put(i, values[i]);
        }
    }
    public int[] toArray(){
        return IntegerArray.toArray(this);
    }
    public void fill(int value){
        int size = size();
        for(int i = 0; i < size; i++){
            put(i, value);
        }
    }
    public void ensureArraySize(int size){
        int current = size();
        if(current >= size){
            return;
        }
        setSize(size);
    }
    public void setSize(int size){
        if(size < 0){
            size = 0;
        }
        setBytesLength(size * 2);
    }
    @Override
    public int get(int index){
        if(index < 0 || index >= size()){
            return 0;
        }
        return getShortUnsigned(getBytesInternal(), index * 2);
    }
    @Override
    public int size(){
        return getBytesLength() / 2;
    }
    public void put(int index, int value){
        putShort(getBytesInternal(), index * 2, value);
    }
    @Override
    public String toString(){
        return IntegerArray.toString(this);
    }
}
