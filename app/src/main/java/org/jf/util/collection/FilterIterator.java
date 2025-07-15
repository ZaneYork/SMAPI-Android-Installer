package org.jf.util.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilterIterator<E, T extends E> implements Iterator<T>{
    private final Iterator<? extends T> iterator;
    private T mNext;
    private boolean mNextComputed;
    private final Predicate<E> filter;
    public FilterIterator(Iterator<? extends T> iterator, Predicate<E> filter){
        this.iterator = iterator;
        this.filter = filter;
    }

    @Override
    public boolean hasNext() {
        getNext();
        return mNextComputed;
    }
    @Override
    public T next() {
        T item = getNext();
        if(!mNextComputed){
            throw new NoSuchElementException();
        }
        mNextComputed = false;
        mNext = null;
        return item;
    }
    private T getNext(){
        if(!mNextComputed) {
            while (iterator.hasNext()) {
                T item = iterator.next();
                if (testAll(item)) {
                    mNext = item;
                    mNextComputed = true;
                    break;
                }
            }
        }
        return mNext;
    }
    private boolean testAll(T item){
        if(filter == null){
            return true;
        }
        return filter.test(item);
    }
}
