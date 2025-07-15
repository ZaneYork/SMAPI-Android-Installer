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
package com.reandroid.utils.collection;

import java.util.*;
import java.util.function.Predicate;

public class CollectionUtil {

    public static void walk(Iterator<?> iterator){
        if(iterator == null){
            return;
        }
        while (iterator.hasNext()){
            iterator.next();
        }
    }
    public static<T> List<T> toUniqueList(Iterator<? extends T> iterator) {
        return new ArrayCollection<>(toHashSet(iterator));
    }
    @SafeVarargs
    public static<T> HashSet<T> newHashSet(T ... elements) {
        if(elements == null || elements.length == 0) {
            return new HashSet<>();
        }
        int length = elements.length;
        HashSet<T> results = new HashSet<>(length);
        for (int i = 0; i < length; i ++){
            results.add(elements[i]);
        }
        return results;
    }
    public static<T> HashSet<T> toHashSet(Iterator<? extends T> iterator) {
        HashSet<T> results = new HashSet<>();
        while (iterator.hasNext()){
            T item = iterator.next();
            results.add(item);
        }
        return results;
    }

    public static<T extends Comparable<T>> void sort(List<T> list){
        list.sort(getComparator());
    }
    public static<T> T getLast(Iterator<T> iterator){
        if(iterator == null){
            return null;
        }
        T result = null;
        while (iterator.hasNext()){
            result = iterator.next();
        }
        return result;
    }
    public static boolean contains(Iterator<?> iterator, Object obj){
        if(iterator == null){
            return false;
        }
        while (iterator.hasNext()){
            if(obj.equals(iterator.next())){
                return true;
            }
        }
        return false;
    }
    public static<T> T getFirst(Iterator<T> iterator){
        if(iterator == null || !iterator.hasNext()){
            return null;
        }
        return iterator.next();
    }
    public static<T> T getSingle(Iterator<T> iterator){
        T result = null;
        if(iterator != null && iterator.hasNext()){
            result = iterator.next();
            if(iterator.hasNext()){
                result = null;
            }
        }
        return result;
    }
    public static int count(Iterable<?> iterable){
        if(iterable == null || iterable instanceof EmptyItem){
            return 0;
        }
        if(iterable instanceof Collection){
            return ((Collection<?>)iterable).size();
        }
        if(iterable instanceof SizedItem){
            if(((SizedItem)iterable).size() == 0){
                return 0;
            }
        }
        Iterator<?> iterator = iterable.iterator();
        int result = 0;
        while (iterator.hasNext()){
            iterator.next();
            result ++;
        }
        return result;
    }
    public static int count(Iterator<?> iterator){
        if(iterator == null || iterator instanceof EmptyItem){
            return 0;
        }
        if(iterator instanceof SizedItem){
            if(((SizedItem)iterator).size() == 0){
                return 0;
            }
        }
        int result = 0;
        while (iterator.hasNext()){
            iterator.next();
            result ++;
        }
        return result;
    }
    public static boolean isEmpty(Iterable<?> iterable){
        if(iterable == null || iterable instanceof EmptyItem){
            return true;
        }
        if(iterable instanceof Collection){
            return ((Collection<?>)iterable).isEmpty();
        }
        if(iterable instanceof SizedItem){
            if(((SizedItem)iterable).size() == 0){
                return true;
            }
        }
        return !iterable.iterator().hasNext();
    }
    public static boolean isEmpty(Iterator<?> iterator){
        if(iterator == null || iterator instanceof EmptyItem){
            return true;
        }
        if(iterator instanceof SizedItem){
            if(((SizedItem)iterator).size() == 0){
                return true;
            }
        }
        return !iterator.hasNext();
    }
    public static<T> Collection<T> collect(Iterator<? extends T> iterator){
        boolean hasNext = iterator.hasNext();
        if(!hasNext){
            return ArrayCollection.empty();
        }
        ArrayCollection<T> results = new ArrayCollection<>();
        results.addAll(iterator);
        if(results.size() > 1000){
            results.trimToSize();
        }
        return results;
    }
    @SafeVarargs
    public static<T> List<T> asList(T ... elements) {
        return new ArrayCollection<>(elements);
    }
    public static<T> List<T> toList(Iterator<? extends T> iterator){
        boolean hasNext = iterator.hasNext();
        if(!hasNext){
            return EmptyList.of();
        }
        ArrayCollection<T> results = new ArrayCollection<>(2);
        while (hasNext){
            results.add(iterator.next());
            hasNext = iterator.hasNext();
        }
        if(results.size() > 1000){
            results.trimToSize();
        }
        return results;
    }
    public static<T> Iterator<T> newIterator(Collection<? extends T> collection) {
        int size = collection.size();
        if(size == 0) {
            return EmptyIterator.of();
        }
        return ArrayIterator.of(collection.toArray());
    }
    public static<T> Iterator<T> copyOf(Iterator<? extends T> iterator){
        boolean hasNext = iterator.hasNext();
        if(!hasNext){
            return EmptyIterator.of();
        }
        List<T> results = toList(iterator);
        return results.iterator();
    }

    public static void shuffle(List<?> list) {
        if(list.isEmpty()){
            return;
        }
        int random = Long.toString(System.currentTimeMillis()).hashCode();
        shuffle(random, list);
    }
    @SuppressWarnings("all")
    public static void shuffle(int random, List<?> list) {
        if(list.isEmpty()){
            return;
        }
        if(list instanceof ArrayCollection) {
            shuffle(random, (ArrayCollection<?>) list);
        }else {
            ArrayCollection collection = new ArrayCollection<>(list);
            list.clear();
            shuffle(random, collection);
            list.addAll(collection);
        }
    }
    private static void shuffle(int random, ArrayCollection<?> list) {
        int size = list.size();
        for(int i = 0; i < size; i++) {
            int i2 = random % size;
            if(i2 < 0) {
                i2 = -i2;
            }
            list.swap(i, i2);
            random = random * 31 + size + 1;
        }
    }
    @SuppressWarnings("unchecked")
    public static<T> Predicate<T> getAcceptAll(){
        return (Predicate<T>) ACCEPT_ALL;
    }
    @SuppressWarnings("unchecked")
    public static<T> Predicate<T> getRejectAll(){
        return (Predicate<T>) REJECT_ALL;
    }

    public static<T> Predicate<? super T> orFilter(Predicate<? super T> filter1, Predicate<? super T> filter2){
        if(filter1 == null || filter1 == getRejectAll()){
            return filter2;
        }
        if(filter2 == null || filter2 == getRejectAll()){
            return filter1;
        }
        return t -> (filter1.test(t) || filter2.test(t));
    }
    public static<T> Predicate<T> andFilter(Predicate<T> filter1, Predicate<T> filter2){
        if(filter1 == null || filter1 == getAcceptAll()){
            return filter2;
        }
        if(filter2 == null || filter2 == getAcceptAll()){
            return filter1;
        }
        return t -> (filter1.test(t) && filter2.test(t));
    }
    public static<T> Predicate<T> negateFilter(Predicate<T> filter){
        return t -> !filter.test(t);
    }
    public static<T> Predicate<T> diffFilter(Predicate<T> filter1, Predicate<T> filter2){
        return t -> (filter1.test(t) != filter2.test(t));
    }
    public static<T> Predicate<T> equalFilter(Predicate<T> filter1, Predicate<T> filter2){
        return t -> (filter1.test(t) == filter2.test(t));
    }
    @SuppressWarnings("unchecked")
    public static<T extends Comparable<T>> Comparator<T> getComparator(){
        return (Comparator<T>) COMPARABLE_COMPARATOR;
    }
    @SuppressWarnings("all")
    private static final Comparator<? extends Comparable> COMPARABLE_COMPARATOR = new Comparator<Comparable<?>>() {
        @Override
        public int compare(Comparable c1, Comparable c2) {
            return c1.compareTo(c2);
        }
    };

    private static final Predicate<?> ACCEPT_ALL = (Predicate<Object>) o -> true;
    private static final Predicate<?> REJECT_ALL = (Predicate<Object>) o -> false;
}
