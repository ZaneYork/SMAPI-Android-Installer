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
package com.reandroid.arsc.coder;

import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.HexUtil;

/**
 * Handles when the string reference is not available on string pool
 * */
public class CoderUnknownStringRef extends Coder {

    @Override
    public EncodeResult encode(String text) {
        if(text == null || text.length() != LENGTH){
            return null;
        }
        String prefix = PREFIX;
        if(!text.startsWith(prefix)){
            return null;
        }
        Integer value = parseHex(text.substring(prefix.length() + 1));
        if(value != null){
            return new EncodeResult(ValueType.STRING, value);
        }
        return null;
    }
    @Override
    public String decode(int data) {
        return HexUtil.toHex8(PREFIX, data);
    }
    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }
    @Override
    boolean canStartWith(char first) {
        return first == PREFIX_CHAR;
    }
    public static final CoderUnknownStringRef INS = new CoderUnknownStringRef();

    private static final char PREFIX_CHAR = 's';
    private static final String PREFIX = "string-reference@0x";
    private static final int LENGTH = PREFIX.length() + 8;
}
