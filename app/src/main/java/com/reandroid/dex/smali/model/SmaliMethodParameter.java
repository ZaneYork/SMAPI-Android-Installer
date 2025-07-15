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

import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliMethodParameter extends SmaliDebug implements SmaliRegion {

    private final SmaliRegisterSet registerSet;
    private StringKey name;
    private SmaliAnnotationSet annotationSet;

    public SmaliMethodParameter(){
        super();
        registerSet = new SmaliRegisterSet();
    }

    public SmaliRegister getSmaliRegister(){
        SmaliRegisterSet registerSet = getRegisterSet();
        if(registerSet.isEmpty()){
            return null;
        }
        return registerSet.get(0);
    }
    SmaliRegisterSet getRegisterSet() {
        return registerSet;
    }

    public StringKey getNameKey() {
        return name;
    }
    public void setName(StringKey name) {
        this.name = name;
    }
    public String getName() {
        StringKey key = getNameKey();
        if(key != null){
            return key.getString();
        }
        return null;
    }

    public boolean hasAnnotations(){
        SmaliAnnotationSet annotationSet = getAnnotationSet();
        return annotationSet != null && !annotationSet.isEmpty();
    }
    public SmaliAnnotationSet getAnnotationSet() {
        return annotationSet;
    }
    public void setAnnotationSet(SmaliAnnotationSet annotationSet) {
        this.annotationSet = annotationSet;
        if(annotationSet != null){
            annotationSet.setParent(this);
        }
    }
    public int getDefinitionIndex(){
        SmaliMethod smaliMethod = getParentInstance(SmaliMethod.class);
        if(smaliMethod == null){
            return -1;
        }
        ProtoKey protoKey = smaliMethod.getProtoKey();
        if(protoKey == null){
            return -1;
        }
        SmaliRegister smaliRegister = getSmaliRegister();
        if(smaliRegister == null){
            return -1;
        }
        int index = smaliRegister.getNumber();
        if(!smaliMethod.isStatic()){
            index = index - 1;
        }
        return protoKey.getParameterIndex(index);
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PARAM;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        SmaliDirective directive = getSmaliDirective();
        directive.append(writer);
        getRegisterSet().append(writer);
        StringKey name = getNameKey();
        if(name != null){
            writer.append(", ");
            name.append(writer);
        }
        SmaliAnnotationSet annotationSet = getAnnotationSet();
        if(annotationSet != null){
            writer.indentPlus();
            writer.newLine();
            annotationSet.append(writer);
            writer.indentMinus();
            directive.appendEnd(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        super.parse(reader);
        getRegisterSet().parse(reader);
        reader.skipWhitespacesOrComment();
        if(reader.get() == ','){
            reader.skip(1);
            reader.skipWhitespacesOrComment();
        }
        parseName(reader);
        parseAnnotationSet(reader);
    }
    private void parseName(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        if(reader.get() == '"'){
            setName(StringKey.read(reader));
        }
    }
    private void parseAnnotationSet(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != SmaliDirective.ANNOTATION){
            return;
        }
        int position = reader.position();
        SmaliAnnotationSet annotationSet = new SmaliAnnotationSet();
        annotationSet.parse(reader);
        reader.skipWhitespacesOrComment();
        if(getSmaliDirective().isEnd(reader)){
            setAnnotationSet(annotationSet);
            SmaliDirective.parse(reader);
        }else {
            // put back, it is method annotation
            reader.position(position);
        }
    }
}
