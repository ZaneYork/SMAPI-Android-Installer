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

public class SmaliValueDouble extends SmaliValueNumber<Double>{

    private double value;

    public SmaliValueDouble(){
        this(0.0);
    }
    public SmaliValueDouble(double d){
        super();
        this.value = d;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public Double getNumber() {
        return getValue();
    }
    @Override
    public void setNumber(Double number) {
        setValue(number);
    }
    @Override
    public int getWidth() {
        return 8;
    }
    @Override
    public int unsignedInt() {
        return (int) getNumber().longValue();
    }
    @Override
    public long unsignedLong() {
        return getNumber().longValue();
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(getValue());
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.DOUBLE;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        double d;
        try{
            d = Double.parseDouble(reader.readStringForNumber());
        }catch (NumberFormatException ex){
            reader.position(position);
            throw new SmaliParseException(ex.getMessage(), reader);
        }
        setValue(d);
    }
}
