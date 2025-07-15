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

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Iterator;

public class SmaliMethodSet extends SmaliDefSet<SmaliMethod>{

    public SmaliMethodSet(){
        super();
    }

    public Iterator<SmaliMethod> getDirectMethods() {
        return FilterIterator.of(iterator(), SmaliMethod::isDirect);
    }
    public Iterator<SmaliMethod> getVirtualMethods() {
        return FilterIterator.of(iterator(), SmaliMethod::isVirtual);
    }

    @Override
    SmaliMethod createNew() {
        return new SmaliMethod();
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.METHOD;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        boolean appendOnce = false;
        Iterator<SmaliMethod> iterator = getDirectMethods();
        while (iterator.hasNext()){
            writer.newLineDouble();
            if(!appendOnce){
                writer.appendComment("direct methods");
                writer.newLine();
            }
            iterator.next().append(writer);
            appendOnce = true;
        }
        appendOnce = false;
        iterator = getVirtualMethods();
        while (iterator.hasNext()){
            writer.newLineDouble();
            if(!appendOnce){
                writer.appendComment("virtual methods");
                writer.newLine();
            }
            iterator.next().append(writer);
            appendOnce = true;
        }
    }

    public static SmaliMethodSet read(SmaliReader reader) throws IOException {
        SmaliMethodSet smali = new SmaliMethodSet();
        smali.parse(reader);
        if(!smali.isEmpty()) {
            return smali;
        }
        return null;
    }
}
