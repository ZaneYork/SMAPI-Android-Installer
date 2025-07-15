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

public abstract class CoderComplexData extends Coder {
    @Override
    public EncodeResult encode(String text){
        ComplexUnit unit = parseUnit(text);
        if(unit == null){
            return null;
        }
        String number = text.substring(0, text.length() - unit.getSymbol().length());
        Float floatValue = parseFloat(number);
        if(floatValue == null){
            return null;
        }
        int value = ComplexUtil.encodeComplex(floatValue, unit);
        return new EncodeResult(unit.getValueType(), value);
    }
    @Override
    boolean canStartWith(char first) {
        return isNumberStart(first);
    }
    abstract ComplexUnit parseUnit(String text);
}
