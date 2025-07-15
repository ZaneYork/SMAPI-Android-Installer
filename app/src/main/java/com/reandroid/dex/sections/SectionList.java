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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberIntegerReference;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.base.BlockListArray;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class SectionList extends FixedBlockContainer
        implements SectionTool, OffsetSupplier, Iterable<Section<?>> ,
        ArraySupplier<Section<?>>, FullRefresh {

    private final IntegerReference baseOffset;
    private final DexHeader dexHeader;
    private final Section<DexHeader> dexHeaderSection;
    private final BlockList<IdSection<?>> idSectionList;
    private final BlockList<DataSection<?>> dataSectionList;
    private final Section<MapList> mapListSection;
    private final Map<SectionType<?>, Section<?>> typeMap;
    private final MapList mapList;

    private boolean mReading;

    public SectionList() {
        super(4);

        this.baseOffset = new NumberIntegerReference();
        this.idSectionList = new BlockList<>();
        this.dataSectionList = new BlockList<>();

        Section<DexHeader> dexHeaderSection = SectionType
                .HEADER.createSpecialSection(baseOffset);
        DexHeader dexHeader = new DexHeader(baseOffset);
        dexHeaderSection.add(dexHeader);

        this.dexHeaderSection = dexHeaderSection;

        Section<MapList> mapListSection = SectionType.MAP_LIST.createSpecialSection(dexHeader.map);
        MapList mapList = new MapList(dexHeader.map);
        mapListSection.add(mapList);
        this.mapListSection = mapListSection;

        this.typeMap = new HashMap<>();

        addChild(0, dexHeaderSection);
        addChild(1, idSectionList);
        addChild(2, dataSectionList);
        addChild(3, mapListSection);

        this.dexHeader = dexHeader;
        this.mapList = mapList;

        typeMap.put(SectionType.HEADER, dexHeaderSection);
        typeMap.put(SectionType.MAP_LIST, mapListSection);
    }

    public int shrink(){
        int result = 0;
        while (true) {
            int count = clearUnused();
            if(count == 0){
                break;
            }
            result += count;
        }
        while (true) {
            int count = clearDuplicateData();
            if(count == 0){
                break;
            }
            result += count;
            result += clearUnused();
        }
        result += clearEmptySections();
        return result;
    }
    public int clearDuplicateData(){
        refresh();
        int result = 0;
        SectionType<?>[] remove = SectionType.getRemoveOrderList();
        for (SectionType<?> sectionType : remove) {
            Section<?> section = getSection(sectionType);
            if(section == null){
                continue;
            }
            int count = section.getPool().clearDuplicates();
            result += count;
            if(count == 0){
                continue;
            }
            section.refresh();
        }
        if(result != 0){
            refresh();
        }
        return result;
    }
    public int clearUnused() {
        clearUsageTypes();
        refresh();
        int result = 0;
        SectionType<?>[] remove = SectionType.getRemoveOrderList();
        for (SectionType<?> sectionType : remove) {
            Section<?> section = getSection(sectionType);
            if(section != null){
                result += section.clearUnused();
            }
        }
        return result;
    }
    public int clearEmptySections(){
        int result = 0;
        List<Section<?>> sections = CollectionUtil.toList(getSections());
        for (Section<?> section : sections) {
            if(section.removeIfEmpty()){
                result ++;
            }
        }
        return result;
    }
    private void clearUsageTypes(){
        Iterator<Section<?>> iterator = getSections();
        while (iterator.hasNext()) {
            iterator.next().clearUsageTypes();
        }
    }
    public void updateHeader() {
        Block parent = getParentInstance(DexLayout.class);
        if(parent == null){
            parent = this;
        }
        dexHeader.updateHeaderInternal(parent);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        mapList.refresh();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        readSections(reader, null);
    }
    void readSections(BlockReader reader, Predicate<SectionType<?>> filter) throws IOException {
        mReading = true;
        readSpecialSections(reader);
        readBody(reader, filter);
        mReading = false;
    }
    private void readSpecialSections(BlockReader reader) throws IOException {
        getSection(SectionType.HEADER).readBytes(reader);
        getSection(SectionType.MAP_LIST).readBytes(reader);
    }
    private void readBody(BlockReader reader, Predicate<SectionType<?>> filter) throws IOException {
        MapItem[] mapItemList = mapList.getReadSorted();
        int length = mapItemList.length;
        for(int i = 0; i < length; i++){
            MapItem mapItem = mapItemList[i];
            if(mapItem == null){
                continue;
            }
            if(filter != null && !filter.test(mapItem.getSectionType())){
                continue;
            }
            loadSection(mapItem, reader);
        }

        idSectionList.trimToSize();
        dataSectionList.trimToSize();

        idSectionList.sort(getOffsetComparator());
        dataSectionList.sort(getOffsetComparator());
        mapList.linkHeader(dexHeader);
    }
    private void loadSection(MapItem mapItem, BlockReader reader) throws IOException {
        if(mapItem == null){
            return;
        }
        SectionType<?> sectionType = mapItem.getSectionType();
        if(typeMap.containsKey(sectionType)){
            return;
        }
        Section<?> section = mapItem.createNewSection();
        if(section == null){
            return;
        }
        add(section);
        section.readBytes(reader);
    }
    @Override
    public boolean isReading(){
        return mReading;
    }
    public<T1 extends SectionItem> Section<T1> add(Section<T1> section){
        if(section instanceof IdSection){
            idSectionList.add((IdSection<?>) section);
        }else {
            dataSectionList.add((DataSection<?>) section);
        }
        typeMap.put(section.getSectionType(), section);
        return section;
    }
    public DexHeader getHeader() {
        return dexHeader;
    }
    public MapList getMapList() {
        return mapList;
    }

    public void remove(Section<?> section){
        if(section == null){
            return;
        }
        SectionType<?> sectionType = section.getSectionType();
        if(typeMap.remove(sectionType) != section){
            return;
        }
        section.onRemove(this);
        if(sectionType.isDataSection()){
            dataSectionList.remove((DataSection<?>) section);
        }else if(sectionType.isIdSection()){
            idSectionList.remove((IdSection<?>) section);
        }
    }
    public void clear(){
        Iterator<Section<?>> iterator = getSections();
        while (iterator.hasNext()){
            Section<?> section = iterator.next();
            section.onRemove(this);
        }
        idSectionList.clearChildes();
        dataSectionList.clearChildes();
        typeMap.clear();
    }
    public void clearPoolMap(SectionType<?> sectionType){
        Section<?> section = getSection(sectionType);
        if(section != null){
            section.clearPoolMap();
        }
    }
    public void clearPoolMap(){
        for(Section<?> section : this){
            section.clearPoolMap();
        }
    }
    public void sortSection(SectionType<?>[] order){
        //WARN: DO NOT CALL refresh() HERE
        idSectionList.sort(SectionType.comparator(order, Section::getSectionType));
        dataSectionList.sort(SectionType.comparator(order, Section::getSectionType));
        mapList.sortMapItems(order);
    }
    @Override
    public void refreshFull(){
        SectionType<?>[] sortOrder = SectionType.getSortSectionsOrder();
        for(SectionType<?> sectionType : sortOrder){
            Section<?> section = getSection(sectionType);
            if(section != null){
                section.refreshFull();
            }
        }
        clearUnused();
        clearDuplicateData();
    }
    public boolean sortStrings(){
        boolean result = false;
        Section<StringData> stringDataSection = getSection(SectionType.STRING_DATA);
        if(stringDataSection != null){
            result = stringDataSection.sort();
        }
        if(sortItems(SectionType.STRING_ID)){
            result = true;
        }
        if(sortItems(SectionType.TYPE_ID)){
            result = true;
        }
        if(sortItems(SectionType.PROTO_ID)){
            result = true;
        }
        if(sortItems(SectionType.FIELD_ID)){
            result = true;
        }
        if(sortItems(SectionType.METHOD_ID)){
            result = true;
        }
        if(sortItems(SectionType.CLASS_ID)){
            result = true;
        }
        return result;
    }
    private boolean sortItems(SectionType<?> sectionType){
        Section<?> section = getSection(sectionType);
        if(section != null){
            return section.sort();
        }
        return false;
    }
    public<T1 extends SectionItem> T1 getLoaded(SectionType<T1> sectionType, Key key){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getSectionItem(key);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    @Override
    public<T1 extends SectionItem> Section<T1> getSection(SectionType<T1> sectionType){
        return (Section<T1>) typeMap.get(sectionType);
    }
    @Override
    public SectionList getSectionList() {
        return this;
    }
    @Override
    public<T1 extends SectionItem> Section<T1> getOrCreateSection(SectionType<T1> sectionType){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section;
        }
        if(sectionType == SectionType.MAP_LIST || sectionType == SectionType.HEADER){
            return null;
        }
        MapList mapList = getMapList();
        MapItem mapItem = mapList.getOrCreate(sectionType);
        section = mapItem.createNewSection();
        add(section);
        sortSection(SectionType.getR8Order());
        mapItem.link(getHeader());
        return section;
    }

    public int indexOf(Section<?> section){
        if(section == dexHeaderSection){
            return 0;
        }
        if(section == mapListSection){
            return getCount() - 1;
        }
        if (section == idSectionList.get(section.getIndex())){
            return 1 + section.getIndex();
        }
        if (section == dataSectionList.get(section.getIndex())){
            return 1 + idSectionList.size() + section.getIndex();
        }
        return -1;
    }
    @Override
    public Section<?> get(int i) {
        if(i == 0){
            return dexHeaderSection;
        }
        if(i == getCount() - 1){
            return mapListSection;
        }
        if(i <= idSectionList.size()){
            return idSectionList.get(i - 1);
        }
        return dataSectionList.get(i - 1 - idSectionList.size());
    }

    public boolean contains(Key key){
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
    private boolean contains(SectionType<?> sectionType, Key key){
        Section<?> section = getSection(sectionType);
        if(section != null){
            return section.contains(key);
        }
        return false;
    }

    public void keyChangedInternal(SectionItem item, SectionType<?> sectionType, Key oldKey){
        Section<?> section = getSection(sectionType);
        if(section == null){
            return;
        }
        section.keyChanged(item, oldKey);
        if(sectionType == SectionType.TYPE_ID){
            ClassId classId = getLoaded(SectionType.CLASS_ID, oldKey);
            if(classId != null){
                // getKey() call triggers keyChanged event
                classId.getKey();
            }
            //TODO: notify to all uses TypeKey
        }
    }
    public Iterator<Section<?>> getSections() {
        return new CombiningIterator<>(getIdSections(), getDataSections());
    }
    public Iterator<IdSection<?>> getIdSections() {
        return idSectionList.iterator();
    }
    public Iterator<DataSection<?>> getDataSections() {
        return dataSectionList.iterator();
    }
    @Override
    public int getCount(){
        return 2 + idSectionList.size() + dataSectionList.size();
    }
    @Override
    public Iterator<Section<?>> iterator() {
        return ArraySupplierIterator.of(this);
    }
    @Override
    public IntegerReference getOffsetReference() {
        return baseOffset;
    }

    private boolean canAddAll(Collection<IdItem> idItemCollection) {
        int reserveSpace = 200;
        Iterator<IdSection<?>> idSections = getIdSections();
        while (idSections.hasNext()){
            IdSection<?> section = idSections.next();
            if(!section.canAddAll(idItemCollection, reserveSpace)){
                return false;
            }
        }
        return true;
    }
    public boolean merge(MergeOptions options, ClassId classId){
        if(classId == null){
            options.onMergeError(getParentInstance(DexLayout.class), classId, "Null class id");
            return false;
        }
        if(classId.getParent() == null){
            options.onMergeError(getParentInstance(DexLayout.class), classId, "Destroyed class id");
            return false;
        }
        if(classId.getParent(SectionList.class) == this){
            options.onMergeError(getParentInstance(DexLayout.class), classId, "Class id is on same section");
            return false;
        }
        if(options.skipMerging(classId, classId.getKey())){
            return false;
        }
        if(contains(SectionType.CLASS_ID, classId.getKey())){
            options.onDuplicate(classId);
            return false;
        }
        ArrayCollection<IdItem> collection = classId.listUsedIds();
        if(!canAddAll(collection)){
            options.onDexFull(getParentInstance(DexLayout.class), classId);
            return false;
        }
        Section<ClassId> mySection = getOrCreateSection(SectionType.CLASS_ID);

        ClassId myClass = mySection.getOrCreate(classId.getKey());
        myClass.merge(classId);
        if(options.relocateClass()){
            classId.removeSelf();
        }
        options.onMergeSuccess(classId, classId.getKey());
        return true;
    }
    public boolean merge(MergeOptions options, SectionList sectionList){
        if(sectionList == this){
            options.onMergeError(getParentInstance(DexLayout.class), sectionList, "Can not merge with self");
            return false;
        }
        if(sectionList.getParent() == null){
            options.onMergeError(getParentInstance(DexLayout.class), sectionList, "Destroyed section list");
            return false;
        }
        Section<ClassId> comingSection = sectionList.getSection(SectionType.CLASS_ID);
        if(comingSection == null || comingSection.getCount() == 0){
            return false;
        }
        boolean mergedOnce = false;
        boolean mergedAll = true;
        Section<ClassId> mySection = getOrCreateSection(SectionType.CLASS_ID);
        BlockListArray<ClassId> comingArray = comingSection.getItemArray();
        int size = comingArray.size() - 1;
        for (int i = size; i >= 0; i--){
            ClassId coming = comingArray.get(i);
            TypeKey key = coming.getKey();
            if(options.skipMerging(coming, key)){
                continue;
            }
            if(mySection.contains(key)){
                options.onDuplicate(coming);
                continue;
            }
            ArrayCollection<IdItem> collection = coming.listUsedIds();
            if(!canAddAll(collection)){
                mergedAll = false;
                options.onDexFull(this.getParentInstance(DexLayout.class), coming);
                break;
            }
            ClassId classId = mySection.getOrCreate(coming.getKey());
            classId.merge(coming);
            options.onMergeSuccess(coming, key);
            if(options.relocateClass()){
                coming.removeSelf();
            }
            mergedOnce = true;
        }
        if(comingSection.getCount() == 0){
            SectionList comingSectionSectionList = comingSection.getSectionList();
            DexLayout dexLayout = comingSectionSectionList
                    .getParentInstance(DexLayout.class);
            dexLayout.clear();
        }
        if(mergedOnce){
            refresh();
            sortStrings();
            refresh();
        }
        return mergedAll;
    }
    public ClassId fromSmali(SmaliClass smaliClass) throws IOException {
        ClassId classId = getOrCreateSectionItem(SectionType.CLASS_ID, smaliClass.getKey());
        classId.fromSmali(smaliClass);
        return classId;
    }
    private static<T1 extends Section<?>> Comparator<T1> getOffsetComparator() {
        return (section1, section2) -> {
            if(section1 == section2){
                return 0;
            }
            if(section1 == null){
                return 1;
            }
            return section1.compareOffset(section2);
        };
    }
}
