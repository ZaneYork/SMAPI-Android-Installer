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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.dex.smali.model.SmaliField;
import com.reandroid.dex.smali.model.SmaliMethod;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;

public class ClassData extends DataItem implements SmaliFormat {

    private final Ule128Item staticFieldsCount;
    private final Ule128Item instanceFieldsCount;
    private final Ule128Item directMethodsCount;
    private final Ule128Item virtualMethodCount;

    private StaticFieldDefArray staticFields;
    private FieldDefArray instanceFields;
    private MethodDefArray directMethods;
    private MethodDefArray virtualMethods;

    public ClassData() {
        super(8);
        this.staticFieldsCount = new Ule128Item();
        this.instanceFieldsCount = new Ule128Item();
        this.directMethodsCount = new Ule128Item();
        this.virtualMethodCount = new Ule128Item();

        addChild(0, staticFieldsCount);
        addChild(1, instanceFieldsCount);
        addChild(2, directMethodsCount);
        addChild(3, virtualMethodCount);
    }

    @Override
    public SectionType<ClassData> getSectionType() {
        return SectionType.CLASS_DATA;
    }

    public void remove(Key key){
        Def<?> def = get(key);
        if(def != null){
            def.removeSelf();
        }
    }
    public void removeField(FieldKey key){
        Def<?> def = getField(key);
        if(def != null){
            def.removeSelf();
        }
    }
    public void removeMethod(MethodKey key){
        Def<?> def = getMethod(key);
        if(def != null){
            def.removeSelf();
        }
    }
    public Def<?> get(Key key){
        if(key instanceof FieldKey){
            return getField((FieldKey) key);
        }
        if(key instanceof MethodKey){
            return getMethod((MethodKey) key);
        }
        if(key != null){
            throw new RuntimeException("Unknown key type: "
                    + key.getClass() + ", '" + key + "'");
        }
        return null;
    }
    public FieldDef getField(FieldKey key){
        FieldDef fieldDef = null;
        FieldDefArray fieldDefArray = this.staticFields;
        if(fieldDefArray != null){
            fieldDef = fieldDefArray.get(key);
        }
        if(fieldDef == null){
            fieldDefArray = this.instanceFields;
            if(fieldDefArray != null){
                fieldDef = fieldDefArray.get(key);
            }
        }
        return fieldDef;
    }
    public MethodDef getMethod(MethodKey key){
        MethodDef methodDef = null;
        MethodDefArray methodDefArray = this.directMethods;
        if(methodDefArray != null){
            methodDef = methodDefArray.get(key);
        }
        if(methodDef == null){
            methodDefArray = this.virtualMethods;
            if(methodDefArray != null){
                methodDef = methodDefArray.get(key);
            }
        }
        return methodDef;
    }
    public FieldDef getOrCreateStatic(FieldKey fieldKey){
        FieldDef fieldDef = initStaticFieldsArray().getOrCreate(fieldKey);
        fieldDef.addAccessFlag(AccessFlag.STATIC);
        return fieldDef;
    }
    public FieldDef getOrCreateInstance(FieldKey fieldKey){
        return initInstanceFieldsArray().getOrCreate(fieldKey);
    }
    public void ensureStaticConstructor(String type){
        MethodKey methodKey = new MethodKey(type, "<clinit>", null, "V");
        MethodDef methodDef = getMethod(methodKey);
        if(methodDef != null){
            return;
        }
        methodDef = initDirectMethodsArray().getOrCreate(methodKey);
        methodDef.addAccessFlag(AccessFlag.STATIC);
        methodDef.addAccessFlag(AccessFlag.CONSTRUCTOR);
        InstructionList instructionList = methodDef.getCodeItem().getInstructionList();
        instructionList.add(Opcode.RETURN_VOID.newInstance());
    }

    public MethodDef getOrCreateDirect(MethodKey methodKey){
        return initDirectMethodsArray().getOrCreate(methodKey);
    }
    public MethodDef getOrCreateVirtual(MethodKey methodKey){
        return initVirtualMethodsArray().getOrCreate(methodKey);
    }
    public Iterator<Ins> getInstructions(){
        return new IterableIterator<MethodDef, Ins>(getMethods()){
            @Override
            public Iterator<Ins> iterator(MethodDef element) {
                return element.getInstructions();
            }
        };
    }
    public Iterator<FieldDef> getFields(){
        return new CombiningIterator<>(getStaticFields(), getInstanceFields());
    }
    public Iterator<MethodDef> getMethods(){
        return new CombiningIterator<>(getDirectMethods(), getVirtualMethods());
    }
    public Iterator<MethodDef> getDirectMethods(){
        MethodDefArray methodDefArray = this.directMethods;
        if(methodDefArray == null){
            return EmptyIterator.of();
        }
        return methodDefArray.arrayIterator();
    }
    public Iterator<MethodDef> getConstructors(){
        return FilterIterator.of(getDirectMethods(), MethodDef::isConstructor);
    }

    public Iterator<MethodDef> getVirtualMethods(){
        MethodDefArray methodDefArray = this.virtualMethods;
        if(methodDefArray == null){
            return EmptyIterator.of();
        }
        return methodDefArray.arrayIterator();
    }
    public Iterator<FieldDef> getStaticFields(){
        FieldDefArray fieldDefArray = this.staticFields;
        if(fieldDefArray == null){
            return EmptyIterator.of();
        }
        return fieldDefArray.arrayIterator();
    }
    public Iterator<FieldDef> getInstanceFields(){
        FieldDefArray fieldDefArray = this.instanceFields;
        if(fieldDefArray == null){
            return EmptyIterator.of();
        }
        return fieldDefArray.arrayIterator();
    }
    public int getStaticFieldsCount() {
        FieldDefArray fieldDefArray = this.staticFields;
        if(fieldDefArray == null){
            return 0;
        }
        return fieldDefArray.getCount();
    }
    public int getInstanceFieldsCount() {
        FieldDefArray fieldDefArray = this.instanceFields;
        if(fieldDefArray == null){
            return 0;
        }
        return fieldDefArray.getCount();
    }
    public int getDirectMethodsCount() {
        MethodDefArray methodDefArray = this.directMethods;
        if(methodDefArray == null){
            return 0;
        }
        return methodDefArray.getCount();
    }
    public int getVirtualMethodsCount() {
        MethodDefArray methodDefArray = this.virtualMethods;
        if(methodDefArray == null){
            return 0;
        }
        return methodDefArray.getCount();
    }
    public StaticFieldDefArray getStaticFieldsArray(){
        return staticFields;
    }
    public FieldDefArray getInstanceFieldsArray(){
        return instanceFields;
    }
    public MethodDefArray getDirectMethodsArray(){
        return directMethods;
    }
    public MethodDefArray getVirtualMethodArray(){
        return virtualMethods;
    }

    private FieldDefArray initStaticFieldsArray() {
        StaticFieldDefArray fieldDefArray = this.staticFields;
        if(fieldDefArray == null){
            fieldDefArray = new StaticFieldDefArray(staticFieldsCount);
            this.staticFields = fieldDefArray;
            addChild(4, staticFields);
        }
        return fieldDefArray;
    }
    private FieldDefArray initInstanceFieldsArray() {
        FieldDefArray fieldDefArray = this.instanceFields;
        if(fieldDefArray == null){
            fieldDefArray = new FieldDefArray(instanceFieldsCount);
            this.instanceFields = fieldDefArray;
            addChild(5, instanceFields);
        }
        return fieldDefArray;
    }
    private MethodDefArray initDirectMethodsArray() {
        MethodDefArray methodDefArray = this.directMethods;
        if(methodDefArray == null){
            methodDefArray = new MethodDefArray(directMethodsCount);
            this.directMethods = methodDefArray;
            addChild(6, methodDefArray);
        }
        return methodDefArray;
    }
    private MethodDefArray initVirtualMethodsArray() {
        MethodDefArray methodDefArray = this.virtualMethods;
        if(methodDefArray == null){
            methodDefArray = new MethodDefArray(virtualMethodCount);
            this.virtualMethods = methodDefArray;
            addChild(7, methodDefArray);
        }
        return methodDefArray;
    }
    private Iterator<DefArray<?>> getDefArrays() {
        return ArrayIterator.of(getChildes(), 4, 4);
    }


    public void setClassId(ClassId classId) {
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()){
            iterator.next().setClassId(classId);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        if(staticFieldsCount.get() != 0){
            initStaticFieldsArray().onReadBytes(reader);
        }
        if(instanceFieldsCount.get() != 0){
            initInstanceFieldsArray().onReadBytes(reader);
        }
        if(directMethodsCount.get() != 0){
            initDirectMethodsArray().onReadBytes(reader);
        }
        if(virtualMethodCount.get() != 0){
            initVirtualMethodsArray().onReadBytes(reader);
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()){
            iterator.next().sort(CompareUtil.getComparableComparator());
        }
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()){
            iterator.next().clearChildes();
        }
        setClassId(null);
    }
    public void replaceKeys(Key search, Key replace){
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()){
            iterator.next().replaceKeys(search, replace);
        }
    }

    @Override
    public void editInternal(Block user) {
        if(staticFields != null){
            staticFields.editInternal(user);
        }
        if(instanceFields != null){
            instanceFields.editInternal(user);
        }
        if(directMethods != null){
            directMethods.editInternal(user);
        }
        if(virtualMethods != null){
            virtualMethods.editInternal(user);
        }
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<DefArray<?>, IdItem>(getDefArrays()) {
            @Override
            public Iterator<IdItem> iterator(DefArray<?> element) {
                return element.usedIds();
            }
        };
    }
    public void merge(ClassData classData){
        if(classData.getStaticFieldsCount() != 0){
            initStaticFieldsArray().merge(classData.staticFields);
        }
        if(classData.getInstanceFieldsCount() != 0){
            initInstanceFieldsArray().merge(classData.instanceFields);
        }
        if(classData.getDirectMethodsCount() != 0){
            initDirectMethodsArray().merge(classData.directMethods);
        }
        if(classData.getVirtualMethodsCount() != 0){
            initVirtualMethodsArray().merge(classData.virtualMethods);
        }
    }
    public void fromSmali(SmaliClass smaliClass) throws IOException {
        Iterator<SmaliField> smaliStaticFields = smaliClass.getStaticFields();
        if(smaliStaticFields.hasNext()){
            FieldDefArray defArray = initStaticFieldsArray();
            defArray.fromSmali(smaliStaticFields);
        }
        Iterator<SmaliField> smaliInstanceFields = smaliClass.getInstanceFields();
        if(smaliInstanceFields.hasNext()){
            FieldDefArray defArray = initInstanceFieldsArray();
            defArray.fromSmali(smaliInstanceFields);
        }
        Iterator<SmaliMethod> smaliDirectMethods = smaliClass.getDirectMethods();
        if(smaliDirectMethods.hasNext()){
            MethodDefArray defArray = initDirectMethodsArray();
            defArray.fromSmali(smaliDirectMethods);
        }
        Iterator<SmaliMethod> smaliVirtualMethods = smaliClass.getVirtualMethods();
        if(smaliVirtualMethods.hasNext()){
            MethodDefArray defArray = initVirtualMethodsArray();
            defArray.fromSmali(smaliVirtualMethods);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendOptional(staticFields, "static fields");
        writer.appendOptional(instanceFields, "instance fields");
        writer.appendOptional(directMethods, "direct methods");
        writer.appendOptional(virtualMethods, "virtual methods");
    }
    @Override
    public String toString() {
        return "staticFieldsCount=" + staticFieldsCount +
                ", instanceFieldCount=" + instanceFieldsCount +
                ", directMethodCount=" + directMethodsCount +
                ", virtualMethodCount=" + virtualMethodCount;
    }

}
