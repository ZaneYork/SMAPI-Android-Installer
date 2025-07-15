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

import java.util.Objects;

public class EncodeResult {
    public final ValueType valueType;
    public final int value;
    private final String error;

    public EncodeResult(ValueType valueType, int value, String error) {
        this.valueType = valueType;
        this.value = value;
        this.error = error;
    }
    public EncodeResult(ValueType valueType, int value) {
        this(valueType, value, null);
    }
    public EncodeResult(String error) {
        this(ValueType.NULL, -1, error);
    }
    public String getError() {
        return error;
    }
    public boolean isError(){
        return getError() != null;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EncodeResult other = (EncodeResult) obj;
        String error = getError();
        if(error != null){
            return error.equals(other.getError());
        }
        if(other.getError() != null){
            return false;
        }
        return value == other.value && valueType == other.valueType;
    }
    @Override
    public int hashCode() {
        return Objects.hash(valueType, value, getError());
    }
    @Override
    public String toString() {
        String error = getError();
        if(error != null){
            return error;
        }
        return valueType + ": " + HexUtil.toHex8(value);
    }

    public static final EncodeResult RESOURCE_NOT_FOUND = new EncodeResult("RESOURCE NOT FOUND");
}
