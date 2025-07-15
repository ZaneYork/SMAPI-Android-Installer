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
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.utils.HexUtil;

public class IntegerItem extends BlockItem implements ReferenceItem, DirectStreamReader {

    private final boolean bigEndian;
    private int mCache;

    public IntegerItem(boolean bigEndian){
        super(4);
        this.bigEndian = bigEndian;
    }
    public IntegerItem(){
        this(false);
    }
    public IntegerItem(int value){
        this(false);
        set(value);
    }

    @Override
    public void set(int value) {
        if(value == mCache){
            return;
        }

        mCache = value;
        byte[] bytes = getBytesInternal();
        if (bigEndian) {
            putBigEndianInteger(bytes, 0, value);
        } else {
            putInteger(bytes, 0, value);
        }
    }
    @Override
    public int get(){
        return mCache;
    }
    @Override
    public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass){
        return getParentInstance(parentClass);
    }
    public long unsignedLong(){
        return get() & 0x00000000ffffffffL;
    }
    public String toHex(){
        return HexUtil.toHex8(get());
    }
    @Override
    protected void onBytesChanged() {
        int i;
        byte[] bytes = getBytesInternal();
        if (bigEndian) {
            i = getBigEndianInteger(bytes, 0);
        } else {
            i = getInteger(bytes, 0);
        }
        mCache = i;
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }

    public static final Creator<IntegerItem> CREATOR = new Creator<IntegerItem>() {
        @Override
        public IntegerItem[] newArrayInstance(int length) {
            return new IntegerItem[length];
        }
        @Override
        public IntegerItem newInstance() {
            return new IntegerItem(false);
        }
    };

    public static final Creator<IntegerItem> CREATOR_BIG_ENDIAN = new Creator<IntegerItem>() {
        @Override
        public IntegerItem[] newArrayInstance(int length) {
            return new IntegerItem[length];
        }
        @Override
        public IntegerItem newInstance() {
            return new IntegerItem(true);
        }
    };
}
