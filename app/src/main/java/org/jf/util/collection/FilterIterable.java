package org.jf.util.collection;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilterIterable<E, T extends E> implements Iterable<T>{
    private final Iterable<? extends T> iterable;
    private final Predicate<E> filter;
    public FilterIterable(Iterable<? extends T> iterable, Predicate<E> filter){
        this.iterable = iterable;
        this.filter = filter;
    }
    @Override
    public Iterator<T> iterator() {
        return new FilterIterator<>(iterable.iterator(), filter);
    }
}
