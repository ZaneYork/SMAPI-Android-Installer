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
package com.reandroid.dex.value;

import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueDouble;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class DoubleValue extends PrimitiveValueBlock {

    public DoubleValue() {
        super(DexValueType.DOUBLE);
    }

    @Override
    public Double getData() {
        return get();
    }
    @Override
    public void setData(Number number) {
        this.set((Double) number);
    }
    public double get(){
        return Double.longBitsToDouble(getLongBits());
    }
    public void set(double value){
        setLongBits(Double.doubleToLongBits(value));
    }
    private long getLongBits() {
        int shift = (7 - getValueSize()) * 8;
        return getUnsigned() << shift;
    }
    private void setLongBits(long bits) {
        int i = 0;
        while (i < 7 && (bits & 0xff) == 0) {
            bits = bits >>> 8;
            i ++;
        }
        setValue(bits, 8 - i);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.DOUBLE;
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(get());
    }
    @Override
    public String getHex() {
        int shift = (7 - getValueSize()) * 8;
        return HexUtil.toHex(getUnsigned() << shift, 8) + "L";
    }

    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.TYPE_D;
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueDouble smaliValueDouble = (SmaliValueDouble) smaliValue;
        set(smaliValueDouble.getValue());
    }


    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(get());
    }
    @Override
    public String toString() {
        return getHex() + " # " + get();
    }
}
