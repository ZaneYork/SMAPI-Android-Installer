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
package com.reandroid.dex.ins;

public interface ConstNumberLong extends ConstNumber{
    long getLong();
    void set(long value);
    @Override
    default int get() {
        long l = getLong();
        int i = (int) l;
        if((i & 0xffffffffL) != l){
            return 0;
        }
        return i;
    }
    @Override
    default void set(int value) {
        set((long) value);
    }
}
