package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ChainedHashMap<K, V> extends AbstractIterableMap<K, V> {
    private static final double DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD = 1;
    private static final int DEFAULT_INITIAL_CHAIN_COUNT = 11;
    private static final int DEFAULT_INITIAL_CHAIN_CAPACITY = 5;

    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    AbstractIterableMap<K, V>[] chains;
    private int height;
    private int size;
    private double lambda;
    private double threshHold;
    private int bucketCapacity;

    // You're encouraged to add extra fields (and helper methods) though!

    public ChainedHashMap() {
        this(DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD, DEFAULT_INITIAL_CHAIN_COUNT, DEFAULT_INITIAL_CHAIN_CAPACITY);
    }

    public ChainedHashMap(double resizingLoadFactorThreshold, int initialChainCount, int chainInitialCapacity) {
        threshHold = resizingLoadFactorThreshold;
        bucketCapacity = chainInitialCapacity;
        height = initialChainCount;
        size = 0;
        lambda = 0;

        chains = createArrayOfChains(initialChainCount);
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code AbstractIterableMap<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     * @see ArrayMap createArrayOfEntries method for more background on why we need this method
     */
    @SuppressWarnings("unchecked")
    private AbstractIterableMap<K, V>[] createArrayOfChains(int arraySize) {
        return (AbstractIterableMap<K, V>[]) new AbstractIterableMap[arraySize];
    }

    /**
     * Returns a new chain.
     *
     * This method will be overridden by the grader so that your ChainedHashMap implementation
     * is graded using our solution ArrayMaps.
     *
     * Note: You do not need to modify this method.
     */
    protected AbstractIterableMap<K, V> createChain(int initialSize) {
        return new ArrayMap<>(initialSize);
    }

    private double calculateLambda() {
        return (double) size / height;
    }

    @Override
    public V get(Object key) {
        int index = createHashCode(key);
        if (chains[index] != null) {
            Iterator<Entry<K, V>> cur = chains[index].iterator();
            while (cur.hasNext()) {
                Entry<K, V> temp = cur.next();
                if (Objects.equals(key, temp.getKey())) {
                    return temp.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        int index = createHashCode(key);
        if (chains[index] == null) {
            chains[index] = createChain(bucketCapacity);
        }
        V ret = chains[index].put(key, value);
        if (ret == null) {
            size++;
        }
        lambda = calculateLambda();
        if (lambda > threshHold) {
            resize();
        }
        return ret;
    }

    private int createHashCode(Object key) {
        if (key == null) {
            return 0;
        }
        return Math.abs(key.hashCode() % height);
    }

    private void resize() {
        AbstractIterableMap<K, V>[] newChains = createArrayOfChains(height * 2);
        int oldHeight = height;
        height = height * 2;
        for (int i = 0; i < oldHeight; i++) {
            if (chains[i] != null) {
                Iterator<Entry<K, V>> itr = chains[i].iterator();
                while (itr.hasNext()) {
                    Entry<K, V> temp = itr.next();
                    int index = createHashCode(temp.getKey());
                    if (newChains[index] == null) {
                        newChains[index] = createChain(bucketCapacity);
                    }
                    newChains[index].put(temp.getKey(), temp.getValue());
                }
            }
        }
        chains = newChains;
        lambda = calculateLambda();
    }

    @Override
    public V remove(Object key) {
        int index = createHashCode(key);
        V ret = null;
        if (chains[index] != null) {
            ret = chains[index].remove(key);
            if (chains[index].isEmpty()) {
                chains[index] = null;
            }
            size--;
            lambda = calculateLambda();
        }
        return ret;
    }

    @Override
    public void clear() {
        AbstractIterableMap<K, V>[] newChains = createArrayOfChains(DEFAULT_INITIAL_CHAIN_COUNT);
        chains = newChains;
        size = 0;
        lambda = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (chains[createHashCode(key)] == null) {
            return false;
        }
        return chains[createHashCode(key)].containsKey(key);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ChainedHashMapIterator<>(this.chains);
    }


    /*
    See the assignment webpage for tips and restrictions on implementing this iterator.
     */
    private static class ChainedHashMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private AbstractIterableMap<K, V>[] chains;
        // You may add more fields and constructor parameters
        Iterator<Entry<K, V>> currentChain;
        Entry<K, V> nextEntry;
        private int index;

        public ChainedHashMapIterator(AbstractIterableMap<K, V>[] chains) {
            this.chains = chains;
            this.index = 0;
            findNextNonNullBucket();
        }

        private void findNextNonNullBucket() {
            while (hasNext() && chains[index] == null) {
                index++;
            }
            if (hasNext()) {
                currentChain = chains[index].iterator();
                if (currentChain.hasNext()) {
                    nextEntry = currentChain.next();
                }
            }
        }

        @Override
        public boolean hasNext() {
            return chains.length != index;
        }

        @Override
        public Map.Entry<K, V> next() {
            if (hasNext()) {
                SimpleEntry<K, V> ret = new SimpleEntry<>(nextEntry.getKey(), nextEntry.getValue());
                if (!currentChain.hasNext()) {
                    index++;
                    findNextNonNullBucket();
                } else {
                    nextEntry = currentChain.next();
                }
                return ret;
            }
            throw new NoSuchElementException();
        }
    }
}
