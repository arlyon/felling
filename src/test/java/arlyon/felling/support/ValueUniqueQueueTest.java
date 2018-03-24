package arlyon.felling.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueUniqueQueueTest {

    @Test
    void add() {
        ValueUniqueQueue<String> wordWeight = new ValueUniqueQueue<>(item -> item <= 2);
        wordWeight.add("Hello", 3);
        Assertions.assertNull(wordWeight.remove());

        wordWeight.add("World", 2);
        Assertions.assertEquals("World", wordWeight.remove());
        Assertions.assertNull(wordWeight.remove());
    }

    @Test
    void predicate() {
        ValueUniqueQueue<String> evenLength = new ValueUniqueQueue<>(item -> item % 2 == 0);

        String[] strings = {
                "hello",
                "world",
                "this",
                "is",
                "a",
                "test"
        };

        for (String string : strings) {
            evenLength.add(string, string.length());
        }

        evenLength.forEach(word -> Assertions.assertEquals(word.length() % 2, 0));
        evenLength.clear(); // foreach does not delete the items

        evenLength.setPredicate(item -> item % 2 == 1);
        evenLength.forEach(word -> Assertions.assertEquals(word.length() % 2, 1));

        Assertions.assertNull(evenLength.remove());
    }

    @Test
    void setMaxValue() {
        ValueUniqueQueue<String> wordWeight = new ValueUniqueQueue<>(item -> item <= 2);
        wordWeight.add("Hello", 3);
        Assertions.assertNull(wordWeight.remove());

        wordWeight.setPredicate(item -> item <= 3);
        Assertions.assertEquals("Hello", wordWeight.remove());
    }
}