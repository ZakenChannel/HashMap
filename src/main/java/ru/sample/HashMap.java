package ru.sample;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HashMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private Node<K, V>[] table;
    private int size;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @SuppressWarnings("unchecked")
    public HashMap() {
        table = new Node[DEFAULT_INITIAL_CAPACITY];
        size = 0;
    }

    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return size == 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(V value) {
        lock.readLock().lock();
        try {
            for (Node<K, V> node : table) {
                Node<K, V> current = node;
                while (current != null) {
                    if (Objects.equals(current.value, value)) {
                        return true;
                    }
                    current = current.next;
                }
            }

            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V get(K key) {
        int hash = hash(key);
        lock.readLock().lock();
        try {
            int index = (table.length - 1) & hash;
            Node<K, V> current = table[index];
            while (current != null) {
                if (current.hash == hash && Objects.equals(key, current.key)) {
                    return current.value;
                }
                current = current.next;
            }

            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        int hash = hash(key);
        lock.writeLock().lock();
        try {
            int index = (table.length - 1) & hash;
            Node<K, V> current = table[index];
            if (current == null) {
                table[index] = new Node<>(hash, key, value, null);
                size++;
                if (size >= table.length * DEFAULT_LOAD_FACTOR) {
                    resize();
                }

                return null;
            } else {
                while (true) {
                    if (current.hash == hash && Objects.equals(key, current.key)) {
                        V oldValue = current.value;
                        current.value = value;
                        return oldValue;
                    }
                    if (current.next == null) {
                        current.next = new Node<>(hash, key, value, null);
                        size++;
                        if (size >= table.length * DEFAULT_LOAD_FACTOR) {
                            resize();
                        }

                        return null;
                    }
                    current = current.next;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V remove(K key) {
        int hash = hash(key);
        lock.writeLock().lock();
        try {
            int index = (table.length - 1) & hash;
            Node<K, V> current = table[index];
            Node<K, V> previous = null;
            while (current != null) {
                if (current.hash == hash && Objects.equals(key, current.key)) {
                    if (previous == null) {
                        table[index] = current.next;
                    } else {
                        previous.next = current.next;
                    }
                    V oldValue = current.value;
                    size--;
                    return oldValue;
                }
                previous = current;
                current = current.next;
            }

            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        lock.writeLock().lock();
        try {
            table = new Node[DEFAULT_INITIAL_CAPACITY];
            size = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        Node<K, V>[] newTable = new Node[newCapacity];
        for (Node<K, V> node : oldTable) {
            if (node != null) {
                Node<K, V> current = node;
                while (current != null) {
                    Node<K, V> next = current.next;
                    int index = (newCapacity - 1) & current.hash;
                    current.next = newTable[index];
                    newTable[index] = current;
                    current = next;
                }
            }
        }
        table = newTable;
    }
}
