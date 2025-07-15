/*
 *  Copyright (C) 2023 github.com/REAndroid
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
package org.jf.dexlib2.dexbacked.model;

import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.dexbacked.raw.TypeIdItem;
import org.jf.util.collection.ArrayIterator;
import org.jf.util.collection.ComputingList;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class DexTypeStringSection extends AbstractList<DexTypeString> implements Function<DexTypeString, String> {
    private final DexTypeString[] array;
    private final List<String> stringList;
    public DexTypeStringSection(int typeCount){
        this.array = new DexTypeString[typeCount];
        this.stringList = new ComputingList<>(this, this);
    }
    public List<String> getStringList() {
        return stringList;
    }
    @Override
    public DexTypeString get(int i) {
        if(i < 0){
            return null;
        }
        return array[i];
    }
    @Override
    public int size() {
        return array.length;
    }

    @Override
    public Iterator<DexTypeString> iterator(){
        return new ArrayIterator<>(array);
    }

    public void load(DexBuffer dexBuffer, int startOffset, DexStringSection stringSection){
        DexTypeString[] array = this.array;
        int count = array.length;
        int itemSize = TypeIdItem.ITEM_SIZE;
        for(int i = 0; i<count; i++){
            int typeOffset = startOffset + i * itemSize;
            int stringIndex = dexBuffer.readSmallUint(typeOffset);
            DexString dexString = stringSection.get(stringIndex);
            array[i] = new DexTypeString(dexString, i);
        }
    }

    @Override
    public String apply(DexTypeString dexTypeString) {
        if(dexTypeString != null){
            return dexTypeString.getValue();
        }
        return null;
    }
}
