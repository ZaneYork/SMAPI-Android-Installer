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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedList;
import com.reandroid.dex.key.Key;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.util.Iterator;
import java.util.function.Predicate;

public class DirectoryMap<DEFINITION extends DefIndex, VALUE extends DataItem>
        extends CountedList<DirectoryEntry<DEFINITION, VALUE>>
        implements Iterable<DirectoryEntry<DEFINITION, VALUE>> {

    public DirectoryMap(IntegerReference itemCount, Creator<DirectoryEntry<DEFINITION, VALUE>> creator) {
        super(itemCount, creator);
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    public boolean sort(){
        return super.sort(CompareUtil.getComparableComparator());
    }
    public void add(DEFINITION definition, VALUE value){
        if(!contains(definition, value)){
            DirectoryEntry<DEFINITION, VALUE> entry = createNext();
            entry.set(definition, value);
        }
    }
    public boolean contains(DEFINITION definition){
        return getEntries(definition).hasNext();
    }
    public boolean contains(DEFINITION definition, VALUE value){
        Iterator<DirectoryEntry<DEFINITION, VALUE>> iterator = getEntries(definition);
        while (iterator.hasNext()){
            if(iterator.next().equalsValue(value)){
                return true;
            }
        }
        return false;
    }
    public boolean contains(DEFINITION definition, Key valueKey){
        Iterator<DirectoryEntry<DEFINITION, VALUE>> iterator = getEntries(definition);
        while (iterator.hasNext()){
            if(iterator.next().matchesValue(valueKey)){
                return true;
            }
        }
        return false;
    }
    public boolean contains(Key definitionKey, Key valueKey){
        Iterator<DirectoryEntry<DEFINITION, VALUE>> iterator = getEntries(definitionKey);
        while (iterator.hasNext()){
            if(iterator.next().matchesValue(valueKey)){
                return true;
            }
        }
        return false;
    }
    public void remove(DEFINITION definition) {
        super.removeIf(entry -> entry.equalsDefIndex(definition));
    }
    public void remove(DEFINITION definition, Predicate<VALUE> filter) {
        super.removeIf(entry -> entry.equalsDefIndex(definition) && filter.test(entry.getValue()));
    }
    public void link(DEFINITION definition){
        for(DirectoryEntry<DEFINITION, VALUE> entry : this){
            entry.link(definition);
        }
    }
    public Iterator<VALUE> getValues(int definitionIndex){
        return ComputeIterator.of(getEntries(definitionIndex), DirectoryEntry::getValue);
    }
    public Iterator<DirectoryEntry<DEFINITION, VALUE>> getEntries(int definitionIndex){
        return FilterIterator.of(iterator(), entry -> entry.equalsDefIndex(definitionIndex));
    }
    public Iterator<VALUE> getValues(DEFINITION definition){
        return ComputeIterator.of(getEntries(definition), DirectoryEntry::getValue);
    }
    public Iterator<VALUE> getValues(){
        return ComputeIterator.of(iterator(), DirectoryEntry::getValue);
    }
    public Iterator<DirectoryEntry<DEFINITION, VALUE>> getEntries(DEFINITION definition){
        return FilterIterator.of(iterator(), entry -> entry.equalsDefIndex(definition));
    }
    public Iterator<DirectoryEntry<DEFINITION, VALUE>> getEntries(Key definitionKey){
        return FilterIterator.of(iterator(), entry -> entry.matchesDefinition(definitionKey));
    }

    @Override
    public int countBytes() {
        return getCount() * DirectoryEntry.SIZE;
    }

    public void merge(DirectoryMap<DEFINITION, VALUE> directoryMap){
        if(directoryMap == this){
            return;
        }
        int size = directoryMap.size();
        ensureCapacity(size);
        for(int i = 0; i < size; i++){
            DirectoryEntry<DEFINITION, VALUE> coming = directoryMap.get(i);
            DirectoryEntry<DEFINITION, VALUE> item = createNext();
            item.merge(coming);
        }
    }

}
