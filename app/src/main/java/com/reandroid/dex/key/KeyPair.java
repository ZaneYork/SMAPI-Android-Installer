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
package com.reandroid.dex.key;

import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class KeyPair<T1 extends Key, T2 extends Key> implements Comparable<KeyPair<Key, Key>>{

    private T1 first;
    private T2 second;

    public KeyPair(T1 first, T2 second){
        this.first = first;
        this.second = second;
    }
    public KeyPair(T1 first){
        this(first, null);
    }
    public KeyPair(){
        this(null, null);
    }

    public T1 getFirst() {
        return first;
    }
    public void setFirst(T1 first) {
        this.first = first;
    }
    public T2 getSecond() {
        return second;
    }
    public void setSecond(T2 second) {
        this.second = second;
    }

    public KeyPair<T2, T1> flip(){
        return new KeyPair<>(getSecond(), getFirst());
    }
    public boolean isValid(){
        T1 t1 = getFirst();
        if(t1 == null){
            return false;
        }
        T2 t2 = getSecond();
        if(t2 == null){
            return false;
        }
        return !t1.equals(t2);
    }

    @Override
    public int compareTo(KeyPair<Key, Key> pair) {
        if(pair == null){
            return -1;
        }
        Key key1 = this.getFirst();
        Key key2 = pair.getFirst();
        if(key1 == null){
            if(key2 == null){
                return 0;
            }
            return 1;
        }
        if(key2 == null){
            return -1;
        }
        int i = key1.getDeclaring().compareInnerFirst(key2.getDeclaring());
        if(i == 0){
            i = CompareUtil.compare(key1, key2);
        }
        return i;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyPair)) {
            return false;
        }
        KeyPair<?, ?> keyPair = (KeyPair<?, ?>) obj;
        return Objects.equals(getFirst(), keyPair.getFirst());
    }

    @Override
    public int hashCode() {
        T1 first = getFirst();
        if(first != null){
            return first.hashCode();
        }
        return 0;
    }
    @Override
    public String toString() {
        return getFirst() + "=" + getSecond();
    }

    public static<E1 extends Key, E2 extends Key> Iterator<KeyPair<E2, E1>> flip(Iterator<KeyPair<E1, E2>> iterator){
        return ComputeIterator.of(iterator, KeyPair::flip);
    }
    public static<E1 extends Key, E2 extends Key> List<KeyPair<E2, E1>> flip(Collection<KeyPair<E1, E2>> list){
        ArrayCollection<KeyPair<E2, E1>> results = new ArrayCollection<>(list.size());
        results.addAll(KeyPair.flip(list.iterator()));
        return results;
    }
}
