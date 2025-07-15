package org.jf.util.collection;

import java.util.*;

public class ArraySet<T> implements Set<T>, Comparator<T>{
    private final ArrayList<T> items;
    private Comparator<? super T> comparator;
    private int mHashCode;
    private int mHashCodeStamp = -1;
    public ArraySet(){
        this.items = new ArrayList<>();
    }
    public ArraySet(int initialCapacity){
        this.items = new ArrayList<>(initialCapacity);
    }
    public ArraySet(Collection<? extends T> collection){
        this.items = new ArrayList<>(collection.size());
        this.addAll(collection);
    }

    public Comparator<? super T> comparator() {
        return comparator;
    }
    public void setComparator(Comparator<? super T> comparator) {
        this.comparator = comparator;
        if(comparator != null){
            sort(comparator);
        }
    }

    public void trimToSize() {
        items.trimToSize();
    }
    public boolean addAll(Iterable<? extends T> iterable) {
        if(iterable == null){
            return false;
        }
        if(iterable instanceof Collection){
            return addAll((Collection<? extends T>)iterable);
        }
        return addAll(iterable.iterator());
    }
    public boolean addAll(Iterator<? extends T> iterator) {
        if(iterator == null || !iterator.hasNext()){
            return false;
        }
        while (iterator.hasNext()){
            this.add(iterator.next());
        }
        trimToSize();
        return true;
    }
    public boolean addAll(T... elements) {
        if(elements == null || elements.length == 0){
            return false;
        }
        synchronized (items){
            ArrayList<T> items = this.items;
            items.ensureCapacity(items.size() + elements.length);
            boolean result = false;
            for(T item : elements){
                if(item == null){
                    continue;
                }
                if(items.contains(item)){
                    continue;
                }
                items.add(item);
                result = true;
            }
            return result;
        }
    }
    @Override
    public int size() {
        return items.size();
    }
    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
    @Override
    public boolean contains(Object obj) {
        if(obj == null){
            return false;
        }
        return items.contains(obj);
    }
    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
    @Override
    public Object[] toArray() {
        return items.toArray();
    }
    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return items.toArray(t1s);
    }
    @Override
    public boolean add(T item) {
        synchronized (item){
            if(item == null){
                return false;
            }
            if(items.contains(item)){
                return !items.contains(item);
            }
            items.add(item);
            return true;
        }
    }
    private T getByHash(int hash){
        for(T item:items){
            if(item.hashCode() == hash){
                return item;
            }
        }
        return null;
    }
    @Override
    public boolean remove(Object obj) {
        if(obj == null){
            return false;
        }
        synchronized (items){
            return items.remove(obj);
        }
    }
    @Override
    public boolean containsAll(Collection<?> collection) {
        return items.containsAll(collection);
    }
    @Override
    public boolean addAll(Collection<? extends T> collection) {
        if(collection == null || collection.size() == 0 || collection == this){
            return false;
        }
        synchronized (items){
            ArrayList<T> items = this.items;
            items.ensureCapacity(items.size() + collection.size());
            boolean result = false;
            for(T item : collection){
                if(item == null){
                    continue;
                }
                if(items.contains(item)){
                    continue;
                }
                items.add(item);
                result = true;
            }
            if(result){
                trimToSize();
            }
            return result;
        }
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        return items.retainAll(collection);
    }
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = items.removeAll(collection);
        trimToSize();
        return result;
    }
    @Override
    public void clear() {
        items.clear();
        trimToSize();
    }
    @Override
    public int compare(T t1, T t2) {
        if(t1 == t2){
            return 0;
        }
        if(t1 instanceof Comparable && t2 instanceof Comparable){
            Comparable comparable1 = (Comparable) t1;
            return comparable1.compareTo(t2);
        }
        return 0;
    }
    public ArraySet<T> sort(){
        return sort(this);
    }
    public ArraySet<T> sort(Comparator<? super T> comparator){
        items.sort(comparator);
        return this;
    }
    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(!(obj instanceof Set)){
            return false;
        }
        Set<?> set = (Set<?>) obj;
        int size = this.size();
        if(set.size() != size){
            return false;
        }
        if(size == 0){
            return true;
        }
        if(getHashCode(set) != hashCode()){
            return false;
        }
        Iterator<?> iterator1 = this.iterator();
        Iterator<?> iterator2 = set.iterator();
        while (iterator1.hasNext()){
            if(!iterator2.hasNext()){
                return false;
            }
            if(!iterator1.next().equals(iterator2.next())){
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode(){
        if(mHashCodeStamp != items.size()){
            mHashCode = ArraySet.hashCode(this);
            mHashCodeStamp = items.size();
        }
        return mHashCode;
    }
    private static int getHashCode(Set<?> set) {
        if(set instanceof ArraySet){
            return set.hashCode();
        }
        return hashCode(set);
    }
    public static int hashCode(Set<?> set) {
        int[] hashArray = sortedHashCode(set);
        int hashCode = 1;
        for (int hash : hashArray) {
            hashCode = hashCode * 31 + hash;
        }
        return hashCode;
    }
    private static int[] sortedHashCode(Set<?> set){
        int[] results = new int[set.size()];
        int index = 0;
        for (Object obj : set) {
            if(obj != null){
                results[index] = obj.hashCode();
            }
            index++;
        }
        Arrays.sort(results);
        return results;
    }
    public static <E> ArraySet<E> sortedCopy(Iterator<? extends E> elements) {
        ArraySet<E> arraySet = copyOf(elements);
        arraySet.sort();
        return arraySet;
    }
    public static <E> ArraySet<E> copyOf(Iterable<? extends E> elements) {
        if (elements == null) {
            return EmptySet.of();
        }
        ArraySet<E> arraySet = new ArraySet<>();
        arraySet.addAll(elements);
        return arraySet;
    }
    public static <E> ArraySet<E> copyOf(Collection<? extends E> elements) {
        if (elements == null) {
            return EmptySet.of();
        }
        ArraySet<E> arraySet = new ArraySet<>();
        arraySet.addAll(elements);
        return arraySet;
    }
    public static <E> ArraySet<E> copyOf(Iterator<? extends E> elements) {
        if (!elements.hasNext()) {
            return EmptySet.of();
        }
        ArraySet<E> arraySet = new ArraySet<>();
        arraySet.addAll(elements);
        return arraySet;
    }
    public static <E> ArraySet<E> copyOfElements(E... elements) {
        if (elements == null || elements.length == 0) {
            return EmptySet.of();
        }
        ArraySet<E> arraySet = new ArraySet<>();
        arraySet.addAll(elements);
        return arraySet;
    }
    public static <E> ArraySet<E> of() {
        return EmptySet.of();
    }
    public static <E> ArraySet<E> of(E... elements) {
        if (elements == null || elements.length == 0) {
            return EmptySet.of();
        }
        ArraySet<E> arraySet = new ArraySet<>();
        arraySet.addAll(elements);
        return arraySet;
    }

}
