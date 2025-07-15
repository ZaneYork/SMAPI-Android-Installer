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
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;

import java.util.Iterator;

public class SectionArray<T extends SectionItem> extends BlockListArray<T> implements FullRefresh {

    public SectionArray(IntegerPair countAndOffset, Creator<T> creator) {
        super(countAndOffset, creator);
    }

    @Override
    public void refreshFull() {
        if(!(getFirst() instanceof FullRefresh)){
            return;
        }
        Iterator<?> iterator = iterator();
        while (iterator.hasNext()){
            FullRefresh refreshFull = (FullRefresh) iterator.next();
            refreshFull.refreshFull();
        }
    }

    int updatePositionedItemOffsets(int position){
        int count = this.getCount();
        this.getCountAndOffset().getFirst().set(count);
        if(!(getFirst() instanceof PositionedItem)){
            return position;
        }
        DexPositionAlign previous = null;
        for(int i = 0; i < count; i++){
            T item = this.get(i);
            if(item == null) {
                previous = null;
                continue;
            }
            DexPositionAlign itemAlign = null;
            if(item instanceof PositionAlignedItem){
                itemAlign = ((PositionAlignedItem) item).getPositionAlign();
                if(itemAlign != null){
                    itemAlign.setSize(0);
                }
                if(previous != null){
                    previous.align(position);
                    position += previous.size();
                }
            }
            if(i == count-1){
                ((PositionedItem)item).removeLastAlign();
            }
            ((PositionedItem)item).setPosition(position);
            position += item.countBytes();
            previous = itemAlign;
        }
        return position;
    }
    @Override
    public void onPreRemove(T item) {
        super.onPreRemove(item);
        notifyBeforeRemoved(item);
    }
    private void notifyBeforeRemoved(T item){
        Section<T> section = getParentSection();
        if(section != null){
            section.onRemoving(item);
        }
    }
    @SuppressWarnings("unchecked")
    Section<T> getParentSection(){
        return getParentInstance(Section.class);
    }

    public void clear(){
        clearChildes();
        destroy();
    }
}
