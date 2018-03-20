package arlyon.felling.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueQueueTest {

    @Test
    void add() {
        ValueQueue<String> wordWeight = new ValueQueue<>(2);
        wordWeight.add("Hello", 3);
        Assertions.assertNull(wordWeight.remove());

        wordWeight.add("World", 2);
        Assertions.assertEquals("World", wordWeight.remove());
        Assertions.assertNull(wordWeight.remove());
    }

    @Test
    void setMaxValue() {
        ValueQueue<String> wordWeight = new ValueQueue<>(2);
        wordWeight.add("Hello", 3);
        Assertions.assertNull(wordWeight.remove());

        wordWeight.setMaxValue(3);
        Assertions.assertEquals("Hello", wordWeight.remove());
    }
}