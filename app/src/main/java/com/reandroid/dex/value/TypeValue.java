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
package com.reandroid.dex.value;

import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;

public class TypeValue extends SectionIdValue<TypeId> {

    public TypeValue() {
        super(SectionType.TYPE_ID, DexValueType.TYPE);
    }

    @Override
    public TypeKey getKey() {
        return (TypeKey) super.getKey();
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.TYPE;
    }
    @Override
    public TypeKey getDataTypeKey() {
        return TypeKey.CLASS;
    }
}
