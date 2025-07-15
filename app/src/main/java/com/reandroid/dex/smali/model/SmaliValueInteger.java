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
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SmaliValueInteger extends SmaliValueNumber<Integer>{

    private int value;

    public SmaliValueInteger(){
        this(0);
    }
    public SmaliValueInteger(int i){
        super();
        this.value = i;
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public Integer getNumber() {
        return getValue();
    }
    @Override
    public void setNumber(Integer number) {
        setValue(number);
    }
    @Override
    public int getWidth() {
        return 4;
    }
    @Override
    public int unsignedInt() {
        return getValue();
    }
    @Override
    public long unsignedLong() {
        return getValue() & 0xffffffffL;
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(getValue());
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.INT;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendHex(getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        int i;
        try{
            i = HexUtil.parseHexInteger(reader.readStringForNumber());
        }catch (NumberFormatException ex){
            reader.position(position);
            throw new SmaliParseException("Invalid hex integer", reader);
        }
        setValue(i);
    }
}
