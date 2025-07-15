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

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;

public class StringUle128Reference extends Ule128IdItemReference<StringId> implements
        BlockRefresh, Comparable<StringUle128Reference> {

    public StringUle128Reference(int usageType) {
        super(SectionType.STRING_ID, usageType);
    }

    public String getString(){
        StringId stringId = getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public void setString(String text){
        setItem(StringKey.create(text));
    }

    @Override
    public int compareTo(StringUle128Reference reference) {
        if(reference == null){
            return -1;
        }
        return CompareUtil.compare(getItem(), reference.getItem());
    }
}
