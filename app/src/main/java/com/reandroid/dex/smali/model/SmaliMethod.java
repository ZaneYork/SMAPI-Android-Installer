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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class SmaliMethod extends SmaliDef implements RegistersTable{

    private ProtoKey protoKey;
    private Integer locals;

    private final SmaliParamSet paramSet;
    private final SmaliCodeSet codeSet;

    public SmaliMethod(){
        super();
        this.paramSet = new SmaliParamSet();
        this.codeSet = new SmaliCodeSet();

        this.paramSet.setParent(this);
        this.codeSet.setParent(this);
    }

    @Override
    public MethodKey getKey(){
        TypeKey typeKey = getDefining();
        if(typeKey != null) {
            return getKey(typeKey);
        }
        return null;
    }
    public MethodKey getKey(TypeKey declaring){
        ProtoKey protoKey = getProtoKey();
        if(protoKey == null){
            return null;
        }
        return new MethodKey(
                declaring,
                getName(),
                protoKey.getParameterNames(),
                protoKey.getReturnType());
    }

    public boolean hasInstructions(){
        return getInstructions().hasNext();
    }
    public Iterator<SmaliInstruction> getInstructions(){
        return getCodeSet().getInstructions();
    }
    public boolean hasDebugElements(){
        return getDebugElements().hasNext();
    }
    public Iterator<SmaliDebugElement> getDebugElements(){
        return getCodeSet().getDebugElements();
    }
    public Integer getLocals() {
        return locals;
    }
    public void setLocals(Integer locals) {
        this.locals = locals;
    }
    public ProtoKey getProtoKey() {
        return protoKey;
    }
    public void setProtoKey(ProtoKey protoKey) {
        this.protoKey = protoKey;
    }

    public SmaliParamSet getParamSet() {
        return paramSet;
    }
    public Iterator<SmaliMethodParameter> getParameters(){
        return getParamSet().iterator();
    }
    public SmaliCodeSet getCodeSet() {
        return codeSet;
    }
    public Iterator<SmaliCodeTryItem> getTryItems(){
        return getCodeSet().getTryItems();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.METHOD;
    }


    public boolean isConstructor(){
        return Modifier.contains(getAccessFlags(), AccessFlag.CONSTRUCTOR);
    }
    public boolean isDirect(){
        return isConstructor() || isStatic() || isPrivate();
    }
    public boolean isVirtual(){
        return !isDirect();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        Modifier.append(writer, getAccessFlags());
        writer.append(getName());
        getProtoKey().append(writer);
        writer.indentPlus();
        Integer locals = getLocals();
        if(locals != null){
            writer.newLine();
            SmaliDirective.LOCALS.append(writer);
            writer.appendInteger(locals);
        }
        getParamSet().append(writer);
        if(hasAnnotation()){
            writer.newLine();
            getAnnotation().append(writer);
        }
        getCodeSet().append(writer);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, getSmaliDirective());
        setAccessFlags(AccessFlag.parse(reader));
        parseName(reader);
        parseProto(reader);
        reader.skipWhitespacesOrComment();
        while (parseNoneCode(reader)){
            reader.skipWhitespacesOrComment();
        }
        getCodeSet().parse(reader);
        SmaliParseException.expect(reader, getSmaliDirective(), true);
    }
    private boolean parseNoneCode(SmaliReader reader) throws IOException {
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive == SmaliDirective.LOCALS){
            parseLocals(reader);
            return true;
        }
        if(directive == SmaliDirective.ANNOTATION){
            getOrCreateAnnotation().parse(reader);
            return true;
        }
        if(directive == SmaliDirective.PARAM){
            getParamSet().parse(reader);
            return true;
        }
        return false;
    }
    private void parseLocals(SmaliReader reader) throws IOException {
        SmaliParseException.expect(reader, SmaliDirective.LOCALS);
        reader.skipSpaces();
        setLocals(reader.readInteger());
    }
    private void parseName(SmaliReader reader) {
        reader.skipWhitespaces();
        int length = reader.indexOf('(') - reader.position();
        setName(reader.readString(length));
    }
    private void parseProto(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        setProtoKey(ProtoKey.read(reader));
    }

    @Override
    public int getRegistersCount() {
        return getLocalRegistersCount() + getParameterRegistersCount();
    }

    @Override
    public int getParameterRegistersCount() {
        int count = isStatic() ? 0 : 1;
        ProtoKey protoKey = getProtoKey();
        if(protoKey != null){
            count += protoKey.getParameterRegistersCount();
        }
        return count;
    }
    @Override
    public void setRegistersCount(int count) {
    }
    @Override
    public void setParameterRegistersCount(int count) {
    }
    @Override
    public boolean ensureLocalRegistersCount(int count) {
        // FIXME
        return true;
    }
    @Override
    public int getLocalRegistersCount() {
        Integer locals = getLocals();
        if(locals != null){
            return locals;
        }
        return 0;
    }

    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder();
        TypeKey typeKey = getDefining();
        if(typeKey != null){
            builder.append(typeKey);
            builder.append(", ");
        }
        builder.append("method = ");
        builder.append(getName());
        builder.append(getProtoKey());
        return builder.toString();
    }
}
