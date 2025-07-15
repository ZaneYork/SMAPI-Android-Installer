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
package com.reandroid.dex.header;

import com.reandroid.arsc.item.ByteArray;
import com.reandroid.utils.HexUtil;

class HeaderPiece extends ByteArray {
    HeaderPiece(){
        super();
    }
    HeaderPiece(int bytesLength){
        super(bytesLength);
    }
    @Override
    public String toString(){
        return printChars(getBytesInternal());
    }

    static String printHex(byte[] bytes){
        if(bytes == null){
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < bytes.length; i++){
            if(i != 0){
                builder.append(' ');
            }
            builder.append(HexUtil.toHex2(null, bytes[i]));
        }
        return builder.toString();
    }
    static String printChars(byte[] bytes){
        if(bytes == null){
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        for(byte b : bytes) {
            char ch = toChar(b);
            if (isPrintable(ch)) {
                builder.append(ch);
            } else {
                builder.append(HexUtil.toHex2(b));
            }
        }
        return builder.toString();
    }
    private static char toChar(byte b){
        return (char) (0xff & b);
    }
    private static boolean isPrintable(char ch){
        if(ch <= 'z' && ch >= 'a'){
            return true;
        }
        if(ch <= 'Z' && ch >= 'A'){
            return true;
        }
        return ch <= '9' && ch >= '0';
    }
}
