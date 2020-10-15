package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ArrayMap<K, V> extends AbstractIterableMap<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    SimpleEntry<K, V>[] entries;
    private int capacity = DEFAULT_INITIAL_CAPACITY;

    // You may add extra fields or helper methods though!
    private int size;

    public ArrayMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }


    public ArrayMap(int initialCapacity) {
        this.size = 0;
        this.entries = this.createArrayOfEntries(initialCapacity);
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code Entry<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     */
    @SuppressWarnings("unchecked")
    private SimpleEntry<K, V>[] createArrayOfEntries(int arraySize) {
        /*
        It turns out that creating arrays of generic objects in Java is complicated due to something
        known as "type erasure."

        We've given you this helper method to help simplify this part of your assignment. Use this
        helper method as appropriate when implementing the rest of this class.

        You are not required to understand how this method works, what type erasure is, or how
        arrays and generics interact.
        */
        return (SimpleEntry<K, V>[]) (new SimpleEntry[arraySize]);
    }

    @Override
    public V get(Object key) {
        int i = indexOf(key);
        if (i == -1) {
            return null;
        }
        return entries[i].getValue();
    }

    @Override
    public V put(K key, V value) {
        int index = indexOf(key);
        if (index != -1) {
            V temp = entries[index].getValue();
            entries[index].setValue(value);
            return temp;
        } else {
            if (size == entries.length) {
                SimpleEntry<K, V>[] newMap = createArrayOfEntries(size * 2);
                for (int i = 0; i < size; i++) {
                    newMap[i] = entries[i];
                }
                this.entries = newMap;
            }
            SimpleEntry<K, V> add = new SimpleEntry(key, value);
            entries[size] = add;
            size++;
        }
        return null;
    }

    private int indexOf(Object key) {
        for (int i = 0; i < size; i++) {
            K k1 = entries[i].getKey();
            if (Objects.equals(key, k1)) {
                return i;
            }
        }
        return -1;
    }

    // k1 != null && (k1.equals(key) || k1 == key)

    // fixed
    @Override
    public V remove(Object key) {
        int index = indexOf(key);
        if (index == -1) {
            return null;
        }
        V temp = entries[index].getValue();
        entries[index] = entries[size - 1];
        entries[size - 1] = null;
        size--;
        return temp;
    }

    @Override
    public void clear() {
        entries = createArrayOfEntries(DEFAULT_INITIAL_CAPACITY);
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf(key) != -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ArrayMapIterator<>(this.entries);
    }

    private static class ArrayMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private final SimpleEntry<K, V>[] entries;
        // You may add more fields and constructor parameters
        private SimpleEntry<K, V> current;
        private int i;

        public ArrayMapIterator(SimpleEntry<K, V>[] entries) {
            this.entries = entries;
            this.i = 0;
            this.current = entries[0];
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Map.Entry<K, V> next() {
            if (hasNext()) {
                SimpleEntry<K, V> map = new SimpleEntry<>(this.entries[i].getKey(), this.entries[i].getValue());
                i++;
                if (i == entries.length) {
                    this.current = null;
                } else  {
                    this.current = this.entries[i];
                }
                return map;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
