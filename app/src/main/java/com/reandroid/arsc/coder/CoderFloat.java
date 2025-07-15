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

public class CoderFloat extends Coder {
    @Override
    public EncodeResult encode(String text){
        int i = text.indexOf('.');
        if(i <= 0){
            return null;
        }
        Float value = parseFloat(text);
        if(value == null){
            return null;
        }
        return new EncodeResult(ValueType.FLOAT, Float.floatToIntBits(value));
    }
    @Override
    public String decode(int data) {
        return Float.toString(Float.intBitsToFloat(data));
    }
    @Override
    public ValueType getValueType() {
        return ValueType.FLOAT;
    }
    @Override
    boolean canStartWith(char first) {
        return isNumberStart(first);
    }

    public static final CoderFloat INS = new CoderFloat();
}
