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
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.base.ParallelIntegerPair;
import com.reandroid.dex.base.ParallelReference;

public class SpecialSection<T extends SpecialItem> extends Section<T> {

    public SpecialSection(IntegerReference offset, SectionType<T> sectionType) {
        this(IntegerPair.of(
                new NumberIntegerReference(),
                new ParallelReference(offset)),
                sectionType);
    }
    public SpecialSection(IntegerPair countAndOffset, SectionType<T> sectionType) {
        super(sectionType, new SpecialSectionArray<>(
                new ParallelIntegerPair(countAndOffset),
                sectionType));
    }

    @Override
    int clearUnused() {
        return 0;
    }
    @Override
    void clearUsageTypes() {
    }

    @Override
    void onRefreshed(int position){
        updateItemOffsets(position);
    }
    private void updateItemOffsets(int position){
        SpecialSectionArray<T> array = getItemArray();
        position = array.updatePositionedItemOffsets(position);
        updateNextSection(position);
    }
    @Override
    void alignSection(DexPositionAlign positionAlign, int position){
        positionAlign.setAlignment(4);
        positionAlign.align(position);
    }
    public ParallelIntegerPair getCountAndOffset(){
        return getItemArray().getCountAndOffset();
    }

    @Override
    public SpecialSectionArray<T> getItemArray() {
        return (SpecialSectionArray<T>) super.getItemArray();
    }
}
