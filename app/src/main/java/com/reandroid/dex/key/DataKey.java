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

import com.reandroid.dex.data.DataItem;
import com.reandroid.utils.CompareUtil;

public class DataKey<T extends DataItem> implements Key{
    private final T item;

    public DataKey(T item){
        this.item = item;
    }

    public T getItem() {
        return item;
    }
    private int getOffset(){
        return getItem().getOffset();
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null || getClass() != obj.getClass()){
            return 1;
        }
        DataKey<?> key = (DataKey<?>) obj;
        if(getItem().equals(key.getItem())){
            return 0;
        }
        int i = CompareUtil.compareUnsigned(getItem().getIndex(),
                key.getItem().getIndex());
        if(i != 0) {
            return i;
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
        DataKey<?> key = (DataKey<?>) obj;
        return item.equals(key.item);
    }
    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public String toString() {
        return getOffset() + ": {" + getItem() +"}";
    }
}
