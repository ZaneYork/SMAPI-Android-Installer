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
package com.reandroid.dex.id;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SuperClassId extends IdItemIndirectReference<TypeId> implements SmaliRegion {

    SuperClassId(ClassId classId, int offset) {
        super(SectionType.TYPE_ID, classId, offset, UsageMarker.USAGE_SUPER_CLASS);
    }

    @Override
    public TypeKey getKey() {
        return (TypeKey) super.getKey();
    }

    @Override
    public ClassId getBlockItem() {
        return (ClassId) super.getBlockItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();
        getSmaliDirective().append(writer);
        getItem().append(writer);
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.SUPER;
    }
    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this).trim();
    }
}
