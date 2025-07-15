package org.jf.util.collection;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public class ComputingList<E, T>  extends AbstractList<T> {
    private final List<? extends E> list;
    private final Function<E, T> function;
    public ComputingList(List<? extends E> list, Function<E, T> function){
        this.list = list;
        this.function = function;
    }
    @Override
    public T get(int i) {
        return function.apply(list.get(i));
    }
    @Override
    public int size() {
        return list.size();
    }
}
