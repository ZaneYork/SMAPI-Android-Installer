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
import java.util.NoSuchElementException;

public class CombiningIterator<T, E extends T> implements Iterator<T> {
    private Iterator<? extends T> iterator1;
    private Iterator<? extends T> iterator2;
    private Iterator<? extends T> iterator3;
    private Iterator<? extends T> iterator4;
    private Iterator<Iterator<E>> iteratorIterator;

    private Iterator<? extends T> mCurrentIterator;
    private T mCurrentItem;


    public CombiningIterator(Iterator<? extends T> iterator1, Iterator<? extends T> iterator2, Iterator<? extends T> iterator3, Iterator<Iterator<E>> iteratorIterator){
        this.iterator1 = iterator1;
        this.iterator2 = iterator2;
        this.iterator3 = iterator3;
        this.iteratorIterator = iteratorIterator;
    }
    public CombiningIterator(Iterator<? extends T> iterator1, Iterator<? extends T> iterator2, Iterator<? extends T> iterator3){
        this(iterator1, iterator2, iterator3, null);
    }
    public CombiningIterator(Iterator<? extends T> iterator1, Iterator<? extends T> iterator2){
        this(iterator1, iterator2, null, null);
    }
    public CombiningIterator(Iterator<? extends T> iterator1, Iterator<Iterator<E>> iteratorIterator, Void ignored){
        this(iterator1, null, null, iteratorIterator);
    }
    public CombiningIterator(Iterator<Iterator<E>> iteratorIterator){
        this(null, null, null, iteratorIterator);
    }

    @Override
    public boolean hasNext() {
        return getCurrentItem() != null;
    }
    @Override
    public T next() {
        T item = getCurrentItem();
        if(item == null){
            throw new NoSuchElementException();
        }
        this.mCurrentItem = null;
        return item;
    }
    private T getCurrentItem(){
        T item = mCurrentItem;
        if(item == null){
            item = readNext();
            this.mCurrentItem = item;
        }
        return item;
    }
    private T readNext(){
        Iterator<? extends T> iterator = this.iterator1;
        if(iterator != null){
            while (iterator.hasNext()){
                T item = iterator.next();
                if(item != null){
                    return item;
                }
            }
            this.iterator1 = null;
        }
        iterator = this.iterator2;
        if(iterator != null){
            while (iterator.hasNext()){
                T item = iterator.next();
                if(item != null){
                    return item;
                }
            }
            this.iterator2 = null;
        }
        iterator = this.iterator3;
        if(iterator != null){
            while (iterator.hasNext()){
                T item = iterator.next();
                if(item != null){
                    return item;
                }
            }
            this.iterator3 = null;
        }
        iterator = this.iterator4;
        if(iterator != null){
            while (iterator.hasNext()){
                T item = iterator.next();
                if(item != null){
                    return item;
                }
            }
            this.iterator4 = null;
        }
        return readCurrentIterator();
    }
    private T readCurrentIterator(){
        Iterator<? extends T> iterator = getCurrentIterator();
        while (iterator != null){
            while (iterator.hasNext()){
                T item = iterator.next();
                if(item != null){
                    return item;
                }
            }
            this.mCurrentIterator = null;
            iterator = getCurrentIterator();
        }
        return null;
    }

    private Iterator<? extends T> getCurrentIterator() {
        if(mCurrentIterator != null){
            return mCurrentIterator;
        }
        Iterator<Iterator<E>> iteratorIterator = this.iteratorIterator;
        if(iteratorIterator == null){
            return null;
        }
        Iterator<? extends T> iterator = null;
        while (iteratorIterator.hasNext() && iterator == null){
            iterator = iteratorIterator.next();
        }
        mCurrentIterator = iterator;
        return iterator;
    }


    public static<T1, E1 extends T1> Iterator<T1> of(Iterator<T1> iterator1, Iterator<Iterator<E1>> iteratorIterator){
        if(!iteratorIterator.hasNext()){
            return iterator1;
        }
        CombiningIterator<T1, E1> iterator = new CombiningIterator<>(iterator1, null);
        iterator.iteratorIterator = iteratorIterator;
        return iterator;
    }
    public static<T1, E1 extends T1> Iterator<T1> of(T1 item, Iterator<Iterator<E1>> iteratorIterator){
        if(!iteratorIterator.hasNext()){
            return SingleIterator.of(item);
        }
        CombiningIterator<T1, E1> iterator = new CombiningIterator<>(null, null);
        iterator.mCurrentItem = item;
        iterator.iteratorIterator = iteratorIterator;
        return iterator;
    }
    @SuppressWarnings("unchecked")
    public static<T1> Iterator<T1> two(Iterator<? extends T1> iterator1, Iterator<? extends T1> iterator2){
        if(!iterator1.hasNext()){
            return (Iterator<T1>) iterator2;
        }
        if(!iterator2.hasNext()){
            return (Iterator<T1>) iterator1;
        }
        return new CombiningIterator<>(iterator1, iterator2);
    }
    public static<T1> Iterator<T1> three(Iterator<? extends T1> iterator1, Iterator<? extends T1> iterator2, Iterator<? extends T1> iterator3){
        return new CombiningIterator<>(iterator1, iterator2, iterator3);
    }
    public static<T1> Iterator<T1> four(Iterator<? extends T1> iterator1, Iterator<? extends T1> iterator2, Iterator<? extends T1> iterator3, Iterator<? extends T1> iterator4){
        CombiningIterator<T1, T1> iterator = new CombiningIterator<>(iterator1, iterator2, iterator3);
        iterator.iterator4 = iterator4;
        return iterator;
    }
    @SuppressWarnings("unchecked")
    public static<T1> Iterator<T1> singleOne(T1 item, Iterator<? extends T1> iterator1){
        if(item == null){
            return (Iterator<T1>) iterator1;
        }
        CombiningIterator<T1, T1> iterator = new CombiningIterator<>(iterator1, null);
        iterator.mCurrentItem = item;
        return iterator;
    }
    @SuppressWarnings("unchecked")
    public static<T1> Iterator<T1> singleTwo(T1 item, Iterator<? extends T1> iterator1, Iterator<? extends T1> iterator2){
        if(item == null){
            return (Iterator<T1>) iterator1;
        }
        CombiningIterator<T1, T1> iterator = new CombiningIterator<>(iterator1, iterator2);
        iterator.mCurrentItem = item;
        return iterator;
    }
    @SuppressWarnings("unchecked")
    public static<T1> Iterator<T1> singleThree(T1 item, Iterator<? extends T1> iterator1, Iterator<? extends T1> iterator2, Iterator<? extends T1> iterator3){
        if(item == null){
            return (Iterator<T1>) iterator1;
        }
        CombiningIterator<T1, T1> iterator = new CombiningIterator<>(iterator1, iterator2, iterator3);
        iterator.mCurrentItem = item;
        return iterator;
    }
}
