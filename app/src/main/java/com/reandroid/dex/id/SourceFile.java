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
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class SourceFile extends IndirectStringReference implements SmaliRegion {

    SourceFile(ClassId classId, int offset) {
        super(classId, offset, UsageMarker.USAGE_SOURCE);
    }

    @Override
    public ClassId getBlockItem() {
        return (ClassId) super.getBlockItem();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        StringId stringId = getItem();
        if(stringId == null){
            return;
        }
        writer.newLine();
        getSmaliDirective().append(writer);
        stringId.append(writer);
    }

    @Override
    protected int getItemIndex(StringId item) {
        if(item == null){
            return -1;
        }
        return item.getIdx();
    }

    @Override
    protected StringId pullItem(int i) {
        if(i == -1){
            return null;
        }
        return super.pullItem(i);
    }

    @Override
    public void checkNonNullItem(StringId item) {

    }
    @Override
    public void checkNonNullItem(StringId item, int i) {

    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.SOURCE;
    }
    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }

    public static final String SourceFile = ObjectsUtil.of("SourceFile");
}
