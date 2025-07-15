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

public class ExpandIterator<T> implements Iterator<T> {

    private final Iterator<? extends Iterable<? extends T>> iterator;
    private Iterator<? extends T> mCurrent;

    public ExpandIterator(Iterator<? extends Iterable<? extends T>> iterator){
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return getCurrent() != null;
    }

    @Override
    public T next() {
        return mCurrent.next();
    }
    private Iterator<? extends T> getCurrent(){
        if(mCurrent == null || !mCurrent.hasNext()) {
            mCurrent = null;
            while (iterator.hasNext()) {
                Iterable<? extends T> iterable = iterator.next();
                if(iterable == null){
                    continue;
                }
                Iterator<? extends T> item = iterable.iterator();
                if (item.hasNext()) {
                    mCurrent = item;
                    break;
                }
            }
        }
        return mCurrent;
    }
    public static<T1> Iterator<T1> of(Iterator<? extends Iterable<? extends T1>> iterator){
        if(!iterator.hasNext()){
            return EmptyIterator.of();
        }
        return new ExpandIterator<>(iterator);
    }
}
