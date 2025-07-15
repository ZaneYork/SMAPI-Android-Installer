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
package com.reandroid.dex.common;

public class OperandType {

    public static final OperandType NONE;
    public static final OperandType HEX;
    public static final OperandType DECIMAL;
    public static final OperandType LABEL;
    public static final OperandType KEY;

    static {
        NONE = new OperandType("NONE");
        HEX = new OperandType("HEX");
        DECIMAL = new OperandType("DECIMAL");
        LABEL = new OperandType("LABEL");
        KEY = new OperandType("KEY");
    }

    private final String name;

    private OperandType(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    @Override
    public String toString() {
        return name;
    }
}
