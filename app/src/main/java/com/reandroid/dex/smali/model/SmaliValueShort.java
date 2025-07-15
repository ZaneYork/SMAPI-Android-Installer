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

public class SmaliValueShort extends SmaliValueNumber<Short>{

    private short value;

    public SmaliValueShort(){
        this((short) 0);
    }
    public SmaliValueShort(short sh){
        super();
        this.value = sh;
    }

    public short getValue() {
        return value;
    }
    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public Short getNumber() {
        return getValue();
    }
    @Override
    public void setNumber(Short number) {
        setValue(number);
    }
    @Override
    public int getWidth() {
        return 2;
    }
    @Override
    public int unsignedInt() {
        return getValue() & 0xffff;
    }
    @Override
    public long unsignedLong() {
        return getValue() & 0xffffL;
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(getValue());
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.SHORT;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendHex(getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        short s;
        try{
            s = HexUtil.parseHexShort(reader.readStringForNumber());
        }catch (NumberFormatException ex){
            reader.position(position);
            throw new SmaliParseException(ex.getMessage(), reader);
        }
        setValue(s);
    }
}
