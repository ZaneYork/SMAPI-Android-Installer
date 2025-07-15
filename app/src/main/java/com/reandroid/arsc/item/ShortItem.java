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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.utils.HexUtil;

public class ShortItem extends BlockItem implements IntegerReference, DirectStreamReader {

    private final boolean bigEndian;
    private int mCache;

    public ShortItem(boolean bigEndian) {
        super(2);
        this.bigEndian = bigEndian;
    }
    public ShortItem() {
        this(false);
    }
    public ShortItem(short value){
        this(false);
        set(value);
    }

    @Override
    public void set(int value){
        if(value == mCache){
            return;
        }
        mCache = value;
        byte[] bytes = getBytesInternal();
        if (bigEndian) {
            putBigEndianShort(bytes, 0, value);
        } else {
            putShort(bytes, 0, value);
        }
    }
    @Override
    public int get(){
        return mCache;
    }
    public void set(short value){
        set(0xffff & value);
    }
    public int unsignedInt(){
        return get();
    }
    public short getShort(){
        return (short) mCache;
    }
    public String toHex(){
        return HexUtil.toHex4(getShort());
    }
    @Override
    protected void onBytesChanged() {
        int s;
        byte[] bytes = getBytesInternal();
        if (bigEndian) {
            s = getBigEndianShort(bytes, 0);
        } else {
            s = getShort(bytes, 0);
        }
        mCache = s;
    }

    @Override
    public String toString(){
        return String.valueOf(get());
    }

    public static final Creator<ShortItem> CREATOR = new Creator<ShortItem>() {
        @Override
        public ShortItem[] newArrayInstance(int length) {
            return new ShortItem[length];
        }
        @Override
        public ShortItem newInstance() {
            return new ShortItem(false);
        }
    };

    public static final Creator<ShortItem> CREATOR_BIG_ENDIAN = new Creator<ShortItem>() {
        @Override
        public ShortItem[] newArrayInstance(int length) {
            return new ShortItem[length];
        }
        @Override
        public ShortItem newInstance() {
            return new ShortItem(true);
        }
    };
}
