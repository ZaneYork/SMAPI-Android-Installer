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

public abstract class ComplexUnit {
    private final int flag;
    private final String symbol;
    ComplexUnit(int flag, String symbol) {
        this.flag = flag;
        this.symbol = symbol;
    }

    public boolean isFraction(){
        return false;
    }
    public int getFlag() {
        return flag;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean hasPostfix(String text) {
        return text.endsWith(symbol);
    }

    public abstract ValueType getValueType();

    @Override
    public int hashCode(){
        return symbol.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        return obj == this;
    }
    @Override
    public String toString() {
        return getSymbol();
    }
}
