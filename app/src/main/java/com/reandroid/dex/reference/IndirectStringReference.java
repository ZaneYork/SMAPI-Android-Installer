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

import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;

public class IndirectStringReference extends IdItemIndirectReference<StringId>{

    public IndirectStringReference(SectionItem blockItem, int offset, int usage) {
        super(SectionType.STRING_ID, blockItem, offset, usage);
    }

    public String getString(){
        StringId stringId = getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public void setString(String text) {
        setItem(StringKey.create(text));
    }

    public static boolean equals(IndirectStringReference reference1, IndirectStringReference reference2) {
        if(reference1 == reference2){
            return true;
        }
        if(reference1 == null){
            return false;
        }
        return StringId.equals(reference1.getItem(), reference2.getItem());
    }
}
