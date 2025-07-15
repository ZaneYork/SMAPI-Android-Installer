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

import java.util.Iterator;
import java.util.List;

public abstract class ReversedIterator<T> implements Iterator<T> {

    private final int mEndIndex;
    private int index;
    private T mNext;

    public ReversedIterator(int start, int length) {
        int end = start - length;
        if(end < 0) {
            end = 0;
        }
        this.mEndIndex = end;
        this.index = start;
    }

    public abstract T get(int i);

    @Override
    public boolean hasNext() {
        return computeNext() != null;
    }

    @Override
    public T next() {
        T item = computeNext();
        this.mNext = null;
        return item;
    }
    private T computeNext() {
        T next = this.mNext;
        while (next == null && this.index >= this.mEndIndex) {
            next = testAll(this.get(this.index));
            this.index --;
        }
        this.mNext = next;
        return next;
    }
    private T testAll(T item) {
        return item;
    }

    public static<T1> Iterator<T1> of(List<? extends T1> elements) {
        int size = elements.size();
        if(size == 0){
            return EmptyIterator.of();
        }
        return of(elements, size - 1, size);
    }
    public static<T1> Iterator<T1> of(List<? extends T1> elements, int start){
        return of(elements, start, start + 1);
    }
    public static<T1> Iterator<T1> of(List<? extends T1> elements, int start, int length){
        if(elements.isEmpty()) {
            return EmptyIterator.of();
        }
        return new ReversedIterator<T1>(start, length){
            @Override
            public T1 get(int i) {
                return elements.get(i);
            }
        };
    }


    public static<T1> Iterator<T1> of(Object[] elements) {
        if(elements == null || elements.length == 0){
            return EmptyIterator.of();
        }
        int size = elements.length;
        return of(elements, size - 1, size);
    }
    public static<T1> Iterator<T1> of(Object[] elements, int start) {
        return of(elements, start, start + 1);
    }
    public static<T1> Iterator<T1> of(Object[] elements, int start, int length) {
        if(elements == null || elements.length == 0){
            return EmptyIterator.of();
        }
        return new ReversedIterator<T1>(start, length) {
            @SuppressWarnings("unchecked")
            @Override
            public T1 get(int i) {
                return (T1) elements[i];
            }
        };
    }
}
