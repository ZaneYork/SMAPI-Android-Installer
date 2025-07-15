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
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class SmaliField extends SmaliDef{

    private TypeKey type;
    private SmaliValue value;

    public SmaliField(){
        super();
    }

    @Override
    public FieldKey getKey(){
        TypeKey defining = getDefining();
        if(defining != null){
            return getKey(defining);
        }
        return null;
    }
    public FieldKey getKey(TypeKey declaring){
        TypeKey type = getType();
        if(type == null){
            return null;
        }
        return new FieldKey(
                declaring.getTypeName(),
                getName(),
                type.getTypeName());
    }

    public TypeKey getType() {
        return type;
    }
    public void setType(TypeKey type) {
        this.type = type;
    }

    public SmaliValue getValue() {
        return value;
    }

    public void setValue(SmaliValue value) {
        this.value = value;
        if(value != null){
            value.setParent(this);
        }
    }
    void fixUninitializedFinalValue() {
        if(this.getValue() != null || !isStatic() || !isFinal()) {
            return;
        }
        SmaliClass smaliClass = getSmaliClass();
        FieldKey fieldKey = getKey();
        if(smaliClass == null || fieldKey == null) {
            return;
        }
        if(!isInitializedInStaticConstructor(smaliClass, fieldKey)) {
            setValue(SmaliValue.createDefaultFor(fieldKey.getType()));
        }
    }
    private boolean isInitializedInStaticConstructor(SmaliClass smaliClass, FieldKey fieldKey) {
        SmaliMethod method = smaliClass.getStaticConstructor();
        if(method == null) {
            return false;
        }
        Iterator<SmaliInstruction> iterator = method.getInstructions();
        while (iterator.hasNext()) {
            SmaliInstruction instruction = iterator.next();
            if(fieldKey.equals(instruction.getKey())) {
                return instruction.getOpcode().isFieldStaticPut();
            }
        }
        return false;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.FIELD;
    }

    public boolean isInstance(){
        return !isStatic();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        Modifier.append(writer, getAccessFlags());
        writer.append(getName());
        writer.append(':');
        getType().append(writer);
        SmaliValue value = getValue();
        if(value != null){
            writer.append(" = ");
            value.append(writer);
        }
        SmaliAnnotationSet annotationSet = getAnnotation();
        if(annotationSet != null && !annotationSet.isEmpty()){
            writer.indentPlus();
            writer.newLine();
            annotationSet.append(writer);
            writer.indentMinus();
            getSmaliDirective().appendEnd(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        SmaliParseException.expect(reader, getSmaliDirective());
        setAccessFlags(AccessFlag.parse(reader));
        parseName(reader);
        setType(TypeKey.read(reader));
        parseValue(reader);
        parseAnnotationSet(reader);
    }
    private void parseName(SmaliReader reader) {
        reader.skipWhitespaces();
        int length = reader.indexOf(':') - reader.position();
        setName(reader.readString(length));
        reader.skip(1); // :
    }
    private void parseValue(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        if(reader.finished()) {
            return;
        }
        if(reader.get() != '='){
            return;
        }
        reader.skip(1); // =
        reader.skipWhitespaces();
        SmaliValue value = SmaliValue.create(reader);
        setValue(value);
        value.parse(reader);
    }
    private void parseAnnotationSet(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != SmaliDirective.ANNOTATION){
            getSmaliDirective().skipEnd(reader);
            return;
        }
        int position = reader.position();
        SmaliAnnotationSet annotationSet = new SmaliAnnotationSet();
        annotationSet.parse(reader);
        reader.skipWhitespacesOrComment();
        if(getSmaliDirective().isEnd(reader)){
            setAnnotation(annotationSet);
            SmaliDirective.parse(reader);
        }else {
            // put back, it is method annotation
            reader.position(position);
        }
    }
    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder();
        TypeKey typeKey = getDefining();
        if(typeKey != null){
            builder.append(typeKey);
            builder.append(", ");
        }
        builder.append("field = ");
        builder.append(getName());
        builder.append(':');
        builder.append(getType());
        SmaliValue value = getValue();
        if(value != null){
            builder.append(" = ");
            builder.append(value.toDebugString());
        }
        return builder.toString();
    }
}
