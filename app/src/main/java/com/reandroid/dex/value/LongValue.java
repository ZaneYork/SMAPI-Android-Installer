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
import com.reandroid.dex.smali.model.SmaliValueLong;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class LongValue extends PrimitiveValueBlock {

    public LongValue() {
        super(DexValueType.LONG);
    }

    @Override
    public Long getData() {
        return get();
    }
    @Override
    public void setData(Number number) {
        this.set((Long) number);
    }
    public long get() {
        return getSignedValue();
    }
    public void set(long value){
        setNumberValue(value);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.LONG;
    }
    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(get());
    }
    @Override
    public String getHex() {
        return HexUtil.toSignedHex(get()) + "L";
    }

    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.TYPE_J;
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueLong smaliValueLong = (SmaliValueLong) smaliValue;
        set(smaliValueLong.getValue());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        super.append(writer);
        writer.appendComment(Long.toString(get()));
    }
    @Override
    public String toString() {
        return getHex() + " # " + get();
    }
}
