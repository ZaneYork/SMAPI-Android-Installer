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

public class MergingIterator<T> implements Iterator<T> {
    private final Iterator<Iterator<T>> iteratorIterator;
    private Iterator<T> mCurrent;

    public MergingIterator(Iterator<Iterator<T>> iteratorIterator){
        this.iteratorIterator = iteratorIterator;
    }

    @Override
    public boolean hasNext() {
        Iterator<T> current = getCurrent();
        return current != null && current.hasNext();
    }
    @Override
    public T next() {
        Iterator<T> current = getCurrent();
        if(current == null){
            throw new NoSuchElementException();
        }
        return current.next();
    }
    private Iterator<T> getCurrent(){
        Iterator<T> current = mCurrent;
        while (current == null || !current.hasNext()){
            if(!iteratorIterator.hasNext()){
                return null;
            }
            current = iteratorIterator.next();
        }
        mCurrent = current;
        return current;
    }
}
