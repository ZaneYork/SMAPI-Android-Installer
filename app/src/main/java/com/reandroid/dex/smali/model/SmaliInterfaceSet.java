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

import com.reandroid.dex.key.TypeListKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliInterfaceSet extends SmaliSet<SmaliInterface> implements SmaliRegion, Iterable<SmaliInterface> {

    public SmaliInterfaceSet(){
        super();
    }

    public TypeListKey getKey(){
        int size = size();
        if(size == 0){
            return null;
        }
        String[] typeNames = new String[size];
        for(int i = 0; i < size; i++){
            typeNames[i] = get(i).getTypeName();
        }
        return new TypeListKey(typeNames);
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.IMPLEMENTS;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(isEmpty()){
            return;
        }
        writer.newLine();
        writer.newLine();
        writer.newLine();
        writer.appendComment("interfaces");
        writer.newLine();
        writer.appendAll(iterator());
    }
    @Override
    SmaliInterface createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != getSmaliDirective()){
            return null;
        }
        return new SmaliInterface();
    }
    public static SmaliInterfaceSet read(SmaliReader reader) throws IOException {
        SmaliInterfaceSet smali = new SmaliInterfaceSet();
        smali.parse(reader);
        if(!smali.isEmpty()) {
            return smali;
        }
        return null;
    }
}
