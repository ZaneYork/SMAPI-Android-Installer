package org.jf.util.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractIterator<T> implements Iterator<T> {
    private T mNext;
    private boolean mFinished;
    public AbstractIterator(){

    }
    public final T endOfData() {
        mFinished = true;
        return null;
    }
    public abstract T computeNext();
    @Override
    public boolean hasNext() {
        return getNext() != null;
    }

    @Override
    public T next() {
        T next = getNext();
        if(next == null){
            throw new NoSuchElementException();
        }
        mNext = null;
        return next;
    }
    private T getNext(){
        T next = mNext;
        if(next != null || mFinished){
            return next;
        }
        next = computeNext();
        mNext = next;
        return next;
    }
}
