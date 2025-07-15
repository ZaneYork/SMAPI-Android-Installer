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

import java.util.Iterator;

public interface IdDefinition<T extends IdItem> {
    T getId();
    int getAccessFlagsValue();
    Iterator<? extends Modifier> getAccessFlags();
    default Iterator<? extends Modifier> getModifiers(){
        return getAccessFlags();
    }
    void setAccessFlagsValue(int value);
    default void addAccessFlag(AccessFlag flag) {
        int current = getAccessFlagsValue();
        int value = flag.getValue();
        if((value & 0x7) != 0){
            current = current & ~0x7;
        }
        setAccessFlagsValue(current | value);
    }
    default void removeAccessFlag(AccessFlag flag) {
        setAccessFlagsValue(getAccessFlagsValue() & ~flag.getValue());
    }
}
