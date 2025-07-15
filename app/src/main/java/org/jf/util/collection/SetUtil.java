package org.jf.util.collection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetUtil {

    public static <T> Set<T> nullToEmptySet(Set<T> set) {
        if (set == null) {
            return of();
        }
        return set;
    }
    public static <E> HashSet<E> of() {
        return newHashSet((E[]) null);
    }
    public static <E> HashSet<E> of(E... elements) {
        return newHashSet(elements);
    }
    public static <E> HashSet<E> newHashSet(E... elements) {
        int capacity = 0;
        if(elements != null){
            capacity = elements.length;
        }
        if(capacity == 0){
            capacity = 1;
        }
        HashSet<E> hashSet = new HashSet<>(capacity);
        if(elements != null){
            Collections.addAll(hashSet, elements);
        }
        return hashSet;
    }
}
