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
package com.reandroid.dex.model;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.data.ClassData;
import com.reandroid.dex.data.FieldDef;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RClass extends DexClass {

    public RClass(DexFile dexFile, ClassId classId) {
        super(dexFile, classId);
    }

    public RField load(ResourceEntry resourceEntry){
        if(resourceEntry.isEmpty()){
            return null;
        }
        String name = RField.sanitizeResourceName(resourceEntry.getName());
        FieldKey fieldKey = new FieldKey(getKey().getTypeName(), name, TypeKey.TYPE_I.getTypeName());
        RField rField = getOrCreateStaticField(fieldKey);
        rField.setResourceId(resourceEntry.getResourceId());
        return rField;
    }
    public String toJavaDeclare() {
        return toJavaDeclare(true);
    }
    public String toJavaDeclare(boolean makeFieldsFinal) {
        StringBuilder builder = new StringBuilder();
        builder.append("    public static class ");
        builder.append(getResourceType());
        String indent = "\n        ";
        builder.append(" {");
        Iterator<DexField> iterator = getStaticFields();
        while (iterator.hasNext()){
            builder.append(indent);
            RField rField = (RField) iterator.next();
            builder.append(rField.toJavaDeclare(makeFieldsFinal));
        }
        builder.append('\n');
        builder.append("    }");
        return builder.toString();
    }
    public String getResourceType(){
        return toResourceTypeName(getDefining().getTypeName());
    }

    @Override
    public RField getOrCreateStaticField(FieldKey fieldKey){
        return new RField(this, getOrCreateStatic(fieldKey));
    }
    @Override
    public Iterator<DexField> getStaticFields() {
        ClassData classData = getClassData();
        if(classData != null){
            return ComputeIterator.of(classData
                    .getStaticFields(), this::createRField);
        }
        return EmptyIterator.of();
    }
    private RField createRField(FieldDef fieldDef){
        if(fieldDef.isStatic()){
            if(RField.isResourceIdValue(fieldDef.getStaticInitialValue())){
                return new RField(this, fieldDef);
            }
        }
        return null;
    }
    public void initialize(){
        ClassId classId = getId();
        classId.setSuperClass(TypeKey.OBJECT);
        classId.setSourceFile("R.java");


        initializeAnnotations();

    }
    private void initializeAnnotations(){
        ClassId classId = getId();
        classId.getOrCreateDalvikEnclosingClass();
        classId.getOrCreateDalvikInnerClass();
    }

    @Override
    public String toString() {
        return toJavaDeclare();
    }

    static boolean isRClassName(ClassId classId) {
        if(classId != null){
            return isRClassName(classId.getName());
        }
        return false;
    }
    static boolean isRClassName(String name) {
        if(name == null){
            return false;
        }
        return toResourceTypeName(name) != null;
    }
    private static String toResourceTypeName(String className) {
        String simpleName = DexUtils.getSimpleName(className);
        String prefix = SIMPLE_NAME_PREFIX;
        if(simpleName.length() < 4 || !simpleName.startsWith(prefix)){
            return null;
        }
        simpleName = simpleName.substring(prefix.length());
        if(simpleName.indexOf('$') >= 0){
            return null;
        }
        return simpleName;
    }
    public static void serializePublicXml(Collection<RField> rFields, XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.text("\n");
        serializer.startTag(null, PackageBlock.TAG_resources);

        List<RField> fieldList = new ArrayCollection<>();
        fieldList.addAll(rFields);
        fieldList.sort(CompareUtil.getComparableComparator());
        for(RField rField : fieldList) {
            rField.serializePublicXml(serializer);
        }

        serializer.text("\n");
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
        serializer.flush();
        IOUtil.close(serializer);
    }

    private static final String SIMPLE_NAME_PREFIX = "R$";

    static final TableBlock EMPTY_TABLE = new TableBlock();
}
