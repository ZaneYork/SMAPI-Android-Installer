package org.jf.util.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ComputeIterator<E, T> implements Iterator<T>{
    private final Iterator<? extends E> iterator;
    private final Function<? super E, ? extends T> function;

    public ComputeIterator(Iterator<? extends E> iterator,
                           Function<? super E, ? extends T> function){
        this.iterator = iterator;
        this.function = function;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    @Override
    public T next() {
        return function.apply(iterator.next());
    }
}