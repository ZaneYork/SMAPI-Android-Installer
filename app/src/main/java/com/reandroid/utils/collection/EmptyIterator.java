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

import java.util.ListIterator;

public class EmptyIterator<T> implements ListIterator<T>, EmptyItem {
    public EmptyIterator(){
    }
    @Override
    public boolean hasNext() {
        return false;
    }
    @Override
    public T next() {
        throw new IllegalArgumentException("Empty iterator");
    }
    @Override
    public boolean hasPrevious() {
        return false;
    }
    @Override
    public T previous() {
        throw new IllegalArgumentException("Empty iterator");
    }
    @Override
    public int nextIndex() {
        return -1;
    }
    @Override
    public int previousIndex() {
        return -1;
    }
    @Override
    public void remove() {
        throw new IllegalArgumentException("Empty iterator");
    }

    @Override
    public void set(T t) {
        throw new IllegalArgumentException("Empty iterator");
    }

    @Override
    public void add(T t) {
        throw new IllegalArgumentException("Empty iterator");
    }

    public static <T1> EmptyIterator<T1> of(){
        return (EmptyIterator<T1>) INS;
    }

    private static final EmptyIterator<?> INS = new EmptyIterator<>();
}

