package ru.spbau.mit;

/**
 * This class specifies a single node of trie.
 * Each node holds:
 * <ol>
 *     <li>References to its children.</li>
 *     <li>Whether or not there is an element in the set which ends in that particular node.</li>
 *     <li>Cumulative amount of all elements which end somewhere in the subtree (including the node).</li>
 * </ol>
 * This class is supposed to be used by <code>StringSetImpl</code> only.
 * Only ways of interaction are constructor and <code>dig</code> method.
 *
 * Invariant: <code>elementsInSubtree</code> is equal to amount of
 * <code>isElementEnd</code> set to true in all descendant <code>TrieNode</code>s,
 * including itself.
 */
class TrieNode {
    /**
     * Internal conversion between chars and a small range of integers.
     * It exploits the fact that we have Latin letters only.
     */
    static final int ALPHABET_SIZE = 52;
    static int getCharacterCode(char c) {
        if ('a' <= c && c <= 'z') {
            return c - 'a';
        }
        if ('A' <= c && c <= 'Z') {
            return 26 + c - 'A';
        }
        throw new IllegalArgumentException("Character is expected to be Latin letter");
    }

    public TrieNode[] children;
    public boolean isElementEnd;
    public int elementsInSubtree;

    /**
     * Creates empty node with no children and no substrings marked as present.
     */
    public TrieNode() {
        children = new TrieNode[ALPHABET_SIZE];
        isElementEnd = false;
        elementsInSubtree = 0;
    }

    /**
     * Digs down the current subtree along string <code>element</code> starting at character
     * <code>startFrom</code>. Calculates some function at the ending node and allows
     * modifying of nodes along the path.
     *
     * After reaching the end of <code>element</code>, <code>listener.nodeReached</code> callback
     * is called and its return value is passed up the tree, transforming by
     * <code>listener.exitingInnerNode</code> in each inner node of the path.
     * If there is no corresponding child at some moment of time, behavior is regulated
     * by <code>enforcePath</code>:
     * <ul>
     *     <li>If it's <code>true</code>, then corresponding child is set to newly created empty node
     *     and digging is continued.</li>
     *     <li>Otherwise, digging is stopped and <code>listener.nodeNotFound</code> callback is called
     *     and the value returned is passed up the tree as usual.</li>
     * </ul>
     * @param element String, which specifies digging path
     * @param startFrom Position in <code>element</code> to start from
     * @param enforcePath Whether or not create new trie node, if no desired child is found
     * @param listener Set of callbacks which define transformation of the subtree and returned value
     * @param <T> Type of value returned by <code>dig</code> and callbacks
     * @return Result of the digging
     */
    public <T> T dig(String element, int startFrom, boolean enforcePath, DigListener<T> listener) {
        if (startFrom == element.length()) {
            return listener.nodeReached(this);
        }
        int childId = getCharacterCode(element.charAt(startFrom));
        if (children[childId] == null) {
            if (!enforcePath) {
                return listener.nodeNotFound();
            }
            children[childId] = new TrieNode();
        }
        T childResult = children[childId].dig(element, startFrom + 1, enforcePath, listener);
        if (children[childId].elementsInSubtree == 0) {
            children[childId] = null;
        }
        return listener.exitingInnerNode(this, childResult);
    }

    /**
     * Interface for listener of <code>dig</code>-generated events.
     * @param <T> Type of value which is calculated during digging
     */
    interface DigListener<T> {
        /**
         * Called whenever digging successfully reaches final node, which
         * corresponds to the input string.
         * May modify <code>node<</code>, but should ensure that all invariants
         * for it are still correct before returning.
         * @param node The last node of the path. Digging stops here.
         * @return Value of type <code>T</code> which will be passed to parent nodes' callbacks
         *         or as the result of <code>dig</code>.
         */
        public T nodeReached(TrieNode node);

        /**
         * Called whenever digging fails because the desired string is not found
         * in the subtree.
         * @return Value of type <code>T</code> which will be passed to parent nodes' callbacks
         *         or as the result of <code>dig</code>.
         */
        public T nodeNotFound();

        /**
         * Called when digging exits a node, regardless whether or not the source string was found.
         * Should process result returned by processing of child subtree.
         * May modify <code>node<</code>, but should ensure that all invariants
         * for it are still correct before returning.
         * @param node Node which is being exited by <code>dig</code>
         * @param childResult Result which was returned by processing one of child nodes
         *                    or result of <code>nodeNotFound</code>, if there is no
         *                    desired child.
         * @return Value of type <code>T</code> which will be passed to parent nodes' callbacks
         *         or as the result of <code>dig</code>.
         */
        public T exitingInnerNode(TrieNode node, T childResult);
    }
}
