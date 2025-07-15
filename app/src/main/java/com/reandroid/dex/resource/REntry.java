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
package com.reandroid.dex.resource;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexField;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class REntry implements IntegerReference {

    private final DexField dexField;

    public REntry(DexField dexField) {
        this.dexField = dexField;
    }

    @Override
    public int get() {
        IntegerReference reference = getDexField().getStaticIntegerValue();
        if(reference != null) {
            return reference.get();
        }
        return 0;
    }
    @Override
    public void set(int value) {
        IntegerReference reference = getDexField().getStaticIntegerValue();
        if(reference != null) {
            reference.set(value);
        }
    }
    public String getName() {
        return getKey().getName();
    }
    public FieldKey getKey() {
        return getDexField().getKey();
    }
    public DexField getDexField() {
        return dexField;
    }
    public boolean isValid() {
        DexField dexField = getDexField();
        return dexField.isPublic()
                && dexField.isStatic()
                && PackageBlock.isResourceId(get());
    }
    public void appendJava(SmaliWriter writer) throws IOException {
        DexField dexField = getDexField();
        writer.appendModifiers(dexField.getAccessFlags());
        TypeKey typeKey = TypeKey.create(dexField.getKey().getType().getTypeName());
        writer.append(typeKey.getSourceName());
        writer.append(' ');
        writer.append(dexField.getName());
        writer.append(" = ");
        writer.appendHex(get());
        writer.append(';');
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof REntry)) {
            return false;
        }
        REntry other = (REntry) obj;
        return getKey().equals(other.getKey());
    }
    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
    @Override
    public String toString() {
        return getName() + " = " + HexUtil.toHex8(get());
    }
}
