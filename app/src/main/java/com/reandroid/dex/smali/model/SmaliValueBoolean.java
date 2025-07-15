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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class SmaliValueBoolean extends SmaliValue{

    private boolean value;

    public SmaliValueBoolean(){
        super();
    }

    public boolean getValue(){
        return value;
    }
    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(getValue());
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.BOOLEAN;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(getValue()){
            writer.append("true");
        }else {
            writer.append("false");
        }
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        byte[] bytes = new byte[]{'f', 'a', 'l', 's', 'e'};
        if(reader.startsWith(bytes)){
            reader.skip(bytes.length);
            setValue(false);
        }else {
            bytes = new byte[]{'t', 'r', 'u', 'e'};
            if(reader.startsWith(bytes)){
                reader.skip(bytes.length);
                setValue(true);
            }else {
                throw new SmaliParseException("Not boolean value", reader);
            }
        }
    }
}
