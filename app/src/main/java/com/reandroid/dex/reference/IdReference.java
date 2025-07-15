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
package com.reandroid.dex.reference;

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.id.IdItem;

public interface IdReference<T extends IdItem> extends DexReference<T> {

    @Override
    default void editInternal(Block user){
        T item = getItem();
        if(item != null){
            item.editInternal(user);
        }
    }
    default void checkNonNullItem(T item) {
        if(item == null){
            throw new NullPointerException("Null item for: " + getSectionType().getName());
        }
    }
    default void checkNonNullItem(T item, int i) {
        if(item == null){
            throw new NullPointerException("Null item " + "(" + i + ")" + getSectionType().getName());
        }
    }
}
