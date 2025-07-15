package org.jf.util.collection;

import java.util.Iterator;

public class ConcatIterables<T> implements Iterable<T>{
    private final Iterable<? extends Iterable<? extends T>> iterables;
    public ConcatIterables(Iterable<? extends Iterable<? extends T>> iterables){
        this.iterables = iterables;
    }
    @Override
    public Iterator<T> iterator() {
        Iterable<? extends Iterable<? extends T>> iterables = this.iterables;
        if(iterables == null){
            return EmptyIterator.of();
        }
        return new ConcatIterators<>(iterables.iterator(), false);
    }
}
