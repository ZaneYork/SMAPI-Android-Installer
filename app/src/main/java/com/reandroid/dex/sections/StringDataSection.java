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

import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.pool.StringDataPool;

public class StringDataSection extends DataSection<StringData> {

    public StringDataSection(IntegerPair countAndOffset, SectionType<StringData> sectionType) {
        super(sectionType, new StringDataArray(countAndOffset, sectionType.getCreator()));
    }

    @Override
    public StringDataPool getPool() {
        return (StringDataPool) super.getPool();
    }
    @Override
    StringDataPool createPool() {
        return new StringDataPool(this);
    }
}
