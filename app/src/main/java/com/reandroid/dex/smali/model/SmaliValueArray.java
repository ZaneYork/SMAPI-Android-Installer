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

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;
import java.util.Iterator;

public class SmaliValueArray extends SmaliValue implements Iterable<SmaliValue>{

    private final SmaliSet<SmaliValue> values;

    public SmaliValueArray(){
        super();
        this.values = new SmaliSet<>();
        this.values.setParent(this);
    }

    public SmaliSet<SmaliValue> getValues() {
        return values;
    }


    public boolean contains(SmaliValue value) {
        return values.contains(value);
    }
    public SmaliValue remove(int i) {
        return values.remove(i);
    }
    public boolean remove(SmaliValue value) {
        return values.remove(value);
    }
    public void clear() {
        values.clear();
    }
    public boolean add(SmaliValue value) {
        return values.add(value);
    }
    public SmaliValue get(int i) {
        return values.get(i);
    }
    public int size() {
        return values.size();
    }
    public boolean isEmpty(){
        return values.isEmpty();
    }
    @Override
    public Iterator<SmaliValue> iterator() {
        return values.iterator();
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ARRAY;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('{');
        Iterator<SmaliValue> iterator = iterator();
        boolean appendOnce = false;
        while (iterator.hasNext()){
            SmaliValue value = iterator.next();
            if(appendOnce){
                writer.append(',');
            }else {
                writer.indentPlus();
            }
            writer.newLine();
            value.append(writer);
            appendOnce = true;
        }
        if(appendOnce){
            writer.indentMinus();
            writer.newLine();
        }
        writer.append('}');
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, '{');
        reader.skipWhitespaces();
        SmaliValue value;
        while ((value = createNext(reader)) != null){
            add(value);
            value.parse(reader);
            reader.skipWhitespacesOrComment();
            if(reader.get() == ','){
                reader.skip(1);
            }
        }
        reader.skipWhitespaces();
        if(reader.readASCII() != '}'){
            // throw
        }
    }
    private SmaliValue createNext(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        byte b = reader.get();
        if(b == '}'){
            return null;
        }
        return SmaliValue.create(reader);
    }
}
