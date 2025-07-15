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

import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexField;
import com.reandroid.dex.model.DexMethod;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;

public class RType extends RTypeItem implements Iterable<REntry> {

    public RType(DexClass dexClass) {
        super(dexClass);
    }

    @Override
    public Iterator<REntry> iterator() {
        return ComputeIterator.of(getDexClass().getStaticFields(), this::create);
    }

    public boolean isEmpty() {
        return !iterator().hasNext();
    }
    REntry create(DexField dexField) {
        return new REntry(dexField);
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
        boolean hasEntries = false;
        for(REntry entry : this) {
            if(!entry.isValid()) {
                return false;
            }
            hasEntries = true;
        }
        return hasEntries;
    }
    @Override
    public void appendJavaEntries(SmaliWriter writer) throws IOException {
        for(REntry entry : this) {
            writer.newLine();
            entry.appendJava(writer);
        }
    }
}
