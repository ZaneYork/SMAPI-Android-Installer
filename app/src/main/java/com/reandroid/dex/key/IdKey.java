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
package com.reandroid.dex.key;

import com.reandroid.dex.id.IdItem;

public class IdKey<T extends IdItem> implements Key{

    private final T item;

    public IdKey(T item){
        this.item = item;
    }

    public T getItem() {
        return item;
    }
    private int getIndex(){
        return getItem().getIndex();
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null || getClass() != obj.getClass()){
            return 1;
        }
        IdKey<?> key = (IdKey<?>) obj;
        T item1 = getItem();
        IdItem item2 = key.getItem();
        if(item1.getClass() == item2.getClass()){
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IdKey<?> key = (IdKey<?>) obj;
        return item.equals(key.item);
    }
    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public String toString() {
        return getIndex() + ": {" + getItem() +"}";
    }
}
