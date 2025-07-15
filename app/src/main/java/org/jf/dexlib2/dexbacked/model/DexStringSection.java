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
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.util.collection.ArrayIterator;
import org.jf.util.collection.ArrayListIterator;
import org.jf.util.collection.ComputingList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

public class DexStringSection implements List<DexString>, Function<DexString, String> {
    private final DexString[] array;
    private final List<String> stringList;
    public DexStringSection(int size){
        this.array = new DexString[size];
        this.stringList = new ComputingList<>(this, this);
    }
    public void load(DexBuffer dexBuffer, int offset, int item_size){
        DexString[] array = this.array;
        int count = array.length;
        DexReader<?> reader = dexBuffer.readerAt(offset);
        for(int i = 0; i < count; i++){
            int stringOffset = offset + i * item_size;
            int stringDataOffset = dexBuffer.readSmallUint(stringOffset);
            reader.setOffset(stringDataOffset);
            int utf16Length = reader.readSmallUleb128();
            String value = reader.readString(utf16Length);
            array[i] = new DexString(this, i, value);
        }
    }
    public List<String> getStringList() {
        return stringList;
    }
    @Override
    public int size() {
        return array.length;
    }
    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }
    @Override
    public boolean contains(Object obj) {
        if(obj instanceof DexString){
            return contains((DexString) obj);
        }
        return false;
    }
    public boolean contains(DexString dexString) {
        if(dexString == null){
            return false;
        }
        if(dexString.getStringSection() != this){
            return false;
        }
        int index = dexString.getIndex();
        if(index < 0 || index >= size()) {
            return false;
        }
        return get(index) == dexString;
    }

    @Override
    public Iterator<DexString> iterator() {
        return new ArrayIterator<>(array);
    }

    @Override
    public Object[] toArray() {
        return array.clone();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return ts;
    }

    @Override
    public boolean add(DexString dexString) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends DexString> collection) {
        return false;
    }

    @Override
    public boolean addAll(int i, Collection<? extends DexString> collection) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }
    @Override
    public void clear() {

    }
    @Override
    public DexString get(int i) {
        if(i < 0){
            return null;
        }
        return array[i];
    }

    @Override
    public DexString set(int i, DexString dexString) {
        return null;
    }

    @Override
    public void add(int i, DexString dexString) {
        array[i] = dexString;
    }
    @Override
    public DexString remove(int i) {
        return null;
    }
    @Override
    public int indexOf(Object obj) {
        if(obj instanceof DexString){
            return ((DexString) obj).getIndex();
        }
        return -1;
    }
    @Override
    public int lastIndexOf(Object obj) {
        if(obj instanceof DexString){
            return ((DexString) obj).getIndex();
        }
        return -1;
    }

    @Override
    public ListIterator<DexString> listIterator() {
        return new ArrayListIterator<>(array);
    }

    @Override
    public ListIterator<DexString> listIterator(int i) {
        return new ArrayListIterator<>(array, i);
    }

    @Override
    public List<DexString> subList(int i, int i1) {
        return null;
    }
    @Override
    public String apply(DexString dexString) {
        if(dexString != null){
            return dexString.getValue();
        }
        return null;
    }
}
