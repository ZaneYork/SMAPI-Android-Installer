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
package com.reandroid.arsc.container;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.Swappable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class BlockList<T extends Block> extends Block implements BlockRefresh, Swappable {
    private ArrayCollection<T> mItems;
    private Creator<? extends T> mCreator;

    public BlockList(Creator<? extends T> creator){
        super();
        mItems = ArrayCollection.empty();
        this.mCreator = creator;
    }
    public BlockList(){
        this(null);
    }

    public Creator<? extends T> getCreator() {
        return mCreator;
    }
    public void setCreator(Creator<T> creator) {
        this.mCreator = creator;
        if(mItems.isImmutableEmpty()){
            return;
        }
        updateCreator();
    }
    public void ensureSize(int size){
        if(size > this.size()){
            setSize(size);
        }
    }
    public void setSize(int size){
        if(size == 0){
            lockList();
        }else if(mCreator != null || size < size()){
            unlockList();
            mItems.setSize(size);
        }
    }
    public void clearTemporarily() {
        mItems.clearTemporarily();
    }
    public void setElements(T[] elements){
        if(elements == null || elements.length == 0){
            lockList();
            return;
        }
        unlockList();
        Creator<? extends T> creator = getCreator();
        int length = elements.length;
        for(int i = 0; i < length; i++){
            T item = elements[i];
            if(item == null && creator != null){
                item = creator.newInstanceAt(i);
                elements[i] = item;
            }
            onItemCreated(i, item);
        }
        mItems.setElements(elements);
        onChanged();
    }
    void onItemCreated(int index, T item){
        if(item == null){
            return;
        }
        item.setIndex(index);
        item.setParent(this);
    }
    public T createAt(int index){
        Creator<? extends T> creator = getCreator();
        ensureSize(index);
        T item = creator.newInstanceAt(index);
        add(index, item);
        return item;
    }
    public T createNext(){
        Creator<? extends T> creator = getCreator();
        T item = creator.newInstanceAt(size());
        add(item);
        return item;
    }
    public T getFirst(){
        int size = size();
        for(int i = 0; i < size; i++){
            T item = get(i);
            if(item != null){
                return item;
            }
        }
        return null;
    }
    public T getLast(){
        int size = size() - 1;
        for(int i = size; i >= 0; i--){
            T item = get(i);
            if(item != null){
                return item;
            }
        }
        return null;
    }

    public Iterator<T> clonedIterator(){
        return mItems.clonedIterator();
    }
    public Iterator<T> clonedIterator(int start){
        return clonedIterator(start, size() - start);
    }
    public Iterator<T> clonedIterator(int start, int length){
        return mItems.clonedIterator(start, length);
    }
    public Iterator<T> arrayIterator(){
        return mItems.arrayIterator();
    }
    public Iterator<T> iterator(){
        return mItems.iterator();
    }
    public Iterator<T> iterator(int start, int length){
        return mItems.iterator(start, length);
    }
    public Iterator<T> iterator(Predicate<? super T> filter){
        return mItems.iterator(filter);
    }
    public<T1> Iterator<T1> iterator(Class<T1> instance){
        return mItems.iterator(instance);
    }

    public int countIf(Predicate<? super T> predicate){
        return mItems.count(predicate);
    }
    public int countFromLast(Predicate<? super T> predicate){
        return mItems.countFromLast(predicate);
    }
    public ArrayCollection<T> subListIf(Predicate<? super T> predicate) {
        return mItems.subListIf(predicate);
    }
    public void clearChildes(){
        if(mItems.isEmpty()){
            return;
        }
        int size = size();
        for (int i = 0; i < size; i++){
            remove(size() - 1, false);
        }
        lockList();
        onChanged();
    }
    public void destroy(){
        mItems.clear();
        lockList();
        onChanged();
    }
    public boolean sort(Comparator<? super T> comparator){
        if(size() < 2){
            return false;
        }
        boolean sorted = mItems.sort(comparator, (i, j) -> {
            T item1 = get(i);
            T item2 = get(j);
            if(item1 != null) {
                item1.setIndex(i);
            }
            if(item2 != null) {
                item2.setIndex(j);
            }
        });
        if(sorted) {
            updateIndex();
        }
        return sorted;
    }
    public boolean sort(Comparator<? super T> comparator, Swappable swappable){
        if(size() < 2){
            return false;
        }
        if(mItems.sort(comparator, swappable)){
            return updateIndex();
        }
        return false;
    }
    public boolean needsSort(Comparator<? super T> comparator) {
        if(comparator == null){
            return false;
        }
        int length = size();
        if(length < 2){
            return false;
        }
        T previous = get(0);
        for(int i = 1; i < length; i++){
            T item = get(i);
            if(comparator.compare(previous, item) > 0){
                return true;
            }
            previous = item;
        }
        return false;
    }
    public boolean removeAll(Collection<?> collection) {
        return removeAllIndexes(toIndexArray(collection));
    }
    private int[] toIndexArray(Collection<?> collection) {
        int[] results = new int[collection.size()];
        int i = 0;
        List<T> items = this.mItems;
        int size = items.size();
        for(Object obj : collection) {
            if(obj == null) {
                continue;
            }
            Block block = (Block) obj;
            int index = block.getIndex();
            if(index < 0 || index >= size || block != items.get(index)) {
                continue;
            }
            results[i] = index;
            i ++;
        }
        int length = results.length;
        while (i < length) {
            results[i] = -1;
            i ++;
        }
        return results;
    }
    public boolean removeAllIndexes(int[] indexes) {
        mItems.removeAllIndexes(indexes);
        updateIndex();
        return true;
    }
    public boolean removeIf(Predicate<? super T> filter){
        boolean removed = mItems.removeIf(filter);
        if(removed) {
            updateIndex();
        }
        return removed;
    }
    public T remove(int index){
        return remove(index, true);
    }
    private T remove(int index, boolean updateIndex){
        T item = mItems.remove(index);
        if(item == null){
            return null;
        }
        item.setParent(null);
        item.setIndex(-1);
        if(updateIndex){
            updateIndex(index);
        }
        onChanged();
        return item;
    }
    public boolean remove(T item){
        if(item == null) {
            return false;
        }
        int index = mItems.indexOfFast(item, item.getIndex());
        if(index < 0){
            index = mItems.indexOfFast(item);
        }
        if(index < 0) {
            return false;
        }
        boolean removed = mItems.remove(index) != null;
        if(removed) {
            updateIndex(index);
            item.setIndex(-1);
            item.setParent(null);
        }
        onChanged();
        return removed;
    }
    public int indexOf(T item){
        if(item == null) {
            return -1;
        }
        int index = mItems.indexOfFast(item, item.getIndex());
        if(index < 0){
            index = mItems.indexOfFast(item);
        }
        return index;
    }
    protected void notifyPreRemove(T item) {
        if(item != null && item.getParent() == this) {
            onPreRemove(item);
            item.setIndex(-1);
            item.setParent(null);
        }
    }
    public void onPreRemove(T item){

    }
    public boolean swap(int i, int j) {
        if(i == j) {
            return false;
        }
        return swap(get(i), get(j));
    }
    public boolean swap(T item1, T item2){
        if(item1 == item2 || item1 == null || item2 == null){
            return false;
        }
        int i1 = item1.getIndex();
        int i2 = item2.getIndex();
        mItems.swap(i1, i2);
        item1.setIndex(i2);
        item2.setIndex(i1);
        return true;
    }
    public void moveTo(T item, int index){
        if(index < 0){
            index = 0;
        }
        int i = mItems.indexOfFast(item, item.getIndex());
        mItems.move(item, index);
        updateIndex(i, index);
    }
    public void set(int index, T item){
        if(item == null){
            return;
        }
        unlockList();
        item.setIndex(index);
        item.setParent(this);
        mItems.set(index, item);
        onChanged();
    }
    public void addAll(int index, T[] items){
        if(items == null){
            return;
        }
        int length = items.length;
        if(length == 0){
            return;
        }
        unlockList();
        mItems.addAll(index, items);
        for(int i = 0; i < length; i++){
            T item = items[i];
            if(item == null){
                continue;
            }
            item.setIndex(index);
            item.setParent(this);
            index ++;
        }
        updateIndex(index);
        onChanged();
    }
    public void add(int index, T item){
        if(item == null){
            return;
        }
        unlockList();
        item.setIndex(index);
        item.setParent(this);
        mItems.add(index, item);
        updateIndex(index);
        onChanged();
    }
    private boolean updateIndex(){
        return updateIndex(0);
    }
    private boolean updateIndex(int start){
        return updateIndex(start, size());
    }
    private boolean updateIndex(int start, int end){
        if(start < 0){
            start = 0;
        }
        if(start > end){
            int i = start;
            start = end;
            end = i;
        }
        end = end + 1;
        boolean changed = false;
        int count = size();
        if(end > count){
            end = count;
        }
        List<T> items = this.getChildes();
        for (int i = start; i < end; i++){
            T item = items.get(i);
            if(item.getIndex() != i){
                item.setIndex(i);
                changed = true;
            }
        }
        return changed;
    }
    public boolean add(T item){
        if(item == null){
            return false;
        }
        unlockList();
        int index = size();
        item.setIndex(index);
        item.setParent(this);
        boolean result = mItems.add(item);
        onChanged();
        return result;
    }
    public T get(int i){
        if(i>=mItems.size() || i<0){
            return null;
        }
        return mItems.get(i);
    }
    public int getCount(){
        return size();
    }
    public int size(){
        return mItems.size();
    }
    public void ensureCapacity(int capacity){
        unlockList();
        mItems.ensureCapacity(capacity);
    }
    public void trimToSize(){
        mItems.trimToSize();
        if(mItems.size() == 0){
            lockList();
        }
    }
    public boolean contains(Object obj){
        return mItems.contains(obj);
    }
    public boolean containsExact(Object obj){
        return mItems.containsFast(obj);
    }
    public Object[] toArray(){
        return mItems.toArray();
    }
    public <T1> T1[] toArray(T1[] ts) {
        return mItems.toArray(ts);
    }

    public List<T> getChildes(){
        return mItems;
    }
    private void lockList(){
        if(mItems.isImmutableEmpty()){
            return;
        }
        mItems = ArrayCollection.empty();
    }
    private void unlockList(){
        if(!mItems.isImmutableEmpty()){
            return;
        }
        mItems = new ArrayCollection<>();
        updateCreator();
        mItems.setMonitor(getMonitor());
    }
    protected ArrayCollection.Monitor<T> getMonitor() {
        return new ArrayCollection.Monitor<T>() {
            @Override
            public void onAdd(int i, T item) {
            }
            @Override
            public void onRemoved(int i, T item) {
                notifyPreRemove(item);
            }
        };
    }
    private void updateCreator(){
        Creator<? extends T> creator = getCreator();
        if(creator == null){
            mItems.setInitializer(null);
            return;
        }
        ArrayCollection.Initializer<T> initializer = new ArrayCollection.Initializer<T>() {
            @Override
            public T createNewItem(int index) {
                T item = creator.newInstanceAt(index);
                onItemCreated(index, item);
                return item;
            }
            @Override
            public T[] newArray(int length) {
                return creator.newArrayInstance(length);
            }
        };
        mItems.setInitializer(initializer);
    }
    @Override
    public final void refresh(){
        if(isNull()){
            return;
        }
        trimToSize();
        onPreRefresh();
        refreshChildes();
        onRefreshed();
        onChanged();
    }
    protected void onPreRefresh(){
    }
    protected void onRefreshed(){
        onChanged();
    }
    public void onChanged(){
        mItems.onChanged();
    }
    private void refreshChildes(){
        Iterator<?> iterator = iterator();
        while (iterator.hasNext()){
            Object item = iterator.next();
            if(item instanceof BlockRefresh){
                ((BlockRefresh) item).refresh();
            }
        }
    }
    @Override
    public byte[] getBytes() {
        byte[] results = null;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            results = addBytes(results, iterator.next().getBytes());
        }
        return results;
    }
    @Override
    public int countBytes() {
        int result = 0;
        int size = size();
        for (int i = 0; i < size; i++){
            T item = get(i);
            if(item == null){
                continue;
            }
            result += item.countBytes();
        }
        return result;
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END == this){
            counter.FOUND = true;
            return;
        }
        int size = size();
        for (int i = 0; i < size && !counter.FOUND; i++){
            T item = get(i);
            if(item == null){
                continue;
            }
            item.onCountUpTo(counter);
        }
    }
    public void readChildes(BlockReader reader) throws IOException{
        int size = this.size();
        for(int i = 0; i < size; i++){
            T item = get(i);
            item.readBytes(reader);
        }
        onChanged();
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result = 0;
        int size = size();
        for (int i = 0; i < size; i++){
            T item = get(i);
            if(item == null){
                continue;
            }
            result += item.writeBytes(stream);
        }
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BlockList<?> blockList = (BlockList<?>) obj;
        return mItems.equals(blockList.mItems);
    }
    @Override
    public int hashCode() {
        return mItems.hashCode();
    }

    @Override
    public String toString() {
        return "size=" + size();
    }

    public static boolean isImmutableEmpty(Object blockList){
        return empty_list == blockList;
    }
    @SuppressWarnings("unchecked")
    public static<T1 extends Block> BlockList<T1> empty(){
        return (BlockList<T1>) empty_list;
    }

    private static final BlockList<?> empty_list = new BlockList<Block>(){
        @Override
        public boolean add(Block item) {
            throw new IllegalArgumentException("Empty BlockList");
        }
        @Override
        public void add(int index, Block item) {
            throw new IllegalArgumentException("Empty BlockList");
        }
        @Override
        public void ensureCapacity(int capacity) {
            if(capacity != 0){
                throw new IllegalArgumentException("Empty BlockList");
            }
        }
        @Override
        public void setSize(int size) {
            if(size != 0){
                throw new IllegalArgumentException("Empty BlockList");
            }
        }
        @Override
        public int size() {
            return 0;
        }
    };
}
