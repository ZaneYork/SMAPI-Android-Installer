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
package com.reandroid.utils.collection;

import java.util.*;

public class ListItr<T> implements ListIterator<T> {

    private final List<T> list;
    private int cursor;
    private int lastRet;

    public ListItr(List<T> list, int start) {
        this.list = list;
        this.cursor = start;
        this.lastRet = -1;
    }
    public ListItr(List<T> list) {
        this(list, 0);
    }

    @Override
    public boolean hasNext() {
        return cursor < list.size();
    }
    @Override
    public T next() {
        int i = this.cursor;
        this.lastRet = i;
        this.cursor = i + 1;
        return list.get(i);
    }
    @Override
    public boolean hasPrevious() {
        return this.cursor != 0;
    }
    @Override
    public int nextIndex() {
        return this.cursor;
    }
    @Override
    public int previousIndex() {
        return this.cursor - 1;
    }
    @Override
    public void remove() {
        int i = this.lastRet;
        this.list.remove(i);
        this.cursor = i;
        this.lastRet = -1;
    }
    @Override
    public T previous() {
        int i = this.cursor - 1;
        if (i < 0) {
            throw new NoSuchElementException();
        }
        this.cursor = i;
        this.lastRet = i;
        return list.get(i);
    }
    @Override
    public void set(T item) {
        list.set(this.lastRet, item);
    }
    @Override
    public void add(T item) {
        int i = this.cursor;
        list.add(i, item);
        this.cursor = i + 1;
        this.lastRet = -1;
    }

    @SuppressWarnings("unchecked")
    public static<T1> ListIterator<T1> empty(){
        ListItr<?> empty = empty_itr;
        if(empty == null){
            empty = new ListItr<>(EmptyList.of());
            empty_itr = empty;
        }
        return (ListIterator<T1>) empty;
    }

    public static<T1> ListIterator<T1> of(List<T1> list){
        return of(list, 0);
    }
    public static<T1> ListIterator<T1> of(List<T1> list, int start){
        if((list.size() - start) <= 0){
            return empty();
        }
        return new ListItr<>(list, start);
    }

    private static ListItr<?> empty_itr;
}
