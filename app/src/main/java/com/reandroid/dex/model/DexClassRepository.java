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

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.data.DebugInfo;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.id.SourceFile;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.Marker;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.*;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public interface DexClassRepository extends FullRefresh, BlockRefresh {

    int getDexClassesCount();
    DexClass getDexClass(TypeKey typeKey);
    default DexClass getDexClass(String name) {
        return getDexClass(TypeKey.create(name));
    }
    Iterator<DexClass> getDexClasses(Predicate<? super TypeKey> filter);
    Iterator<DexClass> getDexClassesCloned(Predicate<? super TypeKey> filter);
    int shrink();
    default Iterator<DexClass> searchExtending(TypeKey typeKey){
        return EmptyIterator.of();
    }
    default Iterator<DexClass> searchImplementations(TypeKey typeKey){
        return EmptyIterator.of();
    }
    <T extends SectionItem> Iterator<Section<T>> getSections(SectionType<T> sectionType);
    default <T extends SectionItem> Iterator<T> getItems(SectionType<T> sectionType) {
        return new IterableIterator<Section<T>, T>(getSections(sectionType)) {
            @Override
            public Iterator<T> iterator(Section<T> element) {
                return element.iterator();
            }
        };
    }
    default <T extends SectionItem> Iterator<T> getClonedItems(SectionType<T> sectionType){
        return new IterableIterator<Section<T>, T>(getSections(sectionType)) {
            @Override
            public Iterator<T> iterator(Section<T> element) {
                return element.clonedIterator();
            }
        };
    }
    default <T extends SectionItem> Iterator<T> getItems(SectionType<T> sectionType, Key key) {
        return new IterableIterator<Section<T>, T>(getSections(sectionType)) {
            @Override
            public Iterator<T> iterator(Section<T> element) {
                return element.getAll(key);
            }
        };
    }
    default <T extends SectionItem> T getItem(SectionType<T> sectionType, Key key){
        Iterator<Section<T>> iterator = getSections(sectionType);
        while (iterator.hasNext()){
            Section<T> section = iterator.next();
            T item = section.get(key);
            if(item != null){
                return item;
            }
        }
        return null;
    }
    default boolean contains(SectionType<?> sectionType, Key key){
        return getItems(sectionType, key).hasNext();
    }
    default boolean contains(Key key){
        if(key == null){
            return false;
        }
        if(key instanceof StringKey){
            return contains(SectionType.STRING_ID, key);
        }
        if(key instanceof TypeKey){
            return contains(SectionType.TYPE_ID, key);
        }
        if(key instanceof FieldKey){
            return contains(SectionType.FIELD_ID, key);
        }
        if(key instanceof ProtoKey){
            return contains(SectionType.PROTO_ID, key);
        }
        if(key instanceof MethodKey){
            return contains(SectionType.METHOD_ID, key);
        }
        if(key instanceof TypeListKey){
            return contains(SectionType.TYPE_LIST, key);
        }
        throw new IllegalArgumentException("Unknown key type: " + key.getClass() + ", '" + key + "'");
    }
    default boolean containsClass(TypeKey key){
        return contains(SectionType.CLASS_ID, key);
    }
    <T1 extends SectionItem> boolean removeEntries(SectionType<T1> sectionType, Predicate<T1> filter);
    <T1 extends SectionItem> boolean removeEntriesWithKey(SectionType<T1> sectionType, Predicate<? super Key> filter);
    <T1 extends SectionItem> boolean removeEntry(SectionType<T1> sectionType, Key key);
    void clearPoolMap();

    default <T extends SectionItem> Iterator<T> getClonedItems(SectionType<T> sectionType, Predicate<? super T> filter) {
        return FilterIterator.of(getClonedItems(sectionType), filter);
    }

    default Iterator<DexClass> findUserClasses(Key key){
        return new UniqueIterator<>(getDexClasses(),
                dexClass -> dexClass.uses(key));
    }
    default Iterator<DexClass> getDexClasses(){
        return getDexClasses(null);
    }
    default Iterator<DexClass> getDexClassesCloned(){
        return getDexClassesCloned(null);
    }
    default Iterator<DexClass> getPackageClasses(String packageName){
        return getPackageClasses(packageName, true);
    }
    default Iterator<DexClass> getPackageClasses(String packageName, boolean includeSubPackages){
        return getDexClasses(key -> key.isPackage(packageName, includeSubPackages));
    }
    default DexMethod getDeclaredMethod(MethodKey methodKey){
        DexClass dexClass = getDexClass(methodKey.getDeclaring());
        if(dexClass != null){
            DexMethod dexMethod = dexClass.getDeclaredMethod(methodKey, false);
            if(dexMethod == null) {
                dexMethod = dexClass.getDeclaredMethod(methodKey, true);
            }
            return dexMethod;
        }
        return null;
    }
    default DexMethod getDeclaredMethod(MethodKey methodKey, boolean ignoreReturnType){
        DexClass dexClass = getDexClass(methodKey.getDeclaring());
        if(dexClass != null){
            return dexClass.getDeclaredMethod(methodKey, ignoreReturnType);
        }
        return null;
    }
    default DexField getDeclaredField(FieldKey fieldKey){
        DexClass dexClass = getDexClass(fieldKey.getDeclaring());
        if(dexClass != null){
            return dexClass.getDeclaredField(fieldKey);
        }
        return null;
    }
    default DexDeclaration getDexDeclaration(Key key){
        if(key instanceof TypeKey){
            return getDexClass((TypeKey) key);
        }
        if(key instanceof MethodKey){
            return getDeclaredMethod((MethodKey) key);
        }
        if(key instanceof FieldKey){
            return getDeclaredField((FieldKey) key);
        }
        return null;
    }
    default Iterator<DexMethod> getDeclaredMethods(){
        return new IterableIterator<DexClass, DexMethod>(getDexClasses()) {
            @Override
            public Iterator<DexMethod> iterator(DexClass dexClass) {
                return dexClass.getDeclaredMethods();
            }
        };
    }
    default Iterator<DexField> getDeclaredFields(){
        return new IterableIterator<DexClass, DexField>(getDexClasses()) {
            @Override
            public Iterator<DexField> iterator(DexClass dexClass) {
                return dexClass.getDeclaredFields();
            }
        };
    }
    default Iterator<IntegerReference> visitIntegers(){
        return new DexIntegerVisitor(this);
    }

    default boolean removeClass(TypeKey typeKey) {
        return removeEntry(SectionType.CLASS_ID, typeKey);
    }
    boolean removeClasses(Predicate<? super DexClass> filter);
    default boolean removeClassesWithKeys(Predicate<? super TypeKey> filter) {
        return removeEntriesWithKey(SectionType.CLASS_ID, ObjectsUtil.cast(filter));
    }
    default boolean removeAnnotations(TypeKey typeKey) {
        return removeEntries(SectionType.ANNOTATION_ITEM,
                annotationItem -> typeKey.equals(annotationItem.getTypeKey()));
    }
    default int removeAnnotationElements(MethodKey methodKey) {
        int removeCount = 0;

        TypeKey typeKey = methodKey.getDeclaring();
        Predicate<AnnotationElement> elementFilter = element -> element.is(methodKey);

        Iterator<AnnotationItem> iterator = getClonedItems(SectionType.ANNOTATION_ITEM);
        while (iterator.hasNext()) {
            AnnotationItem annotationItem = iterator.next();
            if(typeKey.equals(methodKey.getDeclaring())) {
                if(annotationItem.removeIf(elementFilter)){
                    if(annotationItem.isEmpty()) {
                        annotationItem.removeSelf();
                    }
                    removeCount ++;
                }
            }
        }
        return removeCount;
    }
    default void clearDebug() {
        Iterator<Section<DebugInfo>> iterator = getSections(SectionType.DEBUG_INFO);
        while (iterator.hasNext()) {
            iterator.next().removeSelf();
        }
    }

    default List<TypeKeyReference> getExternalTypeKeyReferenceList() {
        return ArrayCollection.empty();
    }

    default Iterator<MethodKey> findEquivalentMethods(MethodKey methodKey){
        DexClass defining = getDexClass(methodKey.getDeclaring());
        if(defining == null){
            return EmptyIterator.of();
        }
        Iterator<DexMethod> iterator = defining.getMethods(methodKey);
        return new IterableIterator<DexMethod, MethodKey>(iterator) {
            @Override
            public Iterator<MethodKey> iterator(DexMethod element) {
                element = element.getDeclared();
                MethodKey definingKey = element.getKey();
                return CombiningIterator.two(SingleIterator.of(definingKey),
                        element.getOverridingKeys());
            }
        };
    }
    default Iterator<DexMethod> getMethods(MethodKey methodKey) {
        return ComputeIterator.of(findEquivalentMethods(methodKey), this::getDeclaredMethod);
    }
    default Iterator<MethodId> getMethodIds(MethodKey methodKey){
        return new IterableIterator<MethodKey, MethodId>(findEquivalentMethods(methodKey)) {
            @Override
            public Iterator<MethodId> iterator(MethodKey element) {
                return getItems(SectionType.METHOD_ID, element);
            }
        };
    }

    Iterator<Marker> getMarkers();
    default void clearMarkers(){
        Iterator<Marker> iterator = getMarkers();
        while (iterator.hasNext()) {
            iterator.next().removeSelf();
        }
    }
    default void setClassSourceFileAll(){
        setClassSourceFileAll(SourceFile.SourceFile);
    }
    default void setClassSourceFileAll(String sourceFile){
        Iterator<ClassId> iterator = getItems(SectionType.CLASS_ID);
        while (iterator.hasNext()){
            ClassId classId = iterator.next();
            classId.setSourceFile(sourceFile);
        }
    }
}
