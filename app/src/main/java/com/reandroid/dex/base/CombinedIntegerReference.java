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
package com.reandroid.dex.base;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.Iterator;

public class CombinedIntegerReference implements IntegerReference,
        Iterable<IntegerReference>, BlockRefresh {

    private final ArrayCollection<IntegerReference> references;

    public CombinedIntegerReference(){
        this.references = new ArrayCollection<>();
    }

    public void add(IntegerReference reference){
        if(reference == this){
            throw new RuntimeException("Cyclic add reference");
        }
        if(reference == null || references.containsFast(reference)){
            return;
        }
        references.add(reference);
    }
    public void remove(IntegerReference reference){
        references.remove(reference);
    }
    public void clear(){
        references.clear();
    }
    public int size(){
        return references.size();
    }
    public boolean isEmpty(){
        return references.isEmpty();
    }
    public boolean contains(IntegerReference reference){
        return references.contains(reference);
    }
    @Override
    public Iterator<IntegerReference> iterator() {
        return references.iterator();
    }
    @Override
    public int get() {
        IntegerReference first = references.getFirst();
        if(first != null){
            return first.get();
        }
        return 0;
    }
    @Override
    public void set(int value) {
        for(IntegerReference reference : this){
            reference.set(value);
        }
    }
    @Override
    public void refresh() {
        set(get());
    }
    @Override
    public String toString() {
        return "size = " + size() + ", value = " + get();
    }
}
