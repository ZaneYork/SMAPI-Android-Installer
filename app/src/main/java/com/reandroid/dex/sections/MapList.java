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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.utils.collection.ArraySort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MapList extends SpecialItem
        implements Iterable<MapItem>, PositionAlignedItem {

    private final CountedArray<MapItem> itemArray;
    private final DexPositionAlign positionAlign;

    private final ParallelReference fileSize;
    private final ParallelReference dataStart;
    private final ParallelReference dataSize;

    public MapList(IntegerReference offsetReference) {
        super(3);
        IntegerItem mapItemsCount = new IntegerItem();
        this.itemArray = new CountedArray<>(mapItemsCount, CREATOR);
        this.positionAlign = new DexPositionAlign();

        addChild(0, positionAlign);
        addChild(1, mapItemsCount);
        addChild(2, itemArray);

        setOffsetReference(offsetReference);

        this.fileSize = new ParallelReference(new FileSizeReference(this));
        this.dataStart = new ParallelReference(new DataStartReference(this));
        this.dataSize = new ParallelReference(new DataSizeReference(this));
    }

    @Override
    public SectionType<MapList> getSectionType() {
        return SectionType.MAP_LIST;
    }

    public ParallelReference getFileSize(){
        return fileSize;
    }
    public ParallelReference getDataStart() {
        return dataStart;
    }
    public ParallelReference getDataSize() {
        return dataSize;
    }

    public void sortMapItems(SectionType<?>[] order){
        itemArray.sort(SectionType.comparator(order, MapItem::getSectionType));
    }

    public void linkHeader(DexHeader dexHeader){
        linkSpecialReference(SectionType.HEADER);
        linkSpecialReference(SectionType.MAP_LIST);
        linkIdTypesHeader(dexHeader);
        getFileSize().setReference2(dexHeader.fileSize);
        getDataSize().setReference2(dexHeader.data.getFirst());
        getDataStart().setReference2(dexHeader.data.getSecond());
    }
    private void linkSpecialReference(SectionType<?> sectionType){
        MapItem mapItem = get(sectionType);
        Section<?> section = getSection(sectionType);
        ParallelIntegerPair pair = (ParallelIntegerPair) section.getItemArray()
                .getCountAndOffset();
        pair.setReference2(mapItem.getCountAndOffset());
        pair.refresh();
    }
    private void linkIdTypesHeader(DexHeader dexHeader){
        Iterator<SectionType<?>> iterator = SectionType.getIdSectionTypes();
        while (iterator.hasNext()){
            SectionType<?> sectionType = iterator.next();
            MapItem mapItem = get(sectionType);
            if(mapItem == null){
                continue;
            }
            mapItem.link(dexHeader);
        }
    }
    public MapItem getDataStartItem(){
        boolean headerFound = false;
        for(MapItem mapItem : this){
            SectionType<?> sectionType = mapItem.getSectionType();
            if(!headerFound){
                headerFound = sectionType == SectionType.HEADER;
                continue;
            }
            if(sectionType.isDataSection()){
                return mapItem;
            }
        }
        return null;
    }
    public void remove(SectionType<?> sectionType){
        remove(get(sectionType));
    }
    public void remove(MapItem mapItem){
        if(mapItem == null){
            return;
        }
        ParallelIntegerPair pair = mapItem.getCountAndOffset();
        pair.getFirst().set(0);
        pair.getSecond().set(0);
        pair.setReference2(null);
        itemArray.remove(mapItem);
        mapItem.setParent(null);
        mapItem.setIndex(-1);
    }
    public MapItem get(SectionType<?> type){
        for(MapItem mapItem:this){
            if(type == mapItem.getSectionType()){
                return mapItem;
            }
        }
        return null;
    }
    public MapItem getOrCreate(SectionType<?> type){
        MapItem mapItem = get(type);
        if(mapItem != null){
            return null;
        }
        mapItem = itemArray.createNext();
        mapItem.setType(type);
        return mapItem;
    }
    @Override
    public Iterator<MapItem> iterator() {
        return itemArray.iterator();
    }

    public MapItem[] getReadSorted(){
        List<MapItem> list = itemArray.getChildes();
        MapItem[] mapItemList = list.toArray(new MapItem[list.size()]);
        Comparator<MapItem> comparator = SectionType.comparator(
                SectionType.getReadOrderList(), MapItem::getSectionType);
        ArraySort.sort(mapItemList, comparator);
        return mapItemList;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        getFileSize().refresh();
        getDataStart().refresh();
        getDataSize().refresh();
    }

    @Override
    public String toString() {
        Iterator<MapItem> it = itemArray.iterator();
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (it.hasNext()){
            MapItem item = it.next();
            if(i != 0){
                builder.append('\n');
            }
            if(i < 9){
                builder.append(' ');
            }
            builder.append((i + 1));
            builder.append(") ");
            builder.append(item);
            i ++;
        }
        return builder.toString();
    }

    @Override
    public DexPositionAlign getPositionAlign() {
        return positionAlign;
    }

    private static final Creator<MapItem> CREATOR = new Creator<MapItem>() {
        @Override
        public MapItem[] newArrayInstance(int length) {
            return new MapItem[length];
        }
        @Override
        public MapItem newInstance() {
            return new MapItem();
        }
    };

    static class DataStartReference implements IntegerReference {

        private final MapList mapList;

        private DataStartReference(MapList mapList) {
            this.mapList = mapList;
        }

        @Override
        public int get() {
            MapItem mapItem = mapList.getDataStartItem();
            if(mapItem != null){
                return mapItem.getOffset().get();
            }
            return 0;
        }
        @Override
        public void set(int value) {
        }
    }

    static class DataSizeReference implements IntegerReference {

        private final MapList mapList;

        private DataSizeReference(MapList mapList) {
            this.mapList = mapList;
        }

        @Override
        public int get() {
            MapItem mapItem = mapList.getDataStartItem();
            if(mapItem != null){
                return mapList.getFileSize().get() - mapItem.getOffset().get();
            }
            return 0;
        }
        @Override
        public void set(int value) {
        }
    }

    static class FileSizeReference implements IntegerReference {

        private final MapList mapList;

        private FileSizeReference(MapList mapList) {
            this.mapList = mapList;
        }
        @Override
        public int get() {
            return mapList.getOffset() + mapList.countBytes();
        }
        @Override
        public void set(int value) {
        }
    }
}
