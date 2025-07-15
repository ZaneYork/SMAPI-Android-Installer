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

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexField;
import com.reandroid.dex.model.DexMethod;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.InstanceIterator;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class RStyleableType extends RTypeItem implements Iterable<RStyleableItem> {

    public RStyleableType(DexClass dexClass) {
        super(dexClass);
    }

    @Override
    public Iterator<RStyleableItem> iterator() {
        return ComputeIterator.of(getDexClass().getStaticFields(), this::create);
    }
    public Iterator<RDeclareStyleable> getRDeclareStyleables() {
        return InstanceIterator.of(this.iterator(), RDeclareStyleable.class);
    }

    public boolean isEmpty() {
        return !iterator().hasNext();
    }
    RStyleableItem create(DexField dexField) {
        if(!dexField.isPublic() || !dexField.isStatic()) {
            return null;
        }
        TypeKey typeKey = dexField.getKey().getType();
        if(TypeKey.TYPE_I.equals(typeKey)) {
            return new RStyleableIndex(dexField);
        }
        if(RDeclareStyleable.INT_ARRAY.equals(typeKey)) {
            return new RDeclareStyleable(dexField);
        }
        return null;
    }

    @Override
    public boolean isValid() {
        DexClass dexClass = getDexClass();
        if(dexClass.getInstanceFields().hasNext()) {
            return false;
        }
        Iterator<DexMethod> declaredMethods = dexClass.getDeclaredMethods();
        while (declaredMethods.hasNext()) {
            DexMethod dexMethod = declaredMethods.next();
            if(!dexMethod.isConstructor()) {
                return false;
            }
        }
        return isEntriesValid();
    }


    private boolean isEntriesValid() {
        int attrCount = 0;
        int indexCount = 0;
        for(RStyleableItem item : this) {
            if(!item.isValid()) {
                return false;
            }
            if(item instanceof RStyleableIndex) {
                indexCount ++;
            } else  {
                attrCount ++;
            }
        }
        if(indexCount == 0 || attrCount == 0) {
            return false;
        }
        return CollectionUtil.count(getDexClass().getStaticFields()) == (indexCount + attrCount);
    }

    @Override
    public void appendJavaEntries(SmaliWriter writer) throws IOException {
        for(RStyleableItem item : this) {
            writer.newLine();
            item.appendJava(writer);
        }
    }

    public void serialize(TableBlock tableBlock, XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf8", null);
        XmlHelper.setIndent(serializer, true);
        String tag = "resources";
        serializer.startTag(null, tag);
        for(RStyleableItem item : this) {
            item.serialize(tableBlock, serializer);
        }
        XmlHelper.setIndent(serializer, true);
        serializer.endTag(null, tag);
    }
}
