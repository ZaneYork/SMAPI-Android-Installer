package com.reandroid.utils.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class EmptySet<T> implements Set<T>, EmptyItem {
    public EmptySet(){
    }
    @Override
    public int size() {
        return 0;
    }
    @Override
    public boolean isEmpty() {
        return true;
    }
    @Override
    public boolean contains(Object o) {
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        return EmptyIterator.of();
    }
    @Override
    public Object[] toArray() {
        return new Object[0];
    }
    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return t1s;
    }
    @Override
    public boolean add(T t) {
        throw new IllegalArgumentException("Empty set");
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
    public boolean addAll(Collection<? extends T> collection) {
        throw new IllegalArgumentException("Empty set");
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException("Empty set");
    }
    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }
    @Override
    public void clear() {
    }

    public static <T1> EmptySet<T1> of(){
        return (EmptySet<T1>) INS;
    }

    public static final EmptySet<?> INS = new EmptySet<>();
}
