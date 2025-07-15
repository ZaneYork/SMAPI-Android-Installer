package org.jf.util.collection;

import java.util.Iterator;
import java.util.function.Function;

public class ComputeIterable <E, T> implements Iterable<T> {
    private final Iterable<? extends E> iterable;
    private final Function<? super E, ? extends T> function;
    public ComputeIterable(Iterable<? extends E> iterable, Function<? super E, ? extends T> function){
        this.iterable = iterable;
        this.function = function;
    }
    @Override
    public Iterator<T> iterator() {
        return new ComputeIterator<E, T>(iterable.iterator(), function);
    }
}
