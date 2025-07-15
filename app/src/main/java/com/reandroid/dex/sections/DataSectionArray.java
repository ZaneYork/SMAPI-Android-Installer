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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.DataItem;

import java.io.IOException;

public class DataSectionArray<T extends DataItem> extends SectionArray<T> {

    private int mStart;
    private int mEnd;

    public DataSectionArray(IntegerPair countAndOffset, Creator<T> creator) {
        super(countAndOffset, creator);
    }

    @Override
    public void onChanged() {
        super.onChanged();
        mStart = 0;
    }

    public T getAt(int offset){
        if(offset <= 0){
            return null;
        }
        int start = this.mStart;
        if(start == 0 || offset < start || offset > mEnd){
            updateBounds();
        }
        return getAt(offset, estimateBeginPosition(offset));
    }
    public T getAt(int offset, int indexHint){
        if(offset <= 0){
            return null;
        }
        T item = spiderSearch(offset, indexHint);
        if(item != null){
            return item;
        }
        //should not reach here
        item = lazySearch(offset);
        if(item != null){
            updateBounds();
        }
        return item;
    }
    public T[] getAt(int[] offsets){
        if(offsets == null || offsets.length == 0){
            return null;
        }
        int length = offsets.length;
        T[] results = this.newArrayInstance(offsets.length);
        for(int i = 0; i < length; i++){
            results[i] = getAt(offsets[i]);
        }
        return results;
    }
    @Override
    public void readChild(BlockReader reader, T item) throws IOException {
        int offset = reader.getPosition();
        item.setPosition(offset);
        item.onReadBytes(reader);
    }

    @Override
    public void onPreRemove(T item) {
        super.onPreRemove(item);
        IntegerReference reference = item.getOffsetReference();
        if(reference != null){
            reference.set(0);
        }
    }

    private T lazySearch(int offset){
        int size = size();
        for(int i = 0; i < size; i++){
            T item = get(i);
            if(offset == item.getOffset()){
                return item;
            }
        }
        return null;
    }
    private T spiderSearch(int offset, int beginIndex){
        int size = size();
        int i = beginIndex;
        boolean lookBack = false;
        boolean lookFront = false;
        boolean lookChanged = false;
        int step = 1;
        int acceleration = 1;
        while (true){
            if(i < 0 || i >= size){
                return null;
            }
            T item = get(i);
            int itemOffset = item.getOffset();
            if(itemOffset == offset){
                return item;
            }
            if(offset < itemOffset){
                if(lookFront){
                    if(lookChanged){
                        return null;
                    }
                    lookChanged = true;
                    lookFront = false;
                    step = 1;
                }else if(lookBack && i == 0){
                    return null;
                }
                if(!lookChanged){
                    int remain = i/3 + 1;
                    if(step > remain){
                        step = remain;
                    }
                }
                lookBack = true;
                i = i - step;
                if(i < 0){
                    i = 0;
                }
            }else {
                if(lookBack){
                    if(lookChanged){
                        return null;
                    }
                    lookBack = false;
                    lookChanged = true;
                    step = 1;
                }
                if(!lookChanged){
                    int remain = (size - i)/3 + 1;
                    if(step > remain){
                        step = remain;
                    }
                }
                lookFront = true;
                i = i + step;
            }
            if(!lookChanged){
                step += acceleration + 1;
                if(step % 2 == 1){
                    acceleration ++;
                }
            }
        }
    }
    private int estimateBeginPosition(int offset){
        int bytes = mEnd - mStart;
        if(bytes == 0){
            bytes = 1;
        }
        int offsetDiff = offset - mStart;
        long result = (long) offsetDiff * size();
        result = result / bytes;
        int index = (int) result;
        index = index - 1;
        int size = size();
        if(index >= size){
            index = size - 1;
        }
        if(index < 0){
            index = 0;
        }
        return index;
    }
    private void updateBounds(){
        mStart = 0;
        T item = getFirst();
        if(item != null){
            mStart = item.getOffset();
        }
        mEnd = mStart;
        item = getLast();
        if(item != null){
            mEnd = item.getOffset();
        }
    }
}
