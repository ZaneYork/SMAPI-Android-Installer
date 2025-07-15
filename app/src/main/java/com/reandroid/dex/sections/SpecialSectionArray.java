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
package com.reandroid.dex.sections;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberIntegerReference;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.base.ParallelIntegerPair;
import com.reandroid.dex.base.ParallelReference;

public class SpecialSectionArray<T extends SpecialItem> extends SectionArray<T> {

    public SpecialSectionArray(IntegerReference offset, SectionType<T> sectionType) {

        this(IntegerPair.of(
                new NumberIntegerReference(),
                new ParallelReference(offset)),
                sectionType);
    }
    public SpecialSectionArray(IntegerPair offset, SectionType<T> sectionType) {
        super(new ParallelIntegerPair(offset),
                sectionType.getCreator());
    }

    @Override
    public boolean add(T item) {
        boolean result = super.add(item);
        updateCount();
        return result;
    }
    @Override
    public void add(int index, T item) {
        super.add(index, item);
        updateCount();
    }

    @Override
    public ParallelIntegerPair getCountAndOffset(){
        return (ParallelIntegerPair) super.getCountAndOffset();
    }

}
