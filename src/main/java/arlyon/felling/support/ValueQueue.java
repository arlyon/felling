package arlyon.felling.support;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The value queue is a fifo data structure backed by a map of values.
 * Items added to the queue with a value greater than the maxValue will
 * be ignored. If they are added again, and now qualify then they will
 * be added to the queue.
 * <p>
 * Uses include:
 * - calculating Levenshtein distance of words and retrieving words with x or less.
 * - getting items only x units away from a source
 *
 * @param <T> Any object that implements equals and hashCode (for Hashtable)
 */
public class ValueQueue<T> {

    private final Map<T, Integer> map;
    private Queue<T> queue;

    private int maxValue;

    public ValueQueue(int maxValue) {
        this.maxValue = maxValue;
        map = new Hashtable<>();
        queue = new ArrayDeque<>();
    }

    /**
     * Adds the element to the queue if the value is lower
     * than the max and has not been added to the queue.
     *
     * @param t        The element to add to the queue.
     * @param newValue The value to insert.
     */
    public void add(T t, int newValue) {
        try {
            int oldValue = map.get(t); // get the old value for the item
            if (oldValue > newValue) map.put(t, newValue); // new value is smaller: update it
            if (oldValue > maxValue && newValue <= maxValue) queue.add(t); // new value qualifies for queue: add it
        } catch (NullPointerException e) {
            map.put(t, newValue); // add to the map if not exists
            if (newValue <= maxValue) queue.add(t); // add to the queue if qualified
        }
    }

    /**
     * Polls the queue for an element and
     * if the queue has any elements left to give,
     * sets that element to -1 (signifying completion)
     * and then returns it.
     *
     * @return The element or null if no more exist.
     */
    public T remove() {
        T element = queue.poll();
        if (element != null) map.put(element, -1);
        return element;
    }

    /**
     * Peeks the first element.
     *
     * @return The element or null.
     */
    public T peek() {
        return queue.peek();
    }

    public int getValue(T value) {
        return map.get(value);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean contains(T o) {
        return queue.contains(o);
    }

    public Iterator<T> iterator() {
        return queue.iterator();
    }

    public void forEach(Consumer<? super T> action) {
        queue.forEach(action);
    }

    public Object[] toArray() {
        return queue.toArray();
    }

    public void clear() {
        queue.clear();
        map.clear();
    }

    public Stream<T> stream() {
        return queue.stream();
    }

    /**
     * Updates the max value and recalculates
     * the queue to match.
     * @param maxValue The new max value.
     */
    public void setMaxValue(int maxValue) {
        if (maxValue > this.maxValue)
            this.map.keySet().forEach(element -> {
                int val = this.map.get(element);
                if (val > this.maxValue && val <= maxValue) {
                    this.queue.add(element);
                }
            });
        else if (maxValue < this.maxValue) {
            this.queue = this.queue.stream()
                    .filter(element -> this.map.get(element) > maxValue)
                    .collect(Collectors.toCollection(ArrayDeque::new));
        }

        this.maxValue = maxValue;
    }
}
