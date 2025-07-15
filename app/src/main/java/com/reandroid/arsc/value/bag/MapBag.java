package com.reandroid.arsc.value.bag;

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;

import java.util.*;

public abstract class MapBag<K, V extends BagItem> extends AbstractMap<K, V> implements Bag {
    protected final com.reandroid.arsc.value.Entry entry;
    private int modCount = 0;

    protected MapBag(com.reandroid.arsc.value.Entry entry) {
        this.entry = entry;
    }

    protected ResTableMapEntry getTableEntry() {
        return (ResTableMapEntry) entry.getTableEntry();
    }

    protected ResValueMapArray getMapArray() {
        return getTableEntry().getValue();
    }

    private void updateSize() {
        getTableEntry().setValuesCount(size());
        modCount += 1;
    }

    @Override
    public com.reandroid.arsc.value.Entry getEntry() {
        return entry;
    }

    protected abstract V createBagItem(ResValueMap valueMap, boolean copied);

    protected abstract ResValueMap newKey(K key);

    protected abstract K getKeyFor(ResValueMap valueMap);

    protected TableStringPool getStringPool() {
        com.reandroid.arsc.value.Entry entry = getEntry();
        if (entry == null) {
            return null;
        }
        PackageBlock pkg = entry.getPackageBlock();
        if (pkg == null) {
            return null;
        }
        TableBlock tableBlock = pkg.getTableBlock();
        if (tableBlock == null) {
            return null;
        }
        return tableBlock.getTableStringPool();
    }

    private class MapEntry implements Map.Entry<K, V> {
        private final ResValueMap item;

        private MapEntry(ResValueMap item) {
            this.item = item;
        }

        @Override
        public K getKey() {
            return getKeyFor(item);
        }

        @Override
        public V getValue() {
            return createBagItem(item, false);
        }

        @Override
        public V setValue(V v) {
            v.copyTo(item);
            return getValue();
        }
    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Entry<K, V>>() {
                private final Iterator<ResValueMap> iterator = getMapArray().iterator();
                private final int expectedModCount = modCount;

                private void checkValidity() {
                    if (expectedModCount != modCount) {
                        throw new ConcurrentModificationException("Iterator is no longer valid because the size has changed.");
                    }
                }

                @Override
                public boolean hasNext() {
                    checkValidity();
                    return iterator.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    checkValidity();
                    return new MapEntry(iterator.next());
                }
            };
        }

        @Override
        public int size() {
            return getMapArray().size();
        }
    }

    @Override
    public V remove(Object key) {
        ResValueMapArray array = getMapArray();
        Iterator<ResValueMap> iterator = array.clonedIterator();
        while (iterator.hasNext()) {
            ResValueMap item = iterator.next();
            if (getKeyFor(item).equals(key)) {
                if (!array.remove(item)) {
                    throw new IllegalStateException("Could not remove item");
                }
                updateSize();
                return createBagItem(item, true);
            }
        }
        return null;
    }

    @Override
    public void clear() {
        getMapArray().clear();
        updateSize();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        ResValueMapArray array = getMapArray();
        ResValueMap valueMap = null;
        Iterator<ResValueMap> iterator = array.clonedIterator();
        while (iterator.hasNext()) {
            ResValueMap item = iterator.next();
            if (getKeyFor(item).equals(key)) {
                valueMap = item;
                break;
            }
        }

        if (valueMap == null) {
            valueMap = newKey(key);
            array.add(valueMap);
            updateSize();
        }

        value.copyTo(valueMap);
        return createBagItem(valueMap, false);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        LinkedHashSet<K> keys = new LinkedHashSet<>(m.keySet());
        ResValueMapArray array = getMapArray();
        Iterator<ResValueMap> iterator = array.clonedIterator();
        while (iterator.hasNext()) {
            ResValueMap item = iterator.next();
            K currentKey = getKeyFor(item);

            if (keys.remove(currentKey)) {
                V src = m.get(currentKey);
                src.copyTo(item);
            }
        }

        for (K key : keys) {
            if (key == null) {
                throw new NullPointerException("Key is null");
            }
            ResValueMap item = newKey(key);
            array.add(item);
            V src = m.get(key);
            src.copyTo(item);
        }

        updateSize();
    }
}
