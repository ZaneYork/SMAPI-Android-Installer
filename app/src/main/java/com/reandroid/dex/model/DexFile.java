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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.Origin;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.data.CodeItem;
import com.reandroid.dex.data.DebugInfo;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.*;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.FileByteSource;
import com.reandroid.utils.io.FileIterator;
import com.reandroid.utils.io.FileUtil;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class DexFile implements DexClassRepository, Closeable,
        Iterable<DexClass>, FullRefresh {

    private final DexLayout dexLayout;
    private DexDirectory dexDirectory;
    private boolean closed;

    public DexFile(DexLayout dexLayout){
        this.dexLayout = dexLayout;
        dexLayout.setTag(this);
    }

    public int getVersion(){
        return getDexLayout().getVersion();
    }
    public void setVersion(int version){
        getDexLayout().setVersion(version);
    }
    public int shrink(){
        return getDexLayout().getSectionList().shrink();
    }
    public int clearDuplicateData(){
        return getDexLayout().getSectionList().clearDuplicateData();
    }
    public int clearUnused(){
        return getDexLayout().getSectionList().clearUnused();
    }
    public int clearEmptySections(){
        return getDexLayout().getSectionList().clearEmptySections();
    }
    public void fixDebugLineNumbers(){
        Section<CodeItem> section = getSection(SectionType.CODE);
        if(section == null){
            return;
        }
        for(CodeItem codeItem : section){
            DebugInfo debugInfo = codeItem.getDebugInfo();
            if(debugInfo == null){
                continue;
            }
            debugInfo.getDebugSequence().fixDebugLineNumbers();
        }
    }

    public DexClassRepository getClassRepository(){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory;
        }
        return this;
    }
    public DexDirectory getDexDirectory() {
        return dexDirectory;
    }
    public void setDexDirectory(DexDirectory dexDirectory) {
        this.dexDirectory = dexDirectory;
        DexLayout dexLayout = getDexLayout();
        dexLayout.setTag(this);
        dexLayout.setSimpleName(getSimpleName());
    }

    public Iterator<DexClass> getSubTypes(TypeKey typeKey){
        return ComputeIterator.of(getSubTypeIds(typeKey), this::create);
    }
    public Iterator<DexClass> getExtendingClasses(TypeKey typeKey){
        return ComputeIterator.of(getExtendingClassIds(typeKey), this::create);
    }
    public Iterator<DexClass> getImplementClasses(TypeKey typeKey){
        return ComputeIterator.of(getImplementationIds(typeKey), this::create);
    }
    public Iterator<ClassId> getSubTypeIds(TypeKey superClass){
        return getDexLayout().getSubTypes(superClass);
    }
    public Iterator<ClassId> getExtendingClassIds(TypeKey superClass){
        return getDexLayout().getExtendingClassIds(superClass);
    }
    public Iterator<ClassId> getImplementationIds(TypeKey interfaceClass){
        return getDexLayout().getImplementationIds(interfaceClass);
    }
    public DexClass getOrCreateClass(String type){
        return getOrCreateClass(new TypeKey(type));
    }
    public DexClass getOrCreateClass(TypeKey key){
        DexClass dexClass = search(key);
        if(dexClass != null){
            return dexClass;
        }
        ClassId classId = getOrCreateClassId(key);
        return create(classId);
    }
    public DexSource<DexFile> getSource(){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.getDexSourceSet().getSource(this);
        }
        return null;
    }
    public String getSimpleName() {
        return getDexLayout().getSimpleName();
    }
    public void setSimpleName(String simpleName){
        getDexLayout().setSimpleName(simpleName);
    }
    @Override
    public Iterator<DexClass> iterator() {
        return getDexClasses();
    }
    @Override
    public boolean removeClasses(Predicate<? super DexClass> filter){
        Predicate<ClassId> classIdFilter = classId -> filter.test(DexFile.this.create(classId));
        return getDexLayout().removeEntries(SectionType.CLASS_ID, classIdFilter);
    }
    @Override
    public <T1 extends SectionItem> boolean removeEntries(SectionType<T1> sectionType, Predicate<T1> filter){
        return getDexLayout().removeEntries(sectionType, filter);
    }
    @Override
    public <T1 extends SectionItem> boolean removeEntriesWithKey(SectionType<T1> sectionType, Predicate<? super Key> filter) {
        return getDexLayout().removeWithKeys(sectionType, filter);
    }
    @Override
    public <T1 extends SectionItem> boolean removeEntry(SectionType<T1> sectionType, Key key){
        return getDexLayout().removeWithKey(sectionType, key);
    }
    @Override
    public int getDexClassesCount() {
        Section<ClassId> section = getSection(SectionType.CLASS_ID);
        if(section != null){
            return section.getCount();
        }
        return 0;
    }
    @Override
    public DexClass getDexClass(TypeKey key){
        ClassId classId = getItem(SectionType.CLASS_ID, key);
        if(classId == null) {
            return null;
        }
        return create(classId);
    }
    @Override
    public Iterator<DexClass> getDexClasses(Predicate<? super TypeKey> filter) {
        return ComputeIterator.of(getClassIds(filter), this::create);
    }
    @Override
    public Iterator<DexClass> getDexClassesCloned(Predicate<? super TypeKey> filter) {
        return ComputeIterator.of(getClassIdsCloned(filter), this::create);
    }
    @Override
    public<T1 extends SectionItem> Iterator<Section<T1>> getSections(SectionType<T1> sectionType) {
        return SingleIterator.of(getSection(sectionType));
    }
    @Override
    public Iterator<DexClass> searchExtending(TypeKey typeKey){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.searchExtending(typeKey);
        }
        return getExtendingClasses(typeKey);
    }
    @Override
    public Iterator<DexClass> searchImplementations(TypeKey typeKey){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.searchImplementations(typeKey);
        }
        return getImplementClasses(typeKey);
    }
    public DexClass search(TypeKey typeKey){
        return getClassRepository().getDexClass(typeKey);
    }
    public ClassId getOrCreateClassId(TypeKey key){
        Section<ClassId> section = getDexLayout().get(SectionType.CLASS_ID);
        DexSectionPool<ClassId> pool = section.getPool();
        ClassId classId = pool.get(key);
        if(classId != null) {
            return classId;
        }
        classId = pool.getOrCreate(key);
        classId.getOrCreateClassData();
        classId.setSuperClass(TypeKey.OBJECT);
        classId.setSourceFile(DexUtils.toSourceFileName(key.getTypeName()));
        classId.addAccessFlag(AccessFlag.PUBLIC);
        return classId;
    }
    private DexClass create(ClassId classId) {
        return new DexClass(this, classId);
    }
    public Marker getOrCreateMarker() {
        Marker marker = CollectionUtil.getFirst(getMarkers());
        if(marker != null){
            return marker;
        }
        marker = Marker.createR8();
        Section<StringId> stringSection = getSection(SectionType.STRING_ID);

        StringId stringId = stringSection.createItem();
        marker.setStringId(stringId);

        marker.save();

        return marker;
    }
    public void addMarker(Marker marker) {
        StringId stringId = marker.getStringId();
        if(stringId == null){
            Section<StringId> stringSection = getSection(SectionType.STRING_ID);
            stringId = stringSection.createItem();
            marker.setStringId(stringId);
        }
        marker.save();
    }
    @Override
    public Iterator<Marker> getMarkers() {
        return getDexLayout().getMarkers();
    }
    @Override
    public void refreshFull() throws DexException {
        getDexLayout().refreshFull();
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getDexLayout().sortSection(order);
        refresh();
    }
    public void clearPoolMap(SectionType<?> sectionType){
        getDexLayout().clearPoolMap(sectionType);
    }
    @Override
    public void clearPoolMap(){
        getDexLayout().clearPoolMap();
    }
    public void sortStrings(){
        getDexLayout().sortStrings();
    }
    public Iterator<DexInstruction> getDexInstructions(){
        return new IterableIterator<DexClass, DexInstruction>(getDexClasses()){
            @Override
            public Iterator<DexInstruction> iterator(DexClass element) {
                return element.getDexInstructions();
            }
        };
    }
    public Iterator<DexInstruction> getDexInstructionsCloned(){
        return new IterableIterator<DexClass, DexInstruction>(getDexClassesCloned()){
            @Override
            public Iterator<DexInstruction> iterator(DexClass element) {
                return element.getDexInstructions();
            }
        };
    }
    public Iterator<ClassId> getClassIds(Predicate<? super TypeKey> filter){
        return FilterIterator.of(getItems(SectionType.CLASS_ID),
                classId -> filter == null || filter.test(classId.getKey()));
    }
    public Iterator<ClassId> getClassIdsCloned(Predicate<? super TypeKey> filter){
        return FilterIterator.of(getClonedItems(SectionType.CLASS_ID),
                classId -> filter == null || filter.test(classId.getKey()));
    }
    public Iterator<TypeId> getTypes(){
        return getItems(SectionType.TYPE_ID);
    }
    public <T1 extends SectionItem> Section<T1> getSection(SectionType<T1> sectionType){
        return getDexLayout().get(sectionType);
    }
    @Override
    public void refresh() {
        getDexLayout().refresh();
    }
    public DexLayout getDexLayout() {
        return dexLayout;
    }

    public boolean isEmpty(){
        return getDexLayout().isEmpty();
    }
    public int getIndex(){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.indexOf(this);
        }
        return -1;
    }
    public boolean merge(DexClass dexClass){
        return merge(new DexMergeOptions(true), dexClass);
    }
    public boolean merge(MergeOptions options, DexClass dexClass){
        return this.merge(options, dexClass.getId());
    }
    public boolean merge(ClassId classId){
        return merge(new DexMergeOptions(true), classId);
    }
    public boolean merge(MergeOptions options, ClassId classId){
        return getDexLayout().merge(options, classId);
    }
    public boolean merge(MergeOptions options, DexFile dexFile){
        if(dexFile == null || dexFile.isEmpty()){
            return false;
        }
        return getDexLayout().merge(options, dexFile.getDexLayout());
    }
    public void parseSmaliDirectory(File dir) throws IOException {
        requireNotClosed();
        if(!dir.isDirectory()){
            throw new FileNotFoundException("No such directory: " + dir);
        }
        FileIterator iterator = new FileIterator(dir, FileIterator.getExtensionFilter(".smali"));
        FileByteSource byteSource = new FileByteSource();
        SmaliReader reader = new SmaliReader(byteSource);
        DexLayout layout = getDexLayout();
        while (iterator.hasNext()) {
            reader.reset();
            File file = iterator.next();
            byteSource.setFile(file);
            reader.setOrigin(Origin.createNew(file));
            SmaliClass smaliClass = new SmaliClass();
            smaliClass.parse(reader);
            layout.fromSmali(smaliClass);
        }
        shrink();
    }
    public void parseSmaliFile(File file) throws IOException {
        requireNotClosed();
        fromSmali(SmaliReader.of(file));
    }
    public void fromSmaliAll(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        while (!reader.finished()){
            fromSmali(reader);
            reader.skipWhitespacesOrComment();
        }
    }
    public DexClass fromSmali(SmaliReader reader) throws IOException {
        requireNotClosed();
        SmaliClass smaliClass = new SmaliClass();
        smaliClass.parse(reader);
        DexClass dexClass = fromSmali(smaliClass);
        reader.skipWhitespacesOrComment();
        return dexClass;
    }
    public DexClass fromSmali(SmaliClass smaliClass) throws IOException {
        requireNotClosed();
        ClassId classId = getDexLayout().fromSmali(smaliClass);
        return create(classId);
    }

    public byte[] getBytes() {
        if(isClosed()){
            return null;
        }
        if(isEmpty()){
            return new byte[0];
        }
        return getDexLayout().getBytes();
    }
    public void write(File file) throws IOException {
        requireNotClosed();
        OutputStream outputStream = FileUtil.outputStream(file);;
        write(outputStream);
        outputStream.close();
    }
    public void write(OutputStream outputStream) throws IOException {
        requireNotClosed();
        byte[] bytes = getBytes();
        outputStream.write(bytes, 0, bytes.length);
    }

    public String printSectionInfo(){
        return getDexLayout().getMapList().toString();
    }
    public void writeSmali(SmaliWriter writer, File root) throws IOException {
        requireNotClosed();
        File dir = new File(root, buildSmaliDirectoryName());
        for(DexClass dexClass : this){
            dexClass.writeSmali(writer, dir);
        }
    }
    public String buildSmaliDirectoryName() {
        DexDirectory dexDirectory = getDexDirectory();
        if(dexDirectory == null) {
            String name = getSimpleName();
            if(name != null && name.endsWith(".dex")) {
                return name.substring(0, name.length() - 4);
            }
            return "classes";
        }
        int i = 0;
        for(DexFile dexFile : dexDirectory){
            if(dexFile == this){
                break;
            }
            i++;
        }
        if(i == 0){
            return "classes";
        }
        i++;
        return "classes" + i;
    }
    public String getFileName(){
        String simpleName = getSimpleName();
        if(simpleName == null){
            return buildSmaliDirectoryName() + ".dex";
        }
        return FileUtil.getFileName(simpleName);
    }

    private void requireNotClosed() throws IOException {
        if(isClosed()){
            throw new IOException("Closed");
        }
    }
    public boolean isClosed() {
        return closed;
    }
    @Override
    public void close() throws IOException {
        if(!closed){
            closed = true;
            getDexLayout().clear();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getSimpleName());
        builder.append(", version = ");
        builder.append(getVersion());
        builder.append(", classes = ");
        builder.append(getDexClassesCount());
        List<Marker> markers = CollectionUtil.toList(getMarkers());
        int size = markers.size();
        if(size != 0){
            builder.append(", markers = ");
            builder.append(size);
            if(size > 10){
                size = 10;
            }
            for(int i = 0; i < size; i++){
                builder.append('\n');
                builder.append(markers.get(i));
            }
        }
        return builder.toString();
    }

    public static DexFile read(byte[] dexBytes) throws IOException {
        return read(new BlockReader(dexBytes));
    }
    public static DexFile read(InputStream inputStream) throws IOException {
        return read(new BlockReader(inputStream));
    }
    public static DexFile read(File file) throws IOException {
        return read(new BlockReader(file));
    }
    public static DexFile read(BlockReader reader) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readBytes(reader);
        reader.close();
        return new DexFile(dexLayout);
    }
    public static DexFile readStrings(BlockReader reader) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readStrings(reader);
        return new DexFile(dexLayout);
    }
    public static DexFile readClassIds(BlockReader reader) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readClassIds(reader);
        return new DexFile(dexLayout);
    }
    public static DexFile readSections(BlockReader reader, Predicate<SectionType<?>> filter) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readSections(reader, filter);
        return new DexFile(dexLayout);
    }
    public static DexFile readStrings(InputStream inputStream) throws IOException {
        return readStrings(new BlockReader(inputStream));
    }
    public static DexFile readClassIds(InputStream inputStream) throws IOException {
        return readClassIds(new BlockReader(inputStream));
    }

    public static DexFile createDefault(){
        return new DexFile(DexLayout.createDefault());
    }

    public static DexFile findDexFile(ClassId classId){
        if(classId == null){
            return null;
        }
        return DexFile.findDexFile(classId.getParentInstance(DexLayout.class));
    }
    public static DexFile findDexFile(DexLayout dexLayout){
        if(dexLayout == null){
            return null;
        }
        Object obj = dexLayout.getTag();
        if(!(obj instanceof DexFile)){
            return null;
        }
        return  (DexFile) obj;
    }
    public static int getDexFileNumber(String name){
        int i = name.lastIndexOf('/');
        if(i < 0){
            i = name.lastIndexOf('\\');
        }
        if(i >= 0){
            name = name.substring(i + 1);
        }
        if(name.equals("classes.dex")){
            return 0;
        }
        String prefix = "classes";
        String ext = ".dex";
        if(!name.startsWith(prefix) || !name.endsWith(ext)){
            return -1;
        }
        String num = name.substring(prefix.length(), name.length() - ext.length());
        try {
            return Integer.parseInt(num);
        }catch (NumberFormatException ignored){
            return -1;
        }
    }

    public static String getDexName(int i) {
        if (i == 0) {
            return "classes.dex";
        }
        if(i == 1){
            i = 2;
        }
        return "classes" + i + ".dex";
    }
}
