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

import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class SmaliValueKey extends SmaliValue{

    private Key value;

    public SmaliValueKey(){
        super();
    }

    public Key getValue() {
        return value;
    }
    public void setValue(Key value) {
        this.value = value;
    }

    @Override
    public Key getKey() {
        return getValue();
    }
    @Override
    public DexValueType<?> getValueType() {
        Key key = getValue();
        if(key instanceof StringKey){
            return DexValueType.STRING;
        }
        if(key instanceof TypeKey){
            return DexValueType.TYPE;
        }
        if(key instanceof MethodKey){
            return DexValueType.METHOD;
        }
        if(key instanceof FieldKey){
            return DexValueType.FIELD;
        }
        if(key instanceof MethodHandleKey){
            return DexValueType.METHOD_HANDLE;
        }
        // TODO: throw ?
        return null;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendOptional(getValue());
    }
    @Override
    public void parse(SmaliReader reader) throws IOException {
        if(reader.get() == '"'){
            setValue(StringKey.read(reader));
            return;
        }
        int i = reader.indexOfBeforeLineEnd(':');
        if(i >= 0){
            setValue(FieldKey.read(reader));
            return;
        }
        i = reader.indexOfBeforeLineEnd('(');
        if(i >= 0){
            setValue(MethodKey.read(reader));
            return;
        }
        setValue(TypeKey.read(reader));
    }
}
