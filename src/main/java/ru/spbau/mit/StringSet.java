package ru.spbau.mit;

import java.util.Iterator;


public interface StringSet {
    /**
     * Expected complexity: O(|element|)
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element
     */
    boolean add(String element);

    /**
     * Expected complexity: O(|element|)
     */
    boolean contains(String element);

    /**
     * Expected complexity: O(|element|)
     * @return <tt>true</tt> if this set contained the specified element
     */
    boolean remove(String element);

    /**
     * Expected complexity: O(1)
     */
    int size();

    /**
     * Expected complexity: O(|prefix|)
     */
    int howManyStartsWithPrefix(String prefix);

    /**
     * Iterates over the words in a string set in lexicographical order
     * Expected complexity: O(1)
     */
    Iterator<String> iterator();
}
