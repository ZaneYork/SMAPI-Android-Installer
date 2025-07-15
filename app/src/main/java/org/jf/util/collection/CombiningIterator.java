package org.jf.util.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CombiningIterator<T> implements Iterator<T> {
    private final Iterator<? extends T> iterator1;
    private final Iterator<? extends T> iterator2;
    private final Iterable<? extends T> iterable2;
    private boolean mFirstFinished;
    private Iterator<? extends T> mSecond;

    public CombiningIterator(Iterator<? extends T> iterator1, Iterator<? extends T> iterator2){
        this.iterator1 = iterator1;
        this.iterator2 = iterator2;
        this.iterable2 = null;
    }
    public CombiningIterator(Iterator<? extends T> iterator1, Iterable<? extends T> iterable2){
        this.iterator1 = iterator1;
        this.iterator2 = null;
        this.iterable2 = iterable2;
    }

    @Override
    public boolean hasNext() {
        if(!mFirstFinished){
            if(iterator1 != null && iterator1.hasNext()){
                return true;
            }
            mFirstFinished = true;
        }
        Iterator<? extends T> second = this.getSecond();
        return second != null && second.hasNext();
    }
    @Override
    public T next() {
        Iterator<? extends T> current;
        if(mFirstFinished){
            current = getSecond();
        }else {
            current = iterator1;
        }
        if(current == null){
            throw new NoSuchElementException();
        }
        return current.next();
    }

    private Iterator<? extends T> getSecond() {
        if(mSecond != null){
            return mSecond;
        }
        Iterator<? extends T> second = iterator2;
        if(second == null){
            if(iterable2 != null){
                second = iterable2.iterator();
            }else {
                second = EmptyIterator.of();
            }
        }
        mSecond = second;
        return second;
    }
}