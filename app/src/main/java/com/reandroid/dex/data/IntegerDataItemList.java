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
package com.reandroid.dex.data;

import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.ArraySort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class IntegerDataItemList<T extends DataItem> extends IntegerList implements Iterable<T>{
    private final SectionType<T> sectionType;
    private final int usageType;
    private T[] items;

    public IntegerDataItemList(SectionType<T> sectionType, int usageType, DexPositionAlign positionAlign) {
        super(positionAlign);
        this.sectionType = sectionType;
        this.usageType = usageType;
    }

    public T addNew(Key key){
        T item = getOrCreateSection(sectionType).getOrCreate(key);
        add(item.getIdx());
        return item;
    }
    public T addNew(){
        T item = getOrCreateSection(sectionType).createItem();
        add(item.getIdx());
        return item;
    }
    public T getOrCreateAt(int index){
        T item = getItem(index);
        if(item == null){
            ensureSize(index + 1);
            item = getOrCreateSection(sectionType).createItem();
            put(index, item.getIdx());
            onChanged();
        }
        return item;
    }
    public void addNull(){
        add(0);
    }

    @Override
    public void removeSelf() {
        setItems(null);
        super.removeSelf();
    }

    public void remove(T item) {
        removeIf(t -> t == item);
    }
    public void removeIf(Predicate<? super T> filter) {
        T[] items = this.items;
        if(items == null){
            return;
        }
        int length = items.length;
        boolean found = false;
        for(int i = 0; i < length; i++){
            T item = items[i];
            if(filter.test(item)){
                items[i] = null;
                found = true;
            }
        }
        if(found){
            removeNulls();
        }
    }
    void removeNulls() {
        T[] items = this.items;
        if(items == null || items.length == 0){
            setItems(null);
            return;
        }
        int length = items.length;
        int count = 0;
        for(int i = 0; i < length; i++){
            if(items[i] == null){
                count ++;
            }
        }
        if(count == 0){
            return;
        }
        T[] update = sectionType.getCreator()
                .newArrayInstance(length - count);
        int index = 0;
        for(int i  = 0; i < length; i++){
            T element = items[i];
            if(element != null){
                update[index] = element;
                index++;
            }
        }
        setItems(update);
    }
    @Override
    public Iterator<T> iterator() {
        return ArrayIterator.of(items);
    }
    public T getItem(int i){
        if(i < 0){
            return null;
        }
        T[] items = this.items;
        if(items == null || i >= items.length){
            return null;
        }
        return items[i];
    }
    public T[] getItems() {
        return items;
    }
    public void setItems(T[] items){
        if(items == this.items){
            return;
        }
        if(isEmpty(items)){
            this.items = null;
            setSize(0);
            return;
        }
        int length = items.length;
        setSize(length, false);
        for(int i = 0; i < length; i++){
            T item = items[i];
            put(i, getData(item));
            updateUsage(item);
        }
        this.items = items;
    }
    public boolean isEmpty() {
        return isEmpty(this.items);
    }
    public boolean sort(Comparator<? super T> comparator){
        T[] items = this.items;
        if(items == null || items.length < 2){
            return false;
        }
        boolean sorted = ArraySort.sort(items, comparator);
        if(sorted){
            setItems(items.clone());
        }
        return sorted;
    }
    private void updateUsage(T[] items){
        if(items == null){
            return;
        }
        for(T item : items){
            updateUsage(item);
        }
    }
    private void updateUsage(T item){
        if(item == null){
            return;
        }
        item.addUsageType(usageType);
    }

    @Override
    void onChanged() {
        super.onChanged();
        cacheItems();
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        refreshItems();
    }

    private void refreshItems(){
        T[] items = this.items;
        if(isEmpty(items)){
            this.items = null;
            setSize(0);
            return;
        }
        int length = items.length;
        setSize(length, false);
        boolean found = false;
        for(int i = 0; i < length; i++){
            T item = items[i];
            if(item != null){
                item = item.getReplace();
                items[i] = item;
            }
            int data = getData(item);
            put(i, getData(item));
            if(data == 0) {
                items[i] = null;
                found = true;
            }
            updateUsage(item);
        }
        if(found){
            removeNulls();
        }
    }
    private int getData(T item){
        if(item == null){
            return 0;
        }
        return item.getIdx();
    }
    private void cacheItems(){
        items = getSectionItem(sectionType, toArray());
        updateUsage(items);
    }
    private boolean isEmpty(T[] items){
        if(items == null || items.length == 0){
            return true;
        }
        for(int i = 0; i < items.length; i++){
            if(items[i] != null){
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        int hash = 1;
        int size = size();
        for(int i = 0; i < size; i++){
            hash = hash * 31;
            Object item = getItem(i);
            if(item != null){
                hash = hash + item.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        IntegerDataItemList<?> itemList = (IntegerDataItemList<?>)obj;
        int size = size();
        if(size != itemList.size()){
            return false;
        }
        for(int i = 0; i < size; i++){
            Object item1 = getItem(i);
            Object item2 = itemList.getItem(i);
            if(!Objects.equals(item1, item2)){
                return false;
            }
        }
        return true;
    }
}
