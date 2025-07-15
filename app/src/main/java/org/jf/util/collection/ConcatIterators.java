package org.jf.util.collection;


import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConcatIterators<T> implements Iterator<T> {
    private final Iterator<? extends Iterator<? extends T>> iterators;
    private final Iterator<? extends Iterable<? extends T>> iterables;

    private Iterator<? extends T> mCurrentIterator;

    public ConcatIterators(Iterator<? extends Iterator<? extends T>> iterators){
        this.iterators = iterators;
        this.iterables = null;
    }
    public ConcatIterators(Iterator<? extends Iterable<? extends T>> iterables, boolean nothing){
        this.iterators = null;
        this.iterables = iterables;
    }
    @Override
    public boolean hasNext() {
        Iterator<? extends T> current = getCurrent();
        return current != null && current.hasNext();
    }

    @Override
    public T next() {
        return getCurrent().next();
    }
    private Iterator<? extends T> getCurrent() {
        if(iterators != null){
            return getCurrentIterator();
        }
        return getCurrentIterables();
    }
    private Iterator<? extends T> getCurrentIterator() {
        Iterator<? extends T> current = mCurrentIterator;
        if(current != null && current.hasNext()){
            return current;
        }
        mCurrentIterator = null;
        if(iterators == null || !iterators.hasNext()){
            return null;
        }
        current = null;
        while (current == null && iterators.hasNext()){
            current = iterators.next();
            if(current != null && !current.hasNext()){
                current = null;
            }
        }
        mCurrentIterator = current;
        return current;
    }
    private Iterator<? extends T> getCurrentIterables() {
        Iterator<? extends T> current = mCurrentIterator;
        if(current != null && current.hasNext()){
            return current;
        }
        mCurrentIterator = null;
        if(iterables == null || !iterables.hasNext()){
            return null;
        }
        current = null;
        while (current == null && iterables.hasNext()){
            Iterable<? extends T> itr = iterables.next();
            if(itr != null){
                current = itr.iterator();
            }
            if(current != null && !current.hasNext()){
                current = null;
            }
        }
        mCurrentIterator = current;
        return current;
    }
}
