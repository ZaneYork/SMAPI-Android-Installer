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

public class SingleIterator<T> implements Iterator<T> {
    private T mItem;
    public SingleIterator(T item){
        this.mItem = item;
    }
    @Override
    public boolean hasNext() {
        return this.mItem != null;
    }
    @Override
    public T next() {
        T item = this.mItem;
        this.mItem = null;
        return item;
    }

    public static<T1> Iterator<T1> of(T1 item){
        if(item == null){
            return EmptyIterator.of();
        }
        return new SingleIterator<>(item);
    }
}
