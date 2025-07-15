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

public class LongItem extends BlockItem implements LongReference, DirectStreamReader {

    private final boolean bigEndian;
    private long mCache;

    public LongItem(boolean bigEndian) {
        super(8);
        this.bigEndian = bigEndian;
    }
    public LongItem() {
        this(false);
    }

    @Override
    public void set(long value){
        if(value == mCache){
            return;
        }
        mCache = value;
        byte[] bytes = getBytesInternal();
        if (bigEndian) {
            putBigEndianLong(bytes, 0, value);
        } else {
            putLong(bytes, 0, value);
        }
    }
    @Override
    public long getLong(){
        return mCache;
    }
    @Override
    public int get() {
        return 0;
    }
    @Override
    public void set(int value) {
        set(value & 0xffffffffL);
    }
    public String toHex() {
        return HexUtil.toHex(getLong(), 16);
    }

    @Override
    protected void onBytesChanged() {
        long l;
        byte[] bytes = getBytesInternal();
        if (bigEndian) {
            l = getBigEndianLong(bytes, 0);
        } else {
            l = getLong(bytes, 0);
        }
        mCache = l;
    }

    @Override
    public String toString(){
        return String.valueOf(getLong());
    }

    public static final Creator<LongItem> CREATOR = new Creator<LongItem>() {
        @Override
        public LongItem[] newArrayInstance(int length) {
            return new LongItem[length];
        }
        @Override
        public LongItem newInstance() {
            return new LongItem(false);
        }
    };

    public static final Creator<LongItem> CREATOR_BIG_ENDIAN = new Creator<LongItem>() {
        @Override
        public LongItem[] newArrayInstance(int length) {
            return new LongItem[length];
        }
        @Override
        public LongItem newInstance() {
            return new LongItem(true);
        }
    };
}
