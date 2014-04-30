package com.jamierf.mediamanager.db.azure;

import com.microsoft.windowsazure.services.core.storage.StorageException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AzureSet<K> implements Set<K> {

    private static final Boolean VALUE = Boolean.TRUE;

    private final Map<K, Boolean> map;

    public AzureSet(String accountName, String accountKey, String tableName, Class<K> keyClass) throws StorageException {
        map = new AzureMap<K, Boolean>(accountName, accountKey, tableName, keyClass, Boolean.class);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<K> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(K k) {
        return map.put(k, VALUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object key : c) {
            if (!this.contains(key)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean result = false;

        for (K key : c) {
            result |= this.add(key);
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean result = false;

        final Iterator<Map.Entry<K, Boolean>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final K key = iterator.next().getKey();

            if (!c.contains(key)) {
                iterator.remove();
                result = true;
            }
        }

        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;

        for (Object key : c) {
            result |= this.remove(key);
        }

        return result;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
