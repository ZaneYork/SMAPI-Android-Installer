/*
 * Copyright 2012, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.util;

import org.jf.util.collection.ArraySet;

import java.util.*;
import java.util.function.Predicate;

public class CollectionUtils {
    public static <T> int listHashCode( Iterable<T> iterable) {
        int hashCode = 1;
        for (T item: iterable) {
            hashCode = hashCode*31 + item.hashCode();
        }
        return hashCode;
    }

    public static <T> int lastIndexOf( Iterable<T> iterable,  Predicate<? super T> predicate) {
        int index = 0;
        int lastMatchingIndex = -1;
        for (T item: iterable) {
            if (predicate.test(item)) {
                lastMatchingIndex = index;
            }
            index++;
        }
        return lastMatchingIndex;
    }

    public static <T extends Comparable<? super T>> int compareAsList( Collection<? extends T> list1,
                                                                       Collection<? extends T> list2) {
        int res = Integer.compare(list1.size(), list2.size());
        if (res != 0) return res;
        Iterator<? extends T> elements2 = list2.iterator();
        for (T element1: list1) {
            res = element1.compareTo(elements2.next());
            if (res != 0) return res;
        }
        return 0;
    }

    public static <T> int compareAsIterable( Comparator<? super T> comparator,
                                             Iterable<? extends T> it1,
                                             Iterable<? extends T> it2) {
        Iterator<? extends T> elements2 = it2.iterator();
        for (T element1: it1) {
            T element2;
            if (!elements2.hasNext()) {
                return 1;
            }
            element2 = elements2.next();
            int res = comparator.compare(element1, element2);
            if (res != 0) return res;
        }
        if (elements2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static <T extends Comparable<? super T>> int compareAsIterable( Iterable<? extends T> it1,
                                                                           Iterable<? extends T> it2) {
        Iterator<? extends T> elements2 = it2.iterator();
        for (T element1: it1) {
            T element2;
            if (!elements2.hasNext()) {
                return 1;
            }
            element2 = elements2.next();
            int res = element1.compareTo(element2);
            if (res != 0) return res;
        }
        if (elements2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public static <T> int compareAsList( Comparator<? super T> elementComparator,
                                         Collection<? extends T> list1,
                                         Collection<? extends T> list2) {
        int res = Integer.compare(list1.size(), list2.size());
        if (res != 0) return res;
        Iterator<? extends T> elements2 = list2.iterator();
        for (T element1: list1) {
            res = elementComparator.compare(element1, elements2.next());
            if (res != 0) return res;
        }
        return 0;
    }


    public static <T> Comparator<Collection<? extends T>> listComparator(
             final Comparator<? super T> elementComparator) {
        return new Comparator<Collection<? extends T>>() {
            @Override
            public int compare(Collection<? extends T> list1, Collection<? extends T> list2) {
                return compareAsList(elementComparator, list1, list2);
            }
        };
    }

    public static <T> boolean isNaturalSortedSet( Iterable<? extends T> it) {
        if (it instanceof ArraySet) {
            return true;
        }
        return false;
    }

    public static <T> boolean isSortedSet( Comparator<? extends T> elementComparator,
                                           Iterable<? extends T> it) {
        if (it instanceof ArraySet) {
            return true;
        }
        return false;
    }


    private static <T> Set<? extends T> toNaturalSortedSet( Collection<? extends T> collection) {
        if (isNaturalSortedSet(collection)) {
            return (SortedSet<? extends T>)collection;
        }
        return ArraySet.sortedCopy(collection.iterator());
    }


    private static <T> Set<? extends T> toSortedSet( Comparator<? super T> elementComparator,
                                                           Collection<? extends T> collection) {
        if (collection instanceof SortedSet) {
            SortedSet<? extends T> sortedSet = (SortedSet<? extends T>)collection;
            Comparator<?> comparator = sortedSet.comparator();
            if (comparator != null && comparator.equals(elementComparator)) {
                return sortedSet;
            }
        }
        ArraySet<? extends T> arraySet = new ArraySet<>(collection);
        arraySet.sort(elementComparator);
        return arraySet;
    }


    public static <T> Comparator<Collection<? extends T>> setComparator(
             final Comparator<? super T> elementComparator) {
        return new Comparator<Collection<? extends T>>() {
            @Override
            public int compare(Collection<? extends T> list1, Collection<? extends T> list2) {
                return compareAsSet(elementComparator, list1, list2);
            }
        };
    }

    public static <T extends Comparable<T>> int compareAsSet( Collection<? extends T> set1,
                                                              Collection<? extends T> set2) {
        int res = Integer.compare(set1.size(), set2.size());
        if (res != 0) return res;

        Iterator<? extends T> elements2 = set2.iterator();
        for (T element1: set1) {
            res = element1.compareTo(elements2.next());
            if (res != 0) return res;
        }
        return 0;
    }

    public static <T> int compareAsSet( Comparator<? super T> elementComparator,
                                        Collection<? extends T> list1,
                                        Collection<? extends T> list2) {
        int res = Integer.compare(list1.size(), list2.size());
        if (res != 0) return res;

        Set<? extends T> set1 = toSortedSet(elementComparator, list1);
        Set<? extends T> set2 = toSortedSet(elementComparator, list2);

        Iterator<? extends T> elements2 = set2.iterator();
        for (T element1: set1) {
            res = elementComparator.compare(element1, elements2.next());
            if (res != 0) return res;
        }
        return 0;
    }
}
