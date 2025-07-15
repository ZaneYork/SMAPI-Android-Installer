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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.StringsUtil;

import java.nio.charset.StandardCharsets;

public class FixedLengthString  extends StringItem {
    private final int bytesLength;
    public FixedLengthString(int bytesLength){
        super(true);
        this.bytesLength = bytesLength;
        setBytesLength(bytesLength);
    }
    @Override
    protected byte[] encodeString(String text){
        if(text == null){
            return new byte[bytesLength];
        }
        byte[] bytes = getUtf16Bytes(text);
        byte[] results = new byte[this.bytesLength];
        int length = bytes.length;
        if(length > this.bytesLength){
            length = this.bytesLength;
        }
        System.arraycopy(bytes, 0, results, 0, length);
        return results;
    }
    @Override
    protected String decodeString(byte[] bytes){
        return decodeUtf16Bytes(bytes);
    }
    @Override
    public StyleItem getOrCreateStyle(){
        return null;
    }
    @Override
    int calculateReadLength(BlockReader reader){
        return bytesLength;
    }

    @Override
    protected void onStringChanged(String old, String text) {
    }

    @Override
    public int compareTo(StringItem stringItem){
        if(stringItem == null){
            return -1;
        }
        return StringsUtil.compareStrings(get(), stringItem.get());
    }
    @Override
    public String toString(){
        return "FIXED-" + bytesLength + " {" + get() + "}";
    }
    private static String decodeUtf16Bytes(byte[] bytes){
        if(isNullBytes(bytes)){
            return null;
        }
        int length = getEndNullPosition(bytes);
        return new String(bytes,0, length, StandardCharsets.UTF_16LE);
    }
    private static int getEndNullPosition(byte[] bytes){
        int length = bytes.length;
        int result = 0;
        boolean found = false;
        for(int i = 1; i < length; i++){
            byte b0 = bytes[i - 1];
            byte b1 = bytes[i];
            if(b0 == 0 && b1 == 0){
                if(!found){
                    result = i;
                    found = true;
                }else if(result<i-1){
                    return result;
                }
            }else {
                found = false;
            }
        }
        if(!found){
            return length;
        }
        return result;
    }
}
