package org.jf.util.collection;


import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;



public class Iterables {

    public static <T> T getLast(List<T> list) {
        if(list.size() > 0){
            return list.get(list.size() -1);
        }
        return null;
    }
    public static <T> T getLast(Iterator<T> iterator) {
        T result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }
    public static <T> T getFirst(Iterable<? extends T> iterable, T defaultValue) {
        return getNext(iterable.iterator(), defaultValue);
    }
    public static <T> T getNext(Iterator<? extends T> iterator, T defaultValue) {
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }
    public static <E, T extends E> Iterable<T> filter(Iterable<? extends T> iterable, Predicate<E> filter) {
        return new FilterIterable<E, T>(iterable, filter);
    }
    public static <T> Iterator<T> filter(Iterator<? extends T> iterator, Predicate<T> filter) {
        return new FilterIterator<>(iterator, filter);
    }
    public static <T> Iterator<T> forArray(final T... array) {
        return new ArrayIterator<>(array);
    }
    public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate){
        if(iterable == null){
            return false;
        }
        Iterator<T> itr = iterable.iterator();
        while (itr.hasNext()){
            T item = itr.next();
            if (predicate == null){
                if(item != null){
                    return true;
                }
            }else if (predicate.test(item)){
                return true;
            }
        }
        return false;
    }
    public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b) {
        return new CombiningIterator<T>(a, b);
    }
    public static <T> Iterable<T> concat(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2) {
        return new CombiningIterable<>(iterable1, iterable2);
    }
    public static <T> Iterable<T> concat(Iterable<? extends Iterable<? extends T>> iterables){
        return new ConcatIterables<>(iterables);
    }

    public static <E, T> Iterable<T> transform(Iterable<E> iterable, Function<? super E, ? extends T> function) {
        return new ComputeIterable<E, T>(iterable, function);
    }
    public static <E, T> Iterator<T> transform(Iterator<E> iterable, Function<? super E, ? extends T> function) {
        return new ComputeIterator<E, T>(iterable, function);
    }

    public static boolean isEmpty(Iterable<?> iterable) {
        if(iterable == null){
            return true;
        }
        return isEmpty(iterable.iterator());
    }
    public static boolean isEmpty(Iterator<?> iterator) {
        if(iterator == null){
            return true;
        }
        return !iterator.hasNext();
    }
    public static int size(Iterable<?> iterable) {
        return size(iterable.iterator());
    }
    public static int size(Iterator<?> iterator) {
        int result = 0;
        while (iterator.hasNext()){
            iterator.next();
            result++;
        }
        return result;
    }
}
