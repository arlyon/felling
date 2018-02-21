package arlyon.felling.support;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The value queue is a fifo data structure backed by a map of values.
 * The way it works, is that values are added to the queue and assigned a value.
 * @param <T>
 */
public class ValueQueue<T> {

    private final Map<T, Integer> map;
    private final Queue<T> queue;
    private int maxValue;

    public ValueQueue(int maxValue) {
        this.maxValue = maxValue;
        map = new Hashtable<>();
        queue = new ArrayDeque<>();
    }

    /**
     * Adds the element to the queue if the value is lower
     * than the max and has not been added to the queue.
     * @param t The element to add to the queue.
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
     * @return The element or null if no more exist.
     */
    public T remove() {
        T element = queue.poll();
        if (element != null) map.put(element, -1);
        return element;
    }

    /**
     * Peeks the first element.
     * @return The element or null.
     */
    public T peek() {
        return queue.peek();
    }

    public int getDistance(T value) {
        return map.get(value);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean contains(Object o) {
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

    public Spliterator<T> spliterator() {
        return queue.spliterator();
    }

    public Stream<T> stream() {
        return queue.stream();
    }

    public Stream<T> parallelStream() {
        return queue.parallelStream();
    }
}
