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
package com.reandroid.arsc.array;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.StyleItem;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class StyleArray extends OffsetBlockArray<StyleItem> implements
        Iterable<StyleItem>, JSONConvert<JSONArray> {

    public StyleArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart) {
        super(offsets, itemCount, itemStart);
    }

    @Override
    public void clear() {
        for (StyleItem styleItem : this) {
            if (!styleItem.isNull()) {
                styleItem.onRemoved();
            }
        }
        super.clear();
    }
    @Override
    void refreshAlignment(BlockReader reader, AlignItem alignItem) throws IOException {
        alignItem.clear();
        alignItem.setFill(END_BYTE);
        if(reader.available() < 4){
            return;
        }
        IntegerItem integerItem = new IntegerItem();
        while (reader.available() >= 4){
            int position = reader.getPosition();
            integerItem.readBytes(reader);
            if(integerItem.get() != 0xFFFFFFFF){
                reader.seek(position);
                break;
            }
            alignItem.setSize(alignItem.size() + 4);
        }
    }
    @Override
    void refreshAlignment(AlignItem alignItem) {
        if(size() == 0){
            alignItem.clear();
            return;
        }
        alignItem.setFill(END_BYTE);
        alignItem.ensureSize(8);
    }
    @Override
    public StyleItem newInstance() {
        return new StyleItem();
    }
    @Override
    public StyleItem[] newArrayInstance(int length) {
        return new StyleItem[length];
    }
    public boolean sort() {
        return sort(CompareUtil.getComparableComparator());
    }

    @Override
    public boolean sort(Comparator<? super StyleItem> comparator) {
        boolean sorted = super.sort(comparator);
        adjustIndexes();
        trimLastItems();
        return sorted;
    }
    private void adjustIndexes() {
        Iterator<StyleItem> iterator = clonedIterator();
        boolean adjusted = false;
        while (iterator.hasNext()) {
            StyleItem styleItem = iterator.next();
            StringItem stringItem = styleItem.getStringItemInternal();
            if(stringItem != null) {
                int index = stringItem.getIndex();
                if(index != styleItem.getIndex()) {
                    moveTo(styleItem, index);
                    adjusted = true;
                }
            }
        }
        if(adjusted) {
            getParentInstance(StringPool.class).linkStylesInternal();
        }
    }
    private void trimLastItems() {
        trimLastIf(StyleItem::isEmpty);
    }
    @Override
    public JSONArray toJson() {
        return null;
    }
    @Override
    public void fromJson(JSONArray json) {
    }

    private static final byte END_BYTE = (byte) 0xFF;
}
