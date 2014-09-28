package com.jamierf.mediamanager.db.azure;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.yammer.collections.azure.util.AzureTables;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AzureMap<K, V> implements Map<K, V> {

    private static final Boolean KEY = Boolean.TRUE;

    private final Table<Boolean, K, V> table;

    public AzureMap(final String accountName, final String accountKey, final String tableName, final Class<K> keyClass, final Class<V> valueClass, final MetricRegistry metrics) throws StorageException {
        table = AzureTables.clientForAccount(accountName, accountKey)
                .tableWithName(tableName)
                .createIfDoesNotExist()
                .andAddMetrics(metrics)
                .buildWithJsonSerialization(Boolean.class, keyClass, valueClass);
    }

    @Override
    public int size() {
        return table.row(KEY).size();
    }

    @Override
    public boolean isEmpty() {
        return table.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return table.containsColumn(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return table.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return table.get(KEY, key);
    }

    @Override
    public V put(K key, V value) {
        return table.put(KEY, key, value);
    }

    @Override
    public V remove(Object key) {
        return table.remove(KEY, key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        table.clear();
    }

    @Override
    public Set<K> keySet() {
        return table.columnKeySet();
    }

    @Override
    public Collection<V> values() {
        return table.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return table.row(KEY).entrySet();
    }
}
