package org.jf.util.collection;

import java.util.ListIterator;

public class ArrayListIterator<T> extends ArrayIterator<T> implements ListIterator<T> {
    public ArrayListIterator(T[] elements) {
        super(elements);
    }
    public ArrayListIterator(T[] elements, int startIndex) {
        this(elements);
        setIndex(startIndex);
    }
    @Override
    public boolean hasPrevious() {
        return getIndex() > 0;
    }
    @Override
    public T previous() {
        int index = getIndex() - 1;
        T item = get(index);
        setIndex(index);
        return item;
    }
    @Override
    public int nextIndex() {
        return setIndex(getIndex() + 1);
    }

    @Override
    public int previousIndex() {
        return setIndex(getIndex() - 1);
    }

    @Override
    public void remove() {

    }

    @Override
    public void set(T t) {

    }

    @Override
    public void add(T t) {

    }
}
