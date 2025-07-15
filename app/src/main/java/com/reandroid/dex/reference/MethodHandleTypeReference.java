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

import com.reandroid.arsc.item.BlockItem;
import com.reandroid.dex.base.IndirectShort;
import com.reandroid.dex.common.MethodHandleType;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;

public class MethodHandleTypeReference extends IndirectShort {

    public MethodHandleTypeReference(BlockItem blockItem, int offset) {
        super(blockItem, offset);
    }

    public MethodHandleType getHandleType() {
        return MethodHandleType.valueOf(get());
    }
    public void setMethodHandleType(MethodHandleType handleType) {
        set(handleType.type());
    }

    @SuppressWarnings("unchecked")
    public SectionType<IdItem> getSectionType() {
        MethodHandleType type = getHandleType();
        SectionType<?> sectionType;
        if (type != null) {
            if (type.isField()) {
                sectionType = SectionType.FIELD_ID;
            } else {
                sectionType = SectionType.METHOD_ID;
            }
        } else {
            //TODO: should throw ?
            sectionType = null;
        }
        return (SectionType<IdItem>) sectionType;
    }

    @Override
    public String toString() {
        MethodHandleType type = getHandleType();
        if (type == null) {
            return "unknown: " + get();
        }
        return type.name();
    }
}
