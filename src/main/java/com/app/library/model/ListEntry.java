package com.app.library.model;

import java.util.Objects;

public class ListEntry<K,V> {

    private K key;
    private V value;

    public ListEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public ListEntry() {
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListEntry)) return false;
        ListEntry<?, ?> listEntry = (ListEntry<?, ?>) o;
        return Objects.equals(key, listEntry.key) && Objects.equals(value, listEntry.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "ListEntry{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
