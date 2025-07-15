package org.jf.util.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class EmptyList<T> implements List<T>, Empty {
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
        throw new IllegalArgumentException("Empty list");
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
        throw new IllegalArgumentException("Empty list");
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> collection) {
        throw new IllegalArgumentException("Empty list");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException("Empty list");
    }

    @Override
    public void clear() {
    }
    @Override
    public T get(int i) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public T set(int i, T t) {
        throw new IllegalArgumentException("Empty list");
    }

    @Override
    public void add(int i, T t) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public T remove(int i) {
        throw new IllegalArgumentException("Empty list");
    }
    @Override
    public int indexOf(Object o) {
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return -1;
    }
    @Override
    public ListIterator<T> listIterator() {
        return EmptyIterator.of();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return EmptyIterator.of();
    }
    @Override
    public List<T> subList(int i, int i1) {
        throw new IllegalArgumentException("Empty list");
    }

    public static <T1> EmptyList<T1> of(){
        return (EmptyList<T1>) INS;
    }

    public static final EmptyList<?> INS = new EmptyList<>();
}
