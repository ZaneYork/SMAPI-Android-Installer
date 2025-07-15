package org.jf.util.collection;

import java.util.Iterator;

public class CombiningIterable<T> implements Iterable<T>{
    private final Iterable<? extends T> iterable1;
    private final Iterable<? extends T> iterable2;

    public CombiningIterable(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2){
        this.iterable1 = iterable1;
        this.iterable2 = iterable2;
    }
    @Override
    public Iterator<T> iterator() {
        Iterator<? extends T> iterator;
        if(iterable1 != null){
            iterator = iterable1.iterator();
        }else {
            iterator = null;
        }
        return new CombiningIterator<T>(iterator, iterable2);
    }
}
