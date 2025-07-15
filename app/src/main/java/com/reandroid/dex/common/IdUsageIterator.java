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

package com.reandroid.dex.common;

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public interface IdUsageIterator {
    Iterator<IdItem> usedIds();
    default Iterator<Key> usedKeys(){
        return new IterableIterator<IdItem, Key>(usedIds()) {
            @SuppressWarnings("unchecked")
            @Override
            public Iterator<Key> iterator(IdItem element) {
                return (Iterator<Key>) element.getKey().mentionedKeys();
            }
        };
    }
    default boolean uses(IdItem idItem){
        return CollectionUtil.contains(usedIds(), idItem);
    }
    default boolean uses(Key key){
        return CollectionUtil.contains(usedKeys(), key);
    }
}
