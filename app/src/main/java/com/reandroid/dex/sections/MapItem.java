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
import com.reandroid.arsc.item.IndirectInteger;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.header.CountAndOffset;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.utils.HexUtil;

public class MapItem extends DexBlockItem {
    private final IndirectInteger type;
    private final ParallelIntegerPair countAndOffset;

    public MapItem() {
        super(SIZE);
        this.type = new IndirectInteger(this, 0);
        this.countAndOffset = new ParallelIntegerPair(new IndirectIntegerPair(this, 4));
    }

    public void link(DexHeader header){
        CountAndOffset headerCountAndOffset = header.get(getSectionType());
        if(headerCountAndOffset != null){
            getCountAndOffset().setReference2(headerCountAndOffset);
        }
    }
    public ParallelIntegerPair getCountAndOffset() {
        return countAndOffset;
    }
    public<T1 extends SectionItem> SectionType<T1> getSectionType(){
        return SectionType.get(getType().get());
    }

    public<T1 extends SectionItem> Section<T1> createNewSection(){
        SectionType<T1> sectionType = getSectionType();
        if(sectionType == null){
            System.err.println("Unknown section: " + toString());
            return null;
        }
        if(sectionType.getCreator() == null){
            System.err.println("Unimplemented section: " + toString());
            return null;
        }
        Block parent = getParent(SectionList.class);
        if(parent != null){
            parent = parent.getParent();
        }
        if(parent == null){
            parent = getParent(MapList.class);
        }
        if(parent == null){
            parent = getParent(DexLayout.class);
        }
        if(parent == null){
            parent = getParent();
        }
        Section<T1> section = sectionType.createSection(getCountAndOffset());
        section.setParent(parent);
        return section;
    }

    public void setType(SectionType<?> type){
        getType().set(type.getType());
    }
    public IntegerReference getType(){
        return type;
    }
    public ParallelReference getCount(){
        return countAndOffset.getFirst();
    }
    public int getCountValue(){
        return getCount().get();
    }
    public void setCount(int value){
        getCount().set(value);
    }
    public ParallelReference getOffset(){
        return countAndOffset.getSecond();
    }
    public int getOffsetValue(){
        return getOffset().get();
    }
    public void setOffset(int value){
        getOffset().set(value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        SectionType<?> sectionType = getSectionType();
        String name;
        if(sectionType == null){
            name = HexUtil.toHex("UNKNOWN_", getType().get(), 1);
        }else {
            name = sectionType.getName();
        }
        builder.append(name);
        builder.append(' ');
        int fill = 24 - name.length();
        for(int i = 0; i < fill; i++){
            builder.append('-');
        }
        IntegerPair co = getCountAndOffset();
        builder.append("[");
        name = Integer.toString(co.getFirst().get());
        builder.append(name);
        fill = 6 - name.length();
        for(int i = 0; i < fill; i++){
            builder.append(' ');
        }
        builder.append(',');
        name = Integer.toString(co.getSecond().get());
        fill = 8 - name.length();
        for(int i = 0; i < fill; i++){
            builder.append(' ');
        }
        builder.append(name);
        builder.append(']');
        return builder.toString();
    }

    public static final int SIZE = 12;
}
