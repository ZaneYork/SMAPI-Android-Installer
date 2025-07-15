package org.jf.util.collection;


import java.util.*;
import java.util.function.Function;

public class ListUtil {

    public static <E, T> List<T> transform(Collection<E> collection, Function<? super E, ? extends T> function, int limit) {
        List<T> results = new ArrayList<>(collection.size());
        for(E input : collection){
            if(results.size() >= limit){
                break;
            }
            results.add(function.apply(input));
        }
        return results;
    }
    public static <E, T> List<T> transform(Collection<E> collection, Function<? super E, ? extends T> function) {
        List<T> results = new ArrayList<>(collection.size());
        for(E input : collection){
            results.add(function.apply(input));
        }
        return results;
    }
    public static <E> ArrayList<E> of() {
        return of((E[]) null);
    }
    public static <E> ArrayList<E> of(E... elements) {
        return newArrayList(elements);
    }
    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        if(elements == null){
            return new ArrayList<>(1);
        }
        if(elements instanceof Collection){
            return newArrayList((Collection<? extends E>) elements);
        }
        return newArrayList(elements.iterator());
    }
    public static <E> ArrayList<E> newArrayList(Collection<? extends E> elements) {
        if(elements == null){
            return new ArrayList<>(1);
        }
        return new ArrayList<>(elements);
    }
    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        if(elements == null){
            return new ArrayList<>(1);
        }
        ArrayList<E> list = new ArrayList<>();
        while (elements.hasNext()){
            list.add(elements.next());
        }
        return list;
    }
    public static<T> List<T> reverse(List<T> list) {
        List<T> results = new ArrayList<>(list.size());
        for(int i = list.size() - 1; i >= 0; i--){
            results.add(list.get(i));
        }
        return results;
    }
    public static<T> void addAll(List<T> destination, Iterable<T> iterable) {
        if(iterable == null || iterable == destination){
            return;
        }
        addAll(destination, iterable.iterator());
    }
    public static<T> void addAll(List<T> destination, Iterator<T> iterator) {
        while (iterator.hasNext()){
            destination.add(iterator.next());
        }
    }
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>(1);
    }
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = 0;
        if(elements != null){
            capacity = elements.length;
        }
        if(capacity == 0){
            capacity = 1;
        }
        ArrayList<E> list = new ArrayList<>(capacity);
        if(elements != null){
            Collections.addAll(list, elements);
        }
        return list;
    }
    public static<T> List<T> sortedCopy(Collection<T> collection, Comparator<? super T> comparator){
        List<T> result = new ArrayList<>(collection);
        result.sort(comparator);
        return result;
    }
    public static <T> List<T> nullToEmptySet(List<T> list) {
        if (list == null) {
            return of((T[])null);
        }
        return list;
    }
    public static <T> List<T> nullToEmptyList(List<T> list) {
        if (list == null) {
            return ListUtil.<T>of((T[]) null);
        }
        return list;
    }
    public static<T> Set<T> immutableSetOf(Iterable<? extends T> iterable){
        if(iterable == null){
            return ArraySet.of();
        }
        return ArraySet.copyOf(iterable.iterator());
    }
    public static<T> List<T> copyOf(Collection<T> collection){
        if(collection == null){
            return new ArrayList<>(1);
        }
        return new ArrayList<>(collection);
    }
    public static<T> List<T> copyOf(Iterable<? extends T> iterable){
        if(iterable == null){
            return new ArrayList<>(1);
        }
        if(iterable instanceof Collection){
            Collection<T> collection = (Collection<T>) iterable;
            return copyOf(collection);
        }
        return copyOf(iterable.iterator());
    }
    public static<T> List<T> copyOf(Iterator<? extends T> iterator){
        List<T> result = new ArrayList<>();
        while (iterator.hasNext()){
            result.add(iterator.next());
        }
        return result;
    }
    public static<T extends Comparable<?>> Collection<T> sortedCopy(Iterator<? extends T> iterator){
        return ArraySet.sortedCopy(iterator);
    }
    public static<T extends Comparable<?>> List<T> sortedCopy(Collection<? extends T> collection){
        return sortedCopy(collection, false);
    }
    public static<T extends Comparable<?>> List<T> sortedCopy(Collection<? extends T> collection, boolean invert){
        List<T> results = new ArrayList<>(collection);
        if(invert){
            results.sort(INVERT_SORT);
        }else {
            results.sort(NATURAL_SORT);
        }
        return results;
    }
    public static<T> Comparator<T> toStringComparator(){
        return (Comparator<T>) TO_STRING_COMPARATOR;
    }

    private static final NaturalSort NATURAL_SORT = new NaturalSort<>(false);
    private static final NaturalSort INVERT_SORT = new NaturalSort<>(false);

    private static class NaturalSort<T extends Comparable<T>> implements Comparator<T> {
        private final boolean isInvert;
        NaturalSort(boolean isInvert){
            this.isInvert = isInvert;
        }
        @Override
        public int compare(T t1, T t2) {
            if(isInvert){
                return t2.compareTo(t1);
            }
            return t1.compareTo(t2);
        }
    }
    private static final Comparator<?> TO_STRING_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(Object obj1, Object obj2) {
            return String.valueOf(obj1).compareTo(String.valueOf(obj2));
        }
    };
}
