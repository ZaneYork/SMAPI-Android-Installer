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

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.NullKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueNull;

import java.io.IOException;

public class NullValue extends DexValueBlock<Block> {

    public NullValue() {
        super(DexValueType.NULL);
    }

    @Override
    public DexValueType<NullValue> getValueType() {
        return DexValueType.NULL;
    }

    @Override
    public NullKey getKey() {
        return NullKey.INSTANCE;
    }

    @Override
    public void merge(DexValueBlock<?> valueBlock){
        super.merge(valueBlock);
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueNull smaliValueNull = (SmaliValueNull) smaliValue;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append("null");
    }
    @Override
    public String getAsString() {
        return "null";
    }

    @Override
    public int hashCode() {
        return getValueType().getType();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && getClass() == obj.getClass();
    }
    @Override
    public String toString() {
        return "NullValue";
    }

    public static final NullValue PLACE_HOLDER = new NullValue();
}
