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

public class EmptyList<T> implements List<T>, EmptyItem {
    @Override
    public int size() {
        return 0;
    }
    @Override
    public boolean isEmpty() {
        return true;
    }
    @Override
    public boolean contains(Object o) {
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        return EmptyIterator.of();
    }
    @Override
    public Object[] toArray() {
        return EMPTY;
    }
    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return t1s;
    }

    @Override
    public boolean add(T t) {
        throw new IllegalArgumentException("Empty list");
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        if(collection == null || collection.isEmpty()) {
            return false;
        }
        throw new IllegalArgumentException("Empty list");
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> collection) {
        if(collection == null || collection.isEmpty()) {
            return false;
        }
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public void clear() {
    }
    @Override
    public T get(int i) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public T set(int i, T t) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public void add(int i, T t) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public T remove(int i) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public int indexOf(Object o) {
        return -1;
    }
    @Override
    public int lastIndexOf(Object o) {
        return -1;
    }
    @Override
    public ListIterator<T> listIterator() {
        return EmptyIterator.of();
    }
    @Override
    public ListIterator<T> listIterator(int i) {
        return EmptyIterator.of();
    }
    @Override
    public List<T> subList(int i, int i1) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public void sort(Comparator<? super T> c) {
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof Collection)){
            return false;
        }
        return ((Collection<?>) obj).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <T1> EmptyList<T1> of(){
        return (EmptyList<T1>) INS;
    }

    private static final EmptyList<?> INS = new EmptyList<>();
    private static final Object[] EMPTY = new Object[0];
}
