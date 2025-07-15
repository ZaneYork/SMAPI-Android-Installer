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
package com.reandroid.arsc.array;

public class SparseOffsetsArray extends IntegerOffsetArray {
    public SparseOffsetsArray(){
        super();
    }
    public int getHighestId(){
        int result = NO_ENTRY;
        int size = size();
        for(int i=0; i<size;i++){
            int id = getIdx(i);
            if(id > result){
                result = id;
            }
        }
        if(result == NO_ENTRY){
            result = 0;
        }
        return result;
    }
    public int indexOf(int idx){
        int size = super.size();
        for(int i=0; i<size; i++){
            if(idx == getIdx(i)){
                return i;
            }
        }
        return NO_ENTRY;
    }
    public int getIdx(int i){
        int value = super.get(i);
        if(value != NO_ENTRY) {
            value = value & 0xffff;
        }
        return value;
    }
    public void setIdx(int index, int idx){
        int value;
        if(idx == NO_ENTRY){
            value = idx;
        }else {
            int offset = get(index) & 0xffff0000;
            idx = idx & 0xffff;
            value = offset | idx;
        }
        super.put(index, value);
    }
    @Override
    public int getOffset(int i){
        int value = super.get(i);
        if(value == NO_ENTRY){
            return value;
        }
        value = (value >>> 16) & 0xffff;
        return value * 4;
    }
    @Override
    public void setOffset(int index, int offset){
        int value;
        if(offset == NO_ENTRY){
            value = 0;
        }else {
            int idx  = get(index);
            idx = idx & 0xffff;
            offset = offset & 0xffff;
            offset = offset / 4;
            offset = offset << 16;
            value = offset | idx;
        }
        super.put(index, value);
    }
}
