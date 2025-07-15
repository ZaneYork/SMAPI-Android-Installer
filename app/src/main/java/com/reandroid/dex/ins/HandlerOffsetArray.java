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
package com.reandroid.dex.ins;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedList;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public class HandlerOffsetArray extends CountedList<HandlerOffset> {

    private int itemsStart;

    public HandlerOffsetArray(IntegerReference itemCount) {
        super(itemCount, CREATOR);
    }

    public HandlerOffset getOrCreate(int index) {
        ensureSize(index + 1);
        return get(index);
    }
    int getOffset(int i) {
        HandlerOffset offset = get(i);
        if(offset != null){
            return offset.getOffset();
        }
        return -1;
    }
    int indexOf(int value){
        if(value < 0){
            return -1;
        }
        int size = size();
        for(int i = 0; i < size; i++){
            if(value == getOffset(i)){
                return i;
            }
        }
        return -1;
    }

    int getItemsStart() {
        return itemsStart;
    }
    int getMinStart() {
        int result = 0;
        int size = size();
        for(int i = 0; i < size; i++){
            int offset = getOffset(i);
            if(i == 0 || offset < result){
                result = offset;
            }
        }
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        itemsStart = reader.getPosition();
    }

    public void merge(HandlerOffsetArray offsetArray){
        if(offsetArray == this){
            return;
        }
        int size = offsetArray.size();
        setSize(size);
        for(int i = 0; i < size; i++){
            HandlerOffset coming = offsetArray.get(i);
            HandlerOffset offset = this.get(i);
            offset.merge(coming);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = size();
        builder.append("size = ");
        builder.append(size);
        builder.append('[');
        builder.append(StringsUtil.join(iterator(), ", "));
        builder.append(']');
        return builder.toString();
    }

    private static final Creator<HandlerOffset> CREATOR = new Creator<HandlerOffset>() {
        @Override
        public HandlerOffset[] newArrayInstance(int length) {
            return new HandlerOffset[length];
        }
        @Override
        public HandlerOffset newInstance() {
            return new HandlerOffset();
        }
    };
}
