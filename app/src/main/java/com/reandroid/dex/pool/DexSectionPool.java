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
package com.reandroid.dex.pool;

import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.MultiMap;

import java.util.Comparator;

public class DexSectionPool<T extends SectionItem> extends MultiMap<Key, T> {

    private final Section<T> section;

    private boolean keyItems;
    private boolean keyItemsCreate;
    private boolean keyItemsChecked;

    public DexSectionPool(Section<T> section) {
        this.section = section;
    }

    public T getOrCreate(Key key){
        if(key == null || !isKeyItemsCreate()){
            return null;
        }
        T item = get(key);
        if(item == null) {
            item = createNext(key);
            put(key, item);
        }
        return item;
    }
    public SectionType<T> getSectionType(){
        return getSection().getSectionType();
    }
    public void remove(T item){
        if(item != null) {
            super.remove(item.getKey(), item);
        }
    }
    public boolean contains(Key key){
        return super.containsKey(key);
    }
    public void load(){
        if(!isKeyItems()){
            return;
        }
        Section<T> section = this.getSection();
        putAll(T::getKey, section.iterator());
    }
    T createNext(Key key){
        T item = getSection().createItem();
        ((ModifiableKeyItem) item).setKey(key);
        return item;
    }
    Section<T> getSection(){
        return this.section;
    }
    public int clearDuplicates() {
        if (size() == 0 || size() == getSection().getCount()) {
            return 0;
        }
        ArrayCollection<T> result = new ArrayCollection<>();
        Comparator<T> comparator = (item1, item2) -> {
            int i = CompareUtil.compare(item1.isRemoved(), item2.isRemoved());
            if(i != 0 || item1.isRemoved()) {
                return i;
            }
            i = CompareUtil.compare(item1.getKey(), item2.getKey());
            return i;
        };
        findDuplicates(comparator,
                list -> {
                    T first = list.get(0);
                    for(int i = 1; i < list.size(); i++) {
                        T item = list.get(i);
                        item.setReplace(first);
                        result.add(item);
                    }
                });
        Section<T> section = getSection();
        section.getItemArray().removeAll(result);
        return result.size();
    }
    boolean isKeyItemsCreate(){
        isKeyItems();
        return keyItemsCreate;
    }
    private boolean isKeyItems(){
        if(keyItemsChecked){
            return keyItems;
        }
        if(getSectionType().isIdSection()){
            keyItemsChecked = true;
            keyItems = true;
            keyItemsCreate = true;
            return true;
        }
        T sample = getSectionType().getCreator().newInstance();
        keyItemsChecked = true;
        keyItems = sample instanceof KeyItem;
        keyItemsCreate = sample instanceof ModifiableKeyItem;
        return keyItems;
    }
    @Override
    public String toString() {
        return getSectionType().getName() + "-Pool = " + size();
    }
}
