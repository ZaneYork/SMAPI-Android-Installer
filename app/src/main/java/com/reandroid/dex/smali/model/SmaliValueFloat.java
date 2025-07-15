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

public class SmaliValueFloat extends SmaliValueNumber<Float>{

    private float value;
    public SmaliValueFloat(){
        this(0.0f);
    }
    public SmaliValueFloat(float f){
        super();
        this.value = f;
    }

    public float getValue() {
        return value;
    }
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public Float getNumber() {
        return getValue();
    }
    @Override
    public void setNumber(Float number) {
        setValue(number);
    }
    @Override
    public int getWidth() {
        return 4;
    }
    @Override
    public int unsignedInt() {
        return getNumber().intValue();
    }
    @Override
    public long unsignedLong() {
        return getNumber().intValue() & 0xffffffffL;
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(getValue());
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.FLOAT;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        int position = reader.position();
        float f;
        try{
            if(reader.startsWith(POSITIVE_INFINITY)){
                reader.skip(POSITIVE_INFINITY.length);
                f = Float.POSITIVE_INFINITY;
            }else if(reader.startsWith(NEGATIVE_INFINITY)){
                reader.skip(NEGATIVE_INFINITY.length);
                f = Float.NEGATIVE_INFINITY;
            }else if(reader.startsWith(NAN)){
                reader.skip(NAN.length);
                f = Float.NaN;
            }else {
                f = Float.parseFloat(reader.readStringForNumber());
            }
        }catch (NumberFormatException ex){
            reader.position(position);
            throw new SmaliParseException(ex.getMessage(), reader);
        }
        setValue(f);
    }

    public static final byte[] POSITIVE_INFINITY = new byte[]{'I', 'n', 'f', 'i', 'n', 'i', 't', 'y', 'f'};
    public static final byte[] NEGATIVE_INFINITY = new byte[]{'-', 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y', 'f'};
    public static final byte[] NAN = new byte[]{'N', 'a', 'N', 'f'};
}
