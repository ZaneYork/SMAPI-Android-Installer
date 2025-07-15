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

public abstract class Coder {
    public abstract EncodeResult encode(String text);
    public abstract String decode(int data);
    public abstract ValueType getValueType();
    abstract boolean canStartWith(char first);

    static Integer parseInteger(String text){
        try{
            return Integer.parseInt(text);
        }catch (NumberFormatException ignored){
            return null;
        }
    }
    static Integer parseHex(String text){
        try{
            return HexUtil.parseHex(text);
        }catch (NumberFormatException ignored){
            return null;
        }
    }
    static Float parseFloat(String text){
        try{
            return Float.parseFloat(text);
        }catch (NumberFormatException ignored){
            return null;
        }
    }
    static boolean isNumberStart(char ch){
        return ch == '-' || (ch <= '9' && ch >= '0');
    }
}
