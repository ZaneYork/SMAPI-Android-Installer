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

import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.StringsUtil;

public class CoderHex extends Coder {
    @Override
    public EncodeResult encode(String text){
        int length = text.length();
        if(length < 3 || length > 10){
            return null;
        }
        char x = text.charAt(1);
        if(x != 'x'){
            if(x != 'X'){
                return null;
            }
            text = StringsUtil.toLowercase(text);
        }
        Integer value = parseHex(text);
        if(value == null){
            return null;
        }
        return new EncodeResult(ValueType.HEX, value);
    }

    @Override
    public String decode(int data) {
        return HexUtil.toHex8(data);
    }
    @Override
    public ValueType getValueType() {
        return ValueType.HEX;
    }
    @Override
    boolean canStartWith(char first) {
        return first == '0';
    }

    public static final CoderHex INS = new CoderHex();
}
