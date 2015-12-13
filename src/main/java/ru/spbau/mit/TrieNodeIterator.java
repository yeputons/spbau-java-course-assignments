package ru.spbau.mit;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class TrieNodeIterator implements Iterator<String> {
    /**
     * State of the iterator: some node in a trie (stored together with a traversed path).
     * If length of corresponding string is L, then <code>currentString</code> has L elements and
     * <code>currentPath</code> has L+1 elements.
     *
     * If iterator is depleted <code>currentPath</code> is empty
     */
    private Stack<TrieNode> currentPath = new Stack<>();
    private Stack<Integer> currentString = new Stack<>();
    private boolean visitedCurrent;

    public TrieNodeIterator(TrieNode root) {
        currentPath.push(root);
        visitedCurrent = !root.isElementEnd;
    }

    /**
     * Iterates to next non-visited end node in a trie
     */
    private void findNextString() {
        if (currentPath.empty()) {
            throw new NoSuchElementException();
        }
        if (!visitedCurrent && currentPath.peek().isElementEnd) {
            return;
        }
        visitedCurrent = false;

        // Invariant: currentPath and currentString have same length,
        // we've already visited all subtrees of currentPath.peek()'s children
        // before and including currentString.peek().
        currentString.push(-1);
        while (true) {
            if (currentString.empty()) {
                throw new NoSuchElementException();
            }

            boolean found = false;
            for (int childId = currentString.peek() + 1; childId < TrieNode.ALPHABET_SIZE; childId++) {
                if (currentPath.peek().children[childId] != null) {
                    found = true;
                    currentString.pop();
                    currentString.push(childId);
                    currentPath.push(currentPath.peek().children[childId]);
                    if (currentPath.peek().isElementEnd) {
                        return;
                    }
                    currentString.push(-1);
                    break;
                }
            }
            if (!found) {
                currentPath.pop();
                currentString.pop();
            }
        }
    }

    @Override
    public boolean hasNext() {
        try {
            findNextString();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public String next() {
        findNextString();
        assert !visitedCurrent;
        assert currentPath.peek().isElementEnd;
        StringBuilder result = new StringBuilder();
        for (int code : currentString) {
            result.append(TrieNode.getCharacterFromCode(code));
        }
        visitedCurrent = true;
        return result.toString();
    }

    @Override
    public void remove() {
        throw new NotImplementedException();
    }
}
