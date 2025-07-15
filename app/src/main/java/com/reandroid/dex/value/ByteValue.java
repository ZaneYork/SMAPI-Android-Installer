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
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueByte;
import com.reandroid.utils.HexUtil;

public class ByteValue extends PrimitiveValueBlock {

    public ByteValue() {
        super(DexValueType.BYTE);
    }

    @Override
    public Byte getData() {
        return get();
    }
    @Override
    public void setData(Number number) {
        this.set((Byte) number);
    }

    public byte get(){
        return (byte) getSignedValue();
    }
    public void set(byte b){
        setNumberValue(b);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.BYTE;
    }

    @Override
    public PrimitiveKey getKey() {
        return PrimitiveKey.of(get());
    }
    @Override
    public String getHex() {
        return HexUtil.toSignedHex(get()) + "t";
    }

    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.TYPE_B;
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueByte smaliValueByte = (SmaliValueByte) smaliValue;
        set(smaliValueByte.getValue());
    }
}
