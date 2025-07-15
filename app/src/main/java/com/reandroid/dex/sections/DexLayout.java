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
package com.reandroid.dex.sections;

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.BytesOutputStream;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.header.Checksum;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.Predicate;

public class DexLayout extends FixedBlockContainer implements FullRefresh {

    private final SectionList sectionList;

    private String mSimpleName;

    private final MultiMap<TypeKey, ClassId> extendingClassMap;
    private final MultiMap<TypeKey, ClassId> interfaceMap;

    private Object mTag;

    public DexLayout() {
        super(1);
        this.sectionList = new SectionList();
        addChild(0, sectionList);

        this.extendingClassMap = new MultiMap<>();
        this.interfaceMap = new MultiMap<>();
    }

    public int getVersion(){
        return getHeader().getVersion();
    }
    public void setVersion(int version){
        getHeader().setVersion(version);
    }
    public Iterator<Marker> getMarkers(){
        return Marker.parse(get(SectionType.STRING_ID));
    }

    public Iterator<ClassId> getSubTypes(TypeKey typeKey){
        Iterator<ClassId> iterator = CombiningIterator.two(getExtendingClassIds(typeKey),
                getImplementationIds(typeKey));
        return new IterableIterator<ClassId, ClassId>(iterator) {
            @Override
            public Iterator<ClassId> iterator(ClassId element) {
                return CombiningIterator.two(SingleIterator.of(element), getSubTypes(element.getKey()));
            }
        };
    }
    public Iterator<ClassId> getExtendingClassIds(TypeKey typeKey){
        if(extendingClassMap.size() == 0){
            loadExtendingClassMap();
        }
        return this.extendingClassMap.getAll(typeKey);
    }
    public Iterator<ClassId> getImplementationIds(TypeKey interfaceClass){
        if(interfaceMap.size() == 0){
            loadInterfacesMap();
        }
        return this.interfaceMap.getAll(interfaceClass);
    }

    public void clear(){
        extendingClassMap.clear();
        interfaceMap.clear();
        getSectionList().clear();
    }
    private void loadExtendingClassMap(){
        MultiMap<TypeKey, ClassId> superClassMap = this.extendingClassMap;
        superClassMap.clear();
        Section<ClassId> section = getSectionList().getSection(SectionType.CLASS_ID);
        if(section == null) {
            return;
        }
        superClassMap.setInitialSize(section.getCount());
        for (ClassId classId : section) {
            TypeKey typeKey = classId.getSuperClassKey();
            if(!DexUtils.isJavaFramework(typeKey.getTypeName())){
                superClassMap.put(typeKey, classId);
            }
        }
    }
    private void loadInterfacesMap(){
        MultiMap<TypeKey, ClassId> interfaceMap = this.interfaceMap;
        interfaceMap.clear();
        Section<ClassId> section = getSectionList().getSection(SectionType.CLASS_ID);
        if(section == null) {
            return;
        }
        for (ClassId classId : section) {
            Iterator<TypeKey> interfaceKeys = classId.getInterfaceKeys();
            while (interfaceKeys.hasNext()){
                TypeKey typeKey = interfaceKeys.next();
                if(!DexUtils.isJavaFramework(typeKey.getTypeName())) {
                    interfaceMap.put(typeKey, classId);
                }
            }
        }
    }
    public Iterator<StringId> getStrings(){
        return getItems(SectionType.STRING_ID);
    }
    public<T1 extends SectionItem> Iterator<T1> getClonedItems(SectionType<T1> sectionType) {
        Section<T1> section = getSectionList().getSection(sectionType);
        if(section != null){
            return section.clonedIterator();
        }
        return EmptyIterator.of();
    }
    public<T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType) {
        Section<T1> section = getSectionList().getSection(sectionType);
        if(section != null){
            return section.iterator();
        }
        return EmptyIterator.of();
    }
    @Override
    public void refreshFull() throws DexException{
        getSectionList().refreshFull();
        Checksum checksum = getHeader().checksum;
        int previousSum = checksum.getValue();
        int max_trials = 10;
        int trials;
        for(trials = 0; trials < max_trials; trials++){
            refresh();
            int sum = checksum.getValue();
            if(previousSum == sum){
                return;
            }
            previousSum = sum;
        }
        throw new DexException("Failed to refresh trials = " + trials);
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getSectionList().sortSection(order);
        refresh();
    }
    public void clearPoolMap(SectionType<?> sectionType){
        getSectionList().clearPoolMap(sectionType);
    }
    public void clearPoolMap(){
        extendingClassMap.clear();
        interfaceMap.clear();
        getSectionList().clearPoolMap();
    }
    public void sortStrings(){
        getSectionList().sortStrings();
    }

    public <T1 extends SectionItem> Iterator<T1> getAll(SectionType<T1> sectionType, Key key){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.getAll(key);
        }
        return EmptyIterator.of();
    }
    public <T1 extends SectionItem> boolean removeEntries(SectionType<T1> sectionType, Predicate<T1> filter){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.removeEntries(filter);
        }
        return false;
    }
    public <T1 extends SectionItem> boolean removeWithKeys(SectionType<T1> sectionType, Predicate<? super Key> filter){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.removeWithKeys(filter);
        }
        return false;
    }
    public <T1 extends SectionItem> boolean removeWithKey(SectionType<T1> sectionType, Key key){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.remove(key);
        }
        return false;
    }
    public <T1 extends SectionItem> T1 get(SectionType<T1> sectionType, Key key){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.getSectionItem(key);
        }
        return null;
    }
    public<T1 extends SectionItem> Section<T1> get(SectionType<T1> sectionType){
        return getSectionList().getSection(sectionType);
    }
    public DexHeader getHeader() {
        return getSectionList().getHeader();
    }
    public SectionList getSectionList(){
        return sectionList;
    }
    public MapList getMapList(){
        return getSectionList().getMapList();
    }

    @Override
    protected void onPreRefresh() {
        sectionList.refresh();
        interfaceMap.clear();
        extendingClassMap.clear();
    }
    @Override
    protected void onRefreshed() {
        sectionList.updateHeader();
    }
    public boolean isEmpty(){
        Section<ClassId> section = get(SectionType.CLASS_ID);
        return section == null || section.getCount() == 0;
    }
    public boolean merge(MergeOptions options, ClassId classId){
        return getSectionList().merge(options, classId);
    }
    public boolean merge(MergeOptions options, DexLayout dexFile){
        if(dexFile == this){
            options.onMergeError(this, getSectionList(), "Can not merge dex file to self");
            return false;
        }
        return getSectionList().merge(options, dexFile.getSectionList());
    }
    public ClassId fromSmali(SmaliClass smaliClass) throws IOException {
        return getSectionList().fromSmali(smaliClass);
    }
    @Override
    public byte[] getBytes(){
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeader().fileSize.get());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
    }

    public void read(byte[] dexBytes) throws IOException {
        BlockReader reader = new BlockReader(dexBytes);
        readBytes(reader);
        reader.close();
    }
    public void read(InputStream inputStream) throws IOException {
        BlockReader reader;
        if(inputStream instanceof BlockReader){
            reader = (BlockReader) inputStream;
        }else {
            reader = new BlockReader(inputStream);
        }
        readBytes(reader);
        reader.close();
    }
    public void readStrings(InputStream inputStream) throws IOException {
        BlockReader reader;
        if(inputStream instanceof BlockReader){
            reader = (BlockReader) inputStream;
        }else {
            reader = new BlockReader(inputStream);
        }
        readStrings(reader);
    }
    public void readStrings(BlockReader reader) throws IOException {
        readSections(reader, sectionType ->
                sectionType == SectionType.STRING_ID ||
                        sectionType == SectionType.STRING_DATA);
    }
    public void readClassIds(BlockReader reader) throws IOException {
        readSections(reader, sectionType ->
                sectionType == SectionType.STRING_ID ||
                        sectionType == SectionType.STRING_DATA||
                        sectionType == SectionType.TYPE_ID||
                        sectionType == SectionType.CLASS_ID);
    }
    public void readSections(BlockReader reader, Predicate<SectionType<?>> filter) throws IOException {
        getSectionList().readSections(reader, filter);
        reader.close();
    }
    public void read(File file) throws IOException {
        BlockReader reader = new BlockReader(file);
        readBytes(reader);
        reader.close();
    }
    public void write(File file) throws IOException {
        OutputStream outputStream = FileUtil.outputStream(file);
        writeBytes(outputStream);
        outputStream.close();
    }


    public Object getTag() {
        return mTag;
    }
    public void setTag(Object tag) {
        this.mTag = tag;
    }
    public String getSimpleName() {
        return mSimpleName;
    }
    public void setSimpleName(String simpleName) {
        this.mSimpleName = simpleName;
    }

    public static DexLayout createDefault(){
        DexLayout dexLayout = new DexLayout();
        SectionList sectionList = dexLayout.getSectionList();
        MapList mapList = sectionList.getMapList();
        mapList.getOrCreate(SectionType.HEADER);
        mapList.getOrCreate(SectionType.MAP_LIST);
        SectionType<?>[] commonTypes = SectionType.getR8Order();
        for(SectionType<?> sectionType : commonTypes){
            sectionList.getOrCreateSection(sectionType);
        }

        sectionList.getMapList().linkHeader(sectionList.getHeader());

        return dexLayout;
    }
    public static boolean isDexFile(File file){
        if(file == null || !file.isFile()){
            return false;
        }
        DexHeader dexHeader = null;
        try {
            InputStream inputStream = FileUtil.inputStream(file);
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    public static boolean isDexFile(InputStream inputStream){
        DexHeader dexHeader = null;
        try {
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    private static boolean isDexFile(DexHeader dexHeader){
        if(dexHeader == null){
            return false;
        }
        if(dexHeader.magic.isDefault()){
            return false;
        }
        int version = dexHeader.getVersion();
        return version > 0 && version < 1000;
    }
}
