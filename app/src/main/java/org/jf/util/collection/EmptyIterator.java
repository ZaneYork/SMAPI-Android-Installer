package org.jf.util.collection;

import java.util.ListIterator;

public class EmptyIterator<T> implements ListIterator<T>, Empty {
    public EmptyIterator(){
    }
    @Override
    public boolean hasNext() {
        return false;
    }
    @Override
    public T next() {
        throw new IllegalArgumentException("Empty iterator");
    }
    @Override
    public boolean hasPrevious() {
        return false;
    }
    @Override
    public T previous() {
        throw new IllegalArgumentException("Empty iterator");
    }
    @Override
    public int nextIndex() {
        return -1;
    }
    @Override
    public int previousIndex() {
        return -1;
    }
    @Override
    public void remove() {
        throw new IllegalArgumentException("Empty iterator");
    }

    @Override
    public void set(T t) {
        throw new IllegalArgumentException("Empty iterator");
    }

    @Override
    public void add(T t) {
        throw new IllegalArgumentException("Empty iterator");
    }

    public static <T1> EmptyIterator<T1> of(){
        return (EmptyIterator<T1>) INS;
    }

    public static final EmptyIterator<?> INS = new EmptyIterator<>();
}
