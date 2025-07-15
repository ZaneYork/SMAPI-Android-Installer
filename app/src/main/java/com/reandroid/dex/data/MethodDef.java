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
package com.reandroid.dex.data;

import android.text.TextUtils;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.id.*;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.TryBlock;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.DataItemUle128Reference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliMethod;
import com.reandroid.dex.smali.model.SmaliMethodParameter;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class MethodDef extends Def<MethodId>{

    private final DataItemUle128Reference<CodeItem> codeOffset;

    public MethodDef() {
        super(1, SectionType.METHOD_ID);
        this.codeOffset = new DataItemUle128Reference<>(SectionType.CODE, UsageMarker.USAGE_DEFINITION);
        addChild(2, codeOffset);
    }
    public boolean isBridge(){
        return AccessFlag.BRIDGE.isSet(getAccessFlagsValue());
    }

    @Override
    public boolean isDirect(){
        return isConstructor() || isPrivate() || isStatic();
    }
    public String getName() {
        MethodId methodId = getId();
        if(methodId != null) {
            return methodId.getName();
        }
        return null;
    }
    public void setName(String name) {
        if(Objects.equals(getName(), name)){
            return;
        }
        getId().setName(name);
    }
    public int getParametersCount(){
        MethodId methodId = getId();
        if(methodId != null){
            return methodId.getParametersCount();
        }
        return 0;
    }
    public int getParameterRegistersCount(){
        MethodId methodId = getId();
        if(methodId != null){
            int count = methodId.getParameterRegistersCount();
            if(!isStatic()){
                count = count + 1;
            }
            return count;
        }
        return 0;
    }
    public Parameter getParameter(int index){
        if(index < 0 || index >= getParametersCount()){
            return null;
        }
        return new Parameter(this, index);
    }
    public Iterator<Parameter> getParameters(){
        return getParameters(false);
    }
    public Iterator<Parameter> getParameters(boolean skipEmpty){
        if(getParametersCount() == 0){
            return EmptyIterator.of();
        }
        Iterator<Parameter> iterator = ArraySupplierIterator.of(new ArraySupplier<Parameter>() {
            @Override
            public Parameter get(int i) {
                return MethodDef.this.getParameter(i);
            }

            @Override
            public int getCount() {
                return MethodDef.this.getParametersCount();
            }
        });
        if(!skipEmpty) {
            return iterator;
        }
        return FilterIterator.of(iterator, parameter -> !parameter.isEmpty());
    }
    @Override
    public MethodKey getKey(){
        return (MethodKey) super.getKey();
    }

    @Override
    void onRemove() {
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.setMethodDef(null);
            this.codeOffset.setItem((CodeItem) null);
        }
        super.onRemove();
    }

    public void removeParameter(int index){
        Parameter parameter = getParameter(index);
        if(parameter == null){
            return;
        }
        parameter.remove();
        MethodKey methodKey = getKey();
        if(methodKey == null){
            return;
        }
        methodKey = methodKey.removeParameter(index);
        setItem(methodKey);
    }
    public void setDebugInfo(DebugInfo debugInfo){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.setDebugInfo(debugInfo);
        }
    }
    public DebugInfo getDebugInfo(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getDebugInfo();
        }
        return null;
    }
    public DebugInfo getUniqueDebugInfo(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getDebugInfo();
        }
        return null;
    }
    public DebugInfo getOrCreateDebugInfo(){
        return getOrCreateCodeItem().getOrCreateDebugInfo();
    }
    public ProtoId getProtoId(){
        MethodId methodId = getId();
        if(methodId != null){
            return methodId.getProto();
        }
        return null;
    }
    public Iterator<Ins> getInstructions() {
        return new ArraySupplierIterator<>(new ArraySupplier<Ins>() {
            @Override
            public Ins get(int i) {
                return getInstruction(i);
            }
            @Override
            public int getCount() {
                return getInstructionsCount();
            }
        });
    }
    public Ins getInstruction(int i) {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.get(i);
        }
        return null;
    }
    public Ins getInstructionAt(int address) {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.getAtAddress(address);
        }
        return null;
    }
    public int getInstructionsCount() {
        InstructionList instructionList = getInstructionList();
        if(instructionList != null) {
            return instructionList.getCount();
        }
        return 0;
    }

    public InstructionList getOrCreateInstructionList(){
        return getOrCreateCodeItem().getInstructionList();
    }
    public InstructionList getInstructionList(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getInstructionList();
        }
        return null;
    }
    public TryBlock getTryBlock(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            return codeItem.getTryBlock();
        }
        return null;
    }
    public TryBlock getOrCreateTryBlock(){
        return getOrCreateCodeItem().getOrCreateTryBlock();
    }
    public CodeItem getOrCreateCodeItem(){
        CodeItem current = codeOffset.getItem();
        CodeItem codeItem = codeOffset.getOrCreateUniqueItem(this);
        if(current == null){
            codeItem.setMethodDef(this);
            int registers = getParameterRegistersCount();
            codeItem.setRegistersCount(registers);
            codeItem.setParameterRegistersCount(registers);
        }
        return codeItem;
    }
    public CodeItem getCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.setMethodDef(this);
        }
        return codeItem;
    }
    public void clearCode(){
        codeOffset.setItem((CodeItem) null);
    }
    public void clearDebug(){
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.removeDebugInfo();
        }
    }
    private void linkCodeItem(){
        CodeItem codeItem = codeOffset.getItem();
        if(codeItem != null){
            codeItem.addUniqueUser(this);
            codeItem.setMethodDef(this);
        }
    }

    public Iterator<AnnotationGroup> getParameterAnnotations(){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            return directory.getParameterAnnotation(this);
        }
        return EmptyIterator.of();
    }
    public Iterator<AnnotationSet> getParameterAnnotations(int parameterIndex){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory == null){
            return EmptyIterator.of();
        }
        return directory.getParameterAnnotation(getDefinitionIndex(), parameterIndex);
    }
    @Override
    public Iterator<? extends Modifier> getAccessFlags(){
        return AccessFlag.valuesOfMethod(getAccessFlagsValue());
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        linkCodeItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.onWriteMethod(getKey());
        writer.newLine();
        getSmaliDirective().append(writer);

        writer.appendModifiers(getModifiers());

        getId().append(writer, false);
        writer.indentPlus();
        if(!writer.appendOptional(getCodeItem())) {
            writer.appendAllWithDoubleNewLine(this.getParameters(true));
            writer.appendAllWithDoubleNewLine(this.getAnnotations(true));
        }
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }
    @Override
    public void replaceKeys(Key search, Key replace){
        super.replaceKeys(search, replace);
        CodeItem codeItem = getCodeItem();
        if(codeItem != null){
            codeItem.replaceKeys(search, replace);
        }
    }

    @Override
    public void edit(){
        CodeItem shared = codeOffset.getItem();
        CodeItem unique = codeOffset.getUniqueItem(this);
        if(unique != null) {
            unique.setMethodDef(this);
            if(shared != unique) {
                unique.edit();
                shared.getInstructionList()
                        .onEditing(unique.getInstructionList());
            }
        }
    }
    @Override
    public void editInternal(Block user) {
        this.edit();
    }

    @Override
    public Iterator<IdItem> usedIds(){
        Iterator<IdItem> iterator;
        CodeItem codeItem = getCodeItem();
        if(codeItem == null){
            iterator = EmptyIterator.of();
        }else {
            iterator = codeItem.usedIds();
        }
        return CombiningIterator.singleOne(getId(), iterator);
    }
    @Override
    public void merge(Def<?> def){
        super.merge(def);
        MethodDef comingMethod = (MethodDef) def;
        CodeItem comingCode = comingMethod.getCodeItem();
        if(comingCode != null){
            this.codeOffset.setItem(comingCode.getKey());
        }
    }
    public void fromSmali(SmaliMethod smaliMethod) throws IOException {
        setKey(smaliMethod.getKey());
        setAccessFlagsValue(smaliMethod.getAccessFlagsValue());
        if(smaliMethod.hasInstructions()){
            getOrCreateCodeItem().fromSmali(smaliMethod);
        }
        if(smaliMethod.hasAnnotation()){
            AnnotationSet annotationSet = getOrCreateSection(SectionType.ANNOTATION_SET).createItem();
            annotationSet.fromSmali(smaliMethod.getAnnotation());
            addAnnotationSet(annotationSet);
        }
        Iterator<SmaliMethodParameter> iterator = smaliMethod.getParameters();
        while (iterator.hasNext()){
            SmaliMethodParameter smaliMethodParameter = iterator.next();
            int index = smaliMethodParameter.getDefinitionIndex();
            if(index < 0){
                MethodKey methodKey = smaliMethod.getKey();
                throw new RuntimeException("Parameter out of range, class = " +
                        methodKey.getDeclaring() + ", method = " + methodKey.getName() +
                        methodKey.getProtoKey() + "\n" + smaliMethodParameter);
            }
            Parameter parameter = getParameter(index);
            parameter.fromSmali(smaliMethodParameter);
        }
        linkCodeItem();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.METHOD;
    }

    @Override
    public String toString() {
        if(isReading()){
            return getSmaliDirective() + " " + getKey();
        }
        MethodId methodId = getId();
        if(methodId != null){
            return getSmaliDirective() + " " + Modifier.toString(getAccessFlags())
                    + " " + methodId.toString();
        }
        return getSmaliDirective() + " " + Modifier.toString(getAccessFlags())
                + " " + getRelativeIdValue();
    }
    public static class Parameter implements DefIndex, SmaliRegion {

        private final MethodDef methodDef;
        private final int index;

        public Parameter(MethodDef methodDef, int index){
            this.methodDef = methodDef;
            this.index = index;
        }

        public void remove(){
            clearAnnotations();
            clearDebugParameter();
        }

        public void clearAnnotations(){
            AnnotationsDirectory directory = this.methodDef.getUniqueAnnotationsDirectory();
            if(directory == null || !hasAnnotations()){
                return;
            }
            Iterator<DirectoryEntry<MethodDef, AnnotationGroup>> iterator =
                    directory.getParameterEntries(this.methodDef);
            int index = getDefinitionIndex();
            while (iterator.hasNext()){
                DirectoryEntry<MethodDef, AnnotationGroup> entry = iterator.next();
                AnnotationGroup group = entry.getValue();
                if(group == null || group.getItem(index) == null){
                    continue;
                }
                AnnotationGroup update = group.getSection(SectionType.ANNOTATION_GROUP)
                        .createItem();
                entry.setValue(update);
                update.put(index, 0);
                update.refresh();
            }
        }
        public boolean hasAnnotations(){
            return getAnnotations().hasNext();
        }
        public Iterator<AnnotationItem> getAnnotationItems(){
            return ExpandIterator.of(getAnnotations());
        }
        public Iterator<AnnotationSet> getAnnotations(){
            AnnotationsDirectory directory = this.methodDef.getAnnotationsDirectory();
            if(directory != null){
                return directory.getParameterAnnotation(this.methodDef, getDefinitionIndex());
            }
            return EmptyIterator.of();
        }
        public AnnotationItem addAnnotationItem(TypeKey typeKey){
            return getOrCreateAnnotationSet().addNewItem(typeKey);
        }
        public AnnotationItem getOrCreateAnnotationItem(TypeKey typeKey){
            return getOrCreateAnnotationSet().getOrCreate(typeKey);
        }
        public AnnotationSet getOrCreateAnnotationSet(){
            AnnotationsDirectory directory = this.methodDef.getOrCreateUniqueAnnotationsDirectory();
            return directory.getOrCreateParameterAnnotation(methodDef, getDefinitionIndex());
        }
        public AnnotationSet addNewAnnotationSet(){
            AnnotationsDirectory directory = this.methodDef.getOrCreateUniqueAnnotationsDirectory();
            return directory.createNewParameterAnnotation(methodDef, getDefinitionIndex());
        }
        public TypeKey getType() {
            TypeId typeId = getTypeId();
            if(typeId != null){
                return typeId.getKey();
            }
            return null;
        }
        public TypeId getTypeId() {
            ProtoId protoId = this.methodDef.getProtoId();
            if(protoId != null){
                return protoId.getParameter(getDefinitionIndex());
            }
            return null;
        }
        @Override
        public int getDefinitionIndex() {
            return index;
        }
        public int getRegister() {
            MethodDef methodDef = this.methodDef;
            int reg;
            if(methodDef.isStatic()){
                reg = 0;
            }else {
                reg = 1;
            }
            reg += methodDef.getKey().getRegister(getDefinitionIndex());
            return reg;
        }
        public void clearDebugParameter(){
            DebugInfo debugInfo = methodDef.getDebugInfo();
            if(debugInfo != null){
                debugInfo.removeDebugParameter(getDefinitionIndex());
            }
        }
        public String getDebugName(){
            DebugParameter debugParameter = getDebugParameter();
            if(debugParameter != null){
                return debugParameter.getName();
            }
            return null;
        }
        public void setDebugName(String name){
            if(TextUtils.isEmpty(name)){
                name = null;
            }
            DebugInfo debugInfo = methodDef.getDebugInfo();
            if(debugInfo == null){
                if(name == null){
                    return;
                }
                debugInfo = methodDef.getOrCreateDebugInfo();
            }
            if(name == null){
                debugInfo.removeDebugParameter(getDefinitionIndex());
                return;
            }
            DebugParameter parameter = debugInfo.getOrCreateDebugParameter(
                    getDefinitionIndex());
            parameter.setName(name);
        }
        public DebugParameter getDebugParameter(){
            DebugInfo debugInfo = methodDef.getDebugInfo();
            if(debugInfo != null){
                return debugInfo.getDebugParameter(getDefinitionIndex());
            }
            return null;
        }
        @Override
        public Key getKey() {
            TypeId typeId = getTypeId();
            if(typeId != null){
                return typeId.getKey();
            }
            return null;
        }
        public void fromSmali(SmaliMethodParameter smaliMethodParameter){
            if(smaliMethodParameter.hasAnnotations()){
                getOrCreateAnnotationSet().fromSmali(smaliMethodParameter.getAnnotationSet());
            }
            setDebugName(smaliMethodParameter.getName());
        }
        public boolean isEmpty() {
            DebugParameter debugParameter = getDebugParameter();
            if(debugParameter != null && debugParameter.getNameId() != null) {
                return false;
            }
            return !getAnnotationItems().hasNext();
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            DebugParameter debugParameter = getDebugParameter();
            boolean has_debug = debugParameter != null &&
                    debugParameter.getNameId() != null;
            Iterator<AnnotationSet> annotations = getAnnotations();
            boolean has_annotation = annotations.hasNext();
            if(!has_debug && !has_annotation){
                return;
            }
            getSmaliDirective().append(writer);
            writer.append('p');
            writer.appendInteger(getRegister());
            if(has_debug){
                debugParameter.append(writer);
            }
            writer.appendComment(getTypeId().getName());
            if(!has_annotation){
                return;
            }
            writer.indentPlus();
            writer.appendAllWithDoubleNewLine(annotations);
            writer.indentMinus();
            getSmaliDirective().appendEnd(writer);
        }
        @Override
        public int hashCode() {
            return methodDef.hashCode() * 31 + index;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Parameter parameter = (Parameter) obj;
            return index == parameter.index && this.methodDef == parameter.methodDef;
        }

        @Override
        public SmaliDirective getSmaliDirective() {
            return SmaliDirective.PARAM;
        }
        @Override
        public String toString() {
            return SmaliWriter.toStringSafe(this);
        }
    }
}
