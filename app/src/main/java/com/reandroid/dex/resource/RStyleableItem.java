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

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexField;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public abstract class RStyleableItem {

    private final DexField dexField;

    public RStyleableItem(DexField dexField) {
        this.dexField = dexField;
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
                && dexField.isStatic();
    }
    public void appendJava(SmaliWriter writer) throws IOException {
        DexField dexField = getDexField();
        writer.appendModifiers(dexField.getAccessFlags());
        TypeKey typeKey = TypeKey.create(dexField.getKey().getType().getTypeName());
        writer.append(typeKey.getSourceName());
        writer.append(' ');
        writer.append(dexField.getName());
        writer.append(" = ");
        appendJavaValue(writer);
        writer.append(';');
    }
    public abstract void appendJavaValue(SmaliWriter writer) throws IOException;
    public void serialize(TableBlock tableBlock, XmlSerializer serializer) throws IOException {

    }
    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        RStyleableItem other = (RStyleableItem) obj;
        return ObjectsUtil.equals(getName(), other.getName());
    }
    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getName());
    }
    @Override
    public String toString() {
        return getName();
    }
}
