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
import com.reandroid.dex.smali.model.SmaliValueFloat;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class FloatValue extends PrimitiveValueBlock {

    public FloatValue() {
        super(DexValueType.FLOAT);
    }

    @Override
    public Float getData() {
        return get();
    }
    @Override
    public void setData(Number number) {
        this.set((Float) number);
    }
    public float get(){
        return Float.intBitsToFloat(getFloatBits());
    }
    public void set(float value){
        setFloatBits(Float.floatToIntBits(value));
    }
    private int getFloatBits() {
        int shift = (3 - getValueSize()) * 8;
        return  (int) (getUnsigned() << shift);
    }
    private void setFloatBits(int bits) {
        int i = 0;
        while (i < 3 && (bits & 0xff) == 0) {
            bits = bits >>> 8;
            i ++;
        }
        setValue(bits & 0xffffffffL, 4 - i);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.FLOAT;
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(get());
    }
    @Override
    public String getHex() {
        return HexUtil.toHex(getUnsigned(), getValueSize()) + "L";
    }

    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.TYPE_F;
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueFloat smaliValueFloat = (SmaliValueFloat) smaliValue;
        set(smaliValueFloat.getValue());
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
