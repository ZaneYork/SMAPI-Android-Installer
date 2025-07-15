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
package com.reandroid.dex.refactor;

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.*;

public abstract class Rename<T extends Key, R extends Key> {

    private final Set<KeyPair<T, R>> keyPairSet;

    public Rename(){
        this.keyPairSet = new HashSet<>();
    }

    public void add(T search, R replace) {
        add(new KeyPair<>(search, replace));
    }
    public void add(KeyPair<T, R> keyPair){
        addToSet(keyPair);
    }
    public void addAll(Collection<KeyPair<T, R>> keyPairs){
        this.addAll(keyPairs.iterator());
    }
    public void addAll(Iterator<KeyPair<T, R>> iterator){
        while (iterator.hasNext()){
            addToSet(iterator.next());
        }
    }
    private void addToSet(KeyPair<T, R> keyPair){
        if(keyPair != null && keyPair.isValid()){
            this.keyPairSet.add(keyPair);
        }
    }
    public int size(){
        return keyPairSet.size();
    }
    public List<KeyPair<T, R>> sortedList(){
        List<KeyPair<T, R>> results = new ArrayCollection<>(keyPairSet);
        results.sort(CompareUtil.getComparableComparator());
        return results;
    }

    public abstract int apply(DexClassRepository classRepository);

    public Set<KeyPair<T, R>> getKeyPairSet() {
        return keyPairSet;
    }

    @Override
    public String toString() {
        return StringsUtil.join(sortedList(), '\n');
    }
}
