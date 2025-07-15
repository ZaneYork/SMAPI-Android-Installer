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
package com.reandroid.dex.resource;

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.*;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.*;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

public class R implements Iterable<RTypeItem> {

    private final DexClass dexClass;
    private final ArrayCollection<RTypeItem> typeList;
    private boolean typeListLoaded;

    public R(DexClass dexClass) {
        this.dexClass = dexClass;
        this.typeList = new ArrayCollection<>();
    }

    @Override
    public Iterator<RTypeItem> iterator() {
        return getTypeList().iterator();
    }
    public Iterator<RStyleableType> getStyleables() {
        return InstanceIterator.of(getTypeList().iterator(), RStyleableType.class);
    }
    public Iterator<RDeclareStyleable> getRDeclareStyleables() {
        Iterator<RDeclareStyleable> iterator = new IterableIterator<RStyleableType, RDeclareStyleable>(
                getStyleables()) {
            @Override
            public Iterator<RDeclareStyleable> iterator(RStyleableType element) {
                return element.getRDeclareStyleables();
            }
        };
        return new UniqueIterator<>(iterator);
    }
    public int size() {
        return getTypeList().size();
    }
    public List<RTypeItem> getTypeList() {
        loadTypes();
        return typeList;
    }

    private void loadTypes() {
        if(typeListLoaded) {
            return;
        }
        typeListLoaded = true;
        ArrayCollection<RTypeItem> typeList = this.typeList;
        typeList.clear();
        DexClassRepository repository = getDexClass().getClassRepository();
        typeList.addAll(ComputeIterator.of(repository.getDexClasses(), this::createWithCheck));
    }
    RTypeItem createWithCheck(DexClass dexClass) {
        if(isChild(dexClass)) {
            RTypeItem type = createType(dexClass);
            if(type != null && type.isValid()) {
                return type;
            }
            type = createStyleableType(dexClass);
            if(type != null && type.isValid()) {
                return type;
            }
        }
        return null;
    }
    RType createType(DexClass dexClass) {
        if(dexClass != null) {
            return new RType(dexClass);
        }
        return null;
    }
    RStyleableType createStyleableType(DexClass dexClass) {
        if(dexClass != null) {
            return new RStyleableType(dexClass);
        }
        return null;
    }
    boolean isChild(DexClass child) {
        if(child == null ||
                child.isAbstract() ||
                child.isInterface() ||
                child.isEnum()) {
            return false;
        }
        TypeKey typeKey = getKey();
        TypeKey childKey = child.getKey();
        if(typeKey.equals(childKey)) {
            return false;
        }
        if(!typeKey.equalsPackage(childKey)) {
            return false;
        }
        if(containsOnMembers(typeKey)) {
            return true;
        }
        return typeKey.equals(childKey.getEnclosingClass());
    }
    private boolean containsOnMembers(TypeKey typeKey) {
        DexValueArray valueArray = getDalvikMemberClasses();
        if(valueArray != null) {
            return CollectionUtil.contains(valueArray.getKeys(), typeKey);
        }
        return false;
    }
    private DexValueArray getDalvikMemberClasses() {
        DexAnnotation annotation = getDexClass()
                .getAnnotation(TypeKey.DALVIK_MemberClass);
        if(annotation != null) {
            DexAnnotationElement element = annotation.get("value");
            if(element != null) {
                DexValue value = element.getValue();
                if(value instanceof DexValueArray) {
                    return (DexValueArray) value;
                }
            }
        }
        return null;
    }

    public String getName() {
        return getKey().getSimpleInnerName();
    }
    public TypeKey getKey() {
        return getDexClass().getKey();
    }
    public DexClass getDexClass() {
        return dexClass;
    }
    public void refresh() {
        this.typeListLoaded = false;
        loadTypes();
    }
    public boolean isValid() {
        DexClass dexClass = getDexClass();
        if(dexClass.isAbstract() ||
                dexClass.isInterface() ||
                dexClass.isEnum() ||
                dexClass.isSynthetic()) {
            return false;
        }
        if(dexClass.getDeclaredFields().hasNext()) {
            return false;
        }
        Iterator<DexMethod> declaredMethods = dexClass.getDeclaredMethods();
        while (declaredMethods.hasNext()) {
            DexMethod dexMethod = declaredMethods.next();
            if(!dexMethod.isConstructor()) {
                return false;
            }
        }
        return isTypesValid();
    }
    private boolean isTypesValid() {
        boolean types = false;
        for(RTypeItem type : this) {
            if(!type.isValid()) {
                return false;
            }
            types = true;
        }
        return types;
    }
    public String toJavaString() {
        StringWriter stringWriter = new StringWriter();
        SmaliWriter writer = new SmaliWriter();
        writer.setWriter(stringWriter);
        try {
            appendJava(writer);
            writer.close();
        } catch (IOException ignored) {
        }
        return stringWriter.toString();
    }
    public void appendJava(SmaliWriter writer) throws IOException {
        loadTypes();
        DexClass dexClass = getDexClass();
        writer.append("package ");
        writer.append(dexClass.getKey().getPackageSourceName());
        writer.append(';');
        writer.newLine();
        writer.newLine();
        writer.appendModifiers(dexClass.getAccessFlags());
        writer.append("class ");
        writer.append(getName());
        writer.append(" {");
        writer.indentPlus();
        for(RTypeItem typeItem : this) {
            writer.newLine();
            typeItem.appendJava(writer);
        }
        writer.indentMinus();
        writer.newLine();
        writer.append('}');
    }
    public String toXml(TableBlock tableBlock) throws IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = XMLFactory.newSerializer(writer);
        serialize(tableBlock, serializer);
        serializer.flush();
        writer.close();
        return writer.toString();
    }
    public void serialize(TableBlock tableBlock, XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf8", null);
        XmlHelper.setIndent(serializer, true);
        String tag = "resources";
        serializer.startTag(null, tag);
        Iterator<RDeclareStyleable> iterator = getRDeclareStyleables();
        while (iterator.hasNext()) {
            RDeclareStyleable attributes = iterator.next();
            attributes.serialize(tableBlock, serializer);
        }
        serializer.endTag(null, tag);
        serializer.endDocument();
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof R)) {
            return false;
        }
        R other = (R) obj;
        return getKey().equals(other.getKey());
    }
    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean loaded = this.typeListLoaded;
        if(!loaded) {
            builder.append("NOT-LOADED: ");
        }
        builder.append(getKey());
        if(loaded) {
            builder.append(", size = ");
            builder.append(size());
        }
        return builder.toString();
    }
    static R createValid(DexClass dexClass) {
        R r = create(dexClass);
        if(r != null && r.isValid()) {
            return r;
        }
        return null;
    }
    static R create(DexClass dexClass) {
        if(dexClass != null) {
            return new R(dexClass);
        }
        return null;
    }

    public static Iterator<R> findAll(DexClassRepository repository) {
        return findAll(repository.getDexClasses());
    }
    public static Iterator<R> findAll(Iterator<? extends DexClass> iterator) {
        return ComputeIterator.of(iterator, R::createValid);
    }
}
