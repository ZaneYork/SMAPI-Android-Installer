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
package com.reandroid.dex.id;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IndirectInteger;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.*;
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeListKey;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.reference.TypeListReference;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.dex.value.*;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class ClassId extends IdItem implements IdDefinition<TypeId>, Comparable<ClassId> {

    private final ClassTypeId classTypeId;
    private final IndirectInteger accessFlagValue;
    private final SuperClassId superClassId;
    private final TypeListReference interfaces;
    private final SourceFile sourceFile;
    private final DataItemIndirectReference<AnnotationsDirectory> annotationsDirectory;
    private final DataItemIndirectReference<ClassData> classData;
    private final DataItemIndirectReference<EncodedArray> staticValues;

    public ClassId() {
        super(SIZE);
        int offset = -4;
        
        this.classTypeId = new ClassTypeId(this, offset += 4);
        this.accessFlagValue = new IndirectInteger(this, offset += 4);
        this.superClassId = new SuperClassId(this, offset += 4);
        this.interfaces = new TypeListReference(this, offset += 4, USAGE_INTERFACE);
        this.sourceFile = new SourceFile(this, offset += 4);
        this.annotationsDirectory = new DataItemIndirectReference<>(SectionType.ANNOTATION_DIRECTORY, this, offset += 4, UsageMarker.USAGE_DEFINITION);
        this.classData = new DataItemIndirectReference<>(SectionType.CLASS_DATA, this, offset += 4, UsageMarker.USAGE_DEFINITION);
        this.staticValues = new DataItemIndirectReference<>(SectionType.ENCODED_ARRAY, this, offset += 4, UsageMarker.USAGE_STATIC_VALUES);
        addUsageType(UsageMarker.USAGE_DEFINITION);
    }

    @Override
    public void clearUsageType() {
    }

    @Override
    public void edit(){
        this.editInternal(this);
    }
    @Override
    public void editInternal(Block user) {
        annotationsDirectory.editInternal(this);
        classData.editInternal(this);
        staticValues.editInternal(this);
    }

    @Override
    public SectionType<ClassId> getSectionType(){
        return SectionType.CLASS_ID;
    }
    @Override
    public TypeKey getKey(){
        return checkKey(TypeKey.create(getName()));
    }
    @Override
    public void setKey(Key key){
        TypeKey old = getKey();
        if(Objects.equals(old, key)){
            return;
        }
        this.classTypeId.setItem(key);
        keyChanged(old);
    }
    public String getName(){
        TypeId typeId = getId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public void setName(String typeName){
        setKey(new TypeKey(typeName));
    }

    public ClassTypeId getClassTypeId(){
        return classTypeId;
    }
    @Override
    public TypeId getId(){
        return getClassTypeId().getItem();
    }
    @Override
    public int getAccessFlagsValue() {
        return accessFlagValue.get();
    }
    @Override
    public Iterator<? extends Modifier> getAccessFlags(){
        return AccessFlag.valuesOfClass(getAccessFlagsValue());
    }
    @Override
    public void setAccessFlagsValue(int value) {
        accessFlagValue.set(value);
    }
    public void setId(TypeId typeId){
        this.classTypeId.setItem(typeId);
    }
    public SuperClassId getSuperClassId(){
        return superClassId;
    }
    public TypeId getSuperClassType(){
        return getSuperClassId().getItem();
    }
    public TypeKey getSuperClassKey(){
        return getSuperClassId().getKey();
    }
    public void setSuperClass(TypeKey typeKey){
        this.superClassId.setItem(typeKey);
    }
    public void setSuperClass(String superClass){
        this.superClassId.setItem(new TypeKey(superClass));
    }
    public SourceFile getSourceFile(){
        return sourceFile;
    }
    public String getSourceFileName(){
        return getSourceFile().getString();
    }
    public void setSourceFile(String sourceFile){
        getSourceFile().setString(sourceFile);
    }

    public Iterator<TypeKey> getInstanceKeys(){
        return CombiningIterator.singleOne(getSuperClassKey(), getInterfaceKeys());
    }
    public Iterator<TypeKey> getInterfaceKeys(){
        return getInterfacesReference().getTypeKeys();
    }
    public TypeList getInterfaceTypeList(){
        return interfaces.getItem();
    }
    public TypeListReference getInterfacesReference() {
        return this.interfaces;
    }
    public void setInterfaces(TypeListKey typeListKey){
        this.interfaces.setItem(typeListKey);
    }

    public Key getDalvikEnclosing(){
        TypeValue typeValue = getDalvikEnclosingClass();
        if(typeValue != null){
            return typeValue.getKey();
        }
        MethodIdValue methodIdValue = getDalvikEnclosingMethodId();
        if(methodIdValue != null){
            return methodIdValue.getKey();
        }
        return null;
    }
    public MethodIdValue getDalvikEnclosingMethodId(){
        AnnotationItem annotationItem = getDalvikEnclosingMethod();
        if(annotationItem != null){
            DexValueBlock<?> value = annotationItem.getElementValue(Key.DALVIK_value);
            if(value instanceof MethodIdValue){
                return (MethodIdValue) value;
            }
        }
        return null;
    }
    public Iterator<TypeKey> getMemberClassKeys(){
        return ComputeIterator.of(getDalvikMemberClasses(), TypeValue::getKey);
    }
    public Iterator<TypeValue> getDalvikMemberClasses(){
        AnnotationItem annotationItem = getDalvikMemberClass();
        if(annotationItem != null){
            DexValueBlock<?> value = annotationItem.getElementValue(Key.DALVIK_value);
            if(value instanceof ArrayValue){
                return ((ArrayValue)value).iterator(TypeValue.class);
            }
        }
        return EmptyIterator.of();
    }
    public AnnotationItem getDalvikEnclosingMethod(){
        return getClassAnnotation(TypeKey.DALVIK_EnclosingMethod);
    }
    public AnnotationItem getDalvikMemberClass(){
        return getClassAnnotation(TypeKey.DALVIK_MemberClass);
    }
    public AnnotationItem getDalvikInnerClass(){
        AnnotationSet annotationSet = getClassAnnotations();
        if(annotationSet == null){
            return null;
        }
        return annotationSet.get(TypeKey.DALVIK_InnerClass);
    }
    public AnnotationItem getOrCreateDalvikInnerClass(){
        TypeKey typeKey = getKey();
        if(typeKey == null){
            return null;
        }
        String inner = typeKey.getSimpleInnerName();
        if(AccessFlag.SYNTHETIC.isSet(getAccessFlagsValue())
                || inner.equals(typeKey.getSimpleName())
                || StringsUtil.isDigits(inner)){
            inner = null;
        }
        return getOrCreateDalvikInnerClass(getAccessFlagsValue(), inner);
    }
    public AnnotationItem getOrCreateDalvikInnerClass(int flags, String name){
        AnnotationSet annotationSet = getOrCreateClassAnnotations();
        AnnotationItem item = annotationSet.getOrCreate(TypeKey.DALVIK_InnerClass);
        item.setVisibility(AnnotationVisibility.SYSTEM);

        AnnotationElement accessFlags = item.getOrCreateElement(Key.DALVIK_accessFlags);
        IntValue accessFlagsValue = accessFlags.getOrCreateValue(DexValueType.INT);
        accessFlagsValue.set(flags);

        AnnotationElement nameElement = item.getOrCreateElement(Key.DALVIK_name);

        if(name != null){
            StringValue stringValue = nameElement.getOrCreateValue(DexValueType.STRING);
            stringValue.setItem(new StringKey(name));
        }else {
            nameElement.getOrCreateValue(DexValueType.NULL);
        }
        return item;
    }
    public TypeValue getOrCreateDalvikEnclosingClass(){
        TypeKey key = getKey();
        if(key != null){
            TypeKey enclosing = key.getEnclosingClass();
            if(!key.equals(enclosing)){
                return getOrCreateDalvikEnclosingClass(enclosing);
            }
        }
        return null;
    }
    public TypeValue getOrCreateDalvikEnclosingClass(TypeKey enclosing){
        if(enclosing == null){
            return null;
        }
        AnnotationSet annotationSet = getOrCreateClassAnnotations();
        AnnotationItem item = annotationSet.getOrCreate(TypeKey.DALVIK_EnclosingClass);
        item.setVisibility(AnnotationVisibility.SYSTEM);
        AnnotationElement element = item.getOrCreateElement(Key.DALVIK_value);
        TypeValue typeValue = element.getOrCreateValue(DexValueType.TYPE);
        typeValue.setKey(enclosing);
        return typeValue;
    }
    public TypeValue getDalvikEnclosingClass(){
        AnnotationItem item = getDalvikEnclosingClassAnnotation();
        if(item == null){
            return null;
        }
        AnnotationElement element = item.getElement(Key.DALVIK_value);
        DexValueBlock<?> value = element.getValue();
        if(value instanceof TypeValue){
            return (TypeValue) value;
        }
        return null;
    }
    public AnnotationItem getDalvikEnclosingClassAnnotation(){
        AnnotationSet annotationSet = getClassAnnotations();
        if(annotationSet != null){
            return annotationSet.get(TypeKey.DALVIK_EnclosingClass);
        }
        return null;
    }
    public AnnotationSet getOrCreateClassAnnotations(){
        return getOrCreateAnnotationsDirectory().getOrCreateClassAnnotations();
    }
    public AnnotationItem getClassAnnotation(TypeKey typeKey){
        AnnotationSet classAnnotations = getClassAnnotations();
        if(classAnnotations != null){
            return classAnnotations.get(typeKey);
        }
        return null;
    }
    public Iterator<AnnotationItem> getClassAnnotations(TypeKey typeKey){
        AnnotationSet classAnnotations = getClassAnnotations();
        if(classAnnotations != null){
            return classAnnotations.getAll(typeKey);
        }
        return EmptyIterator.of();
    }
    public AnnotationSet getClassAnnotations(){
        AnnotationsDirectory annotationsDirectory = getAnnotationsDirectory();
        if(annotationsDirectory != null){
            return annotationsDirectory.getClassAnnotations();
        }
        return null;
    }
    public void setClassAnnotations(AnnotationSet annotationSet){
        AnnotationsDirectory annotationsDirectory = getAnnotationsDirectory();
        if(annotationsDirectory != null){
            annotationsDirectory.setClassAnnotations(annotationSet);
        }
    }
    public AnnotationsDirectory getOrCreateAnnotationsDirectory(){
        AnnotationsDirectory directory = annotationsDirectory.getOrCreate();
        directory.addUniqueUser(this);
        return directory;
    }
    public AnnotationsDirectory getUniqueAnnotationsDirectory(){
        return annotationsDirectory.getUniqueItem(this);
    }
    public AnnotationsDirectory getOrCreateUniqueAnnotationsDirectory(){
        return annotationsDirectory.getOrCreateUniqueItem(this);
    }
    public AnnotationsDirectory getAnnotationsDirectory(){
        return annotationsDirectory.getItem();
    }
    public void setAnnotationsDirectory(AnnotationsDirectory directory){
        this.annotationsDirectory.setItem(directory);
    }
    public ClassData getOrCreateClassData(){
        ClassData classData = getClassData();
        if(classData != null){
            return classData;
        }
        Section<ClassData> section = getSection(SectionType.CLASS_DATA);
        classData = section.createItem();
        setClassData(classData);
        return classData;
    }
    public ClassData getClassData(){
        ClassData data = classData.getItem();
        linkClassData(data);
        return data;
    }
    public void setClassData(ClassData classData){
        this.classData.setItem(classData);
        linkClassData(classData);
    }
    public EncodedArray getStaticValues(){
        return staticValues.getItem();
    }
    public EncodedArray getOrCreateUniqueStaticValues(){
        return staticValues.getOrCreateUniqueItem(this);
    }
    public EncodedArray getUniqueStaticValues(){
        return staticValues.getUniqueItem(this);
    }
    public DexValueBlock<?> getStaticValue(int i){
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray != null){
            return encodedArray.get(i);
        }
        return null;
    }
    public<T1 extends DexValueBlock<?>> T1 getOrCreateStaticValue(DexValueType<T1> valueType, int i){
        return getOrCreateUniqueStaticValues().getOrCreate(valueType, i);
    }
    public void setStaticValues(EncodedArray staticValues){
        this.staticValues.setItem(staticValues);
    }

    @Override
    public void refresh() {

        this.annotationsDirectory.addUniqueUser(this);
        this.classData.addUniqueUser(this);
        this.staticValues.addUniqueUser(this);

        this.classTypeId.refresh();
        this.superClassId.refresh();
        this.interfaces.refresh();
        this.sourceFile.refresh();
        this.annotationsDirectory.refresh();
        this.classData.refresh();
        this.staticValues.refresh();
    }
    @Override
    void cacheItems(){

        this.classTypeId.pullItem();
        this.superClassId.pullItem();
        this.interfaces.pullItem();
        this.sourceFile.pullItem();
        this.annotationsDirectory.pullItem();
        this.classData.pullItem();
        this.staticValues.pullItem();

        this.annotationsDirectory.addUniqueUser(this);
        this.classData.addUniqueUser(this);
        this.staticValues.addUniqueUser(this);

        linkClassData(this.classData.getItem());
    }
    private void linkClassData(ClassData classData){
        if(classData != null){
            classData.setClassId(this);
        }
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        this.classTypeId.unlink();
        this.superClassId.unlink();
        this.sourceFile.unlink();
        this.classData.unlink();
        this.annotationsDirectory.unlink();
        this.staticValues.unlink();
    }

    public void replaceKeys(Key search, Key replace){
        classTypeId.replaceKeys(search, replace);
        superClassId.replaceKeys(search, replace);
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            directory = getUniqueAnnotationsDirectory();
            directory.replaceKeys(search, replace);
        }
        interfaces.replaceKeys(search, replace);
        ClassData classData = getClassData();
        if(classData != null){
            classData.replaceKeys(search, replace);
        }
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return listUsedIds().iterator();
    }
    public ArrayCollection<IdItem> listUsedIds(){

        ArrayCollection<IdItem> collection = new ArrayCollection<>(200);
        collection.add(classTypeId.getItem());
        collection.add(superClassId.getItem());
        collection.add(sourceFile.getItem());
        collection.addAll(interfaces.iterator());
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            collection.addAll(directory.usedIds());
        }
        ClassData classData = getClassData();
        if(classData != null){
            collection.addAll(classData.usedIds());
        }
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray != null){
            collection.addAll(encodedArray.usedIds());
        }
        int size = collection.size();
        for (int i = 0; i < size; i++){
            IdItem idItem = collection.get(i);
            collection.addAll(idItem.usedIds());
        }
        return collection;
    }

    public void merge(ClassId classId){
        if(classId == this){
            return;
        }
        accessFlagValue.set(classId.accessFlagValue.get());
        superClassId.setItem(classId.superClassId.getKey());
        sourceFile.setItem(classId.sourceFile.getKey());
        interfaces.setItem(classId.interfaces.getKey());
        annotationsDirectory.setItem(classId.annotationsDirectory.getKey());
        EncodedArray comingArray = classId.getStaticValues();
        if(comingArray != null){
            EncodedArray encodedArray = staticValues.getOrCreate();
            encodedArray.merge(comingArray);
        }
        ClassData comingData = classId.getClassData();
        if(comingData != null){
            ClassData classData = getOrCreateClassData();
            classData.merge(comingData);
        }
    }
    public void fromSmali(SmaliClass smaliClass) throws IOException {

        setKey(smaliClass.getKey());
        setAccessFlagsValue(smaliClass.getAccessFlagsValue());
        setSuperClass(smaliClass.getSuperClass());
        setSourceFile(smaliClass.getSourceFileName());
        setInterfaces(smaliClass.getInterfacesKey());

        if(smaliClass.hasClassData()){
            getOrCreateClassData().fromSmali(smaliClass);
        }
        if(smaliClass.hasAnnotation()){
            getOrCreateClassAnnotations().fromSmali(smaliClass.getAnnotation());
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassTypeId().append(writer);
        getSuperClassId().append(writer);
        getSourceFile().append(writer);
        writer.newLine();
        TypeList interfaces = getInterfaceTypeList();
        if(interfaces != null){
            interfaces.appendInterfaces(writer);
        }
        AnnotationSet annotationSet = getClassAnnotations();
        if(annotationSet != null){
            writer.newLine();
            writer.newLine();
            writer.appendComment("annotations");
            annotationSet.append(writer);
            writer.newLine();
        }
        ClassData classData = getClassData();
        if(classData != null){
            classData.append(writer);
        }else {
            writer.appendComment("Null class data: " + this.classData.get());
        }
    }
    @Override
    public int compareTo(ClassId classId) {
        if(classId == null){
            return -1;
        }
        if(classId == this){
            return 0;
        }
        return SectionTool.compareIdx(getId(), classId.getId());
    }
    @Override
    public String toString(){
        if(isReading()){
            return ".class " + getKey();
        }
        return SmaliWriter.toStringSafe(this);
    }


    private static final int SIZE = 32;
}
