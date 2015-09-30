package ru.spbau.mit;

import java.util.*;

/**
 * This class implements <code>Set<E></code> with treap, which
 * is a self-balanced binary tree. All operations of <code>Set<E>/code>
 * are supported. No thread safety is guaranteed.
 * @param <E>
 */
public class TreeSetImpl<E> extends AbstractSet<E> {
    private Comparator<E> comparator;
    private Random priorityGenerator = new Random();
    private TreapNode rootNode;
    private int size = 0;

    public TreeSetImpl(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(E value) {
        if (contains(value)) {
            return false;
        }
        SplitResult splitted = split(rootNode, value, /* keyGoesToRight */ true);
        TreapNode newNode = new TreapNode(value);
        rootNode = merge(merge(splitted.left, newNode), splitted.right);
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        E value = (E)o;
        SplitResult splitted = split(rootNode, value, /* keyGoesToRight */ true);
        TreapNode left = splitted.left;
        splitted = split(splitted.right, value, /* keyGoesToRight */ false);
        boolean result = splitted.left != null;
        rootNode = merge(left, splitted.right);
        if (result) {
            size--;
        }
        return result;
    }

    @Override
    public void clear() {
        rootNode = null;
        size = 0;
    }

    @Override
    public boolean contains(Object o) {
        return contains(rootNode, (E)o);
    }

    private boolean contains(TreapNode node, E value) {
        if (node == null) {
            return false;
        }
        int comparisonResult = comparator.compare(node.value, value);
        if (comparisonResult == 0) {
            return true;
        }
        if (comparisonResult < 0) {
            return contains(node.rightChild, value);
        } else {
            return contains(node.leftChild, value);
        }
    }

    @Override
    public int size() {
        return size;
    }

    private class TreapIterator implements Iterator<E> {
        private TreapNode previous, next;

        TreapIterator(TreapNode first) {
            previous = null;
            next = first;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            previous = next;

            // advance next element
            if (next.rightChild != null) {
                next = next.rightChild;
                while (next.leftChild != null) {
                    next = next.leftChild;
                }
            } else {
                boolean advanced = false;
                while (next.parent != null) {
                    if (next.parent.leftChild == next) {
                        next = next.parent;
                        advanced = true;
                        break;
                    } else {
                        if (next.parent.rightChild != next) {
                            throw new AssertionError("Invalid parent link in TreapNode");
                        }
                        next = next.parent;
                    }
                }
                if (!advanced) {
                    next = null;
                }
            }
            return previous.value;
        }

        @Override
        public void remove() {
            if (previous == null) {
                throw new IllegalStateException();
            }
            TreapNode parent = previous.parent;
            TreapNode replacement = merge(previous.leftChild, previous.rightChild);
            if (replacement != null) {
                replacement.parent = parent;
            }
            if (parent == null) { // it was root of the treap
                rootNode = replacement;
            } else {
                if (parent.leftChild == previous) {
                    parent.leftChild = replacement;
                } else if (parent.rightChild == previous) {
                    parent.rightChild = replacement;
                } else {
                    throw new AssertionError("Invalid parent link in TreapNode");
                }
            }
            previous = null;
        }
    }

    @Override
    public Iterator<E> iterator() {
        if (rootNode == null) {
            return new TreapIterator(null);
        }
        TreapNode first = rootNode;
        while (first.leftChild != null) {
            first = first.leftChild;
        }
        return new TreapIterator(first);
    }

    private class TreapNode {
        public E value;
        public int priority;
        public TreapNode leftChild, rightChild, parent;

        public TreapNode(E value) {
            this.value = value;
            priority = priorityGenerator.nextInt();
            leftChild = null;
            rightChild = null;
            parent = null;
        }

        public void updateChildrenParent() {
            if (leftChild != null) {
                leftChild.parent = this;
            }
            if (rightChild != null) {
                rightChild.parent = this;
            }
        }
    }

    TreapNode merge(TreapNode left, TreapNode right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        if (left.priority > right.priority) {
            left.rightChild = merge(left.rightChild, right);
            left.updateChildrenParent();
            left.parent = null;
            return left;
        } else {
            right.leftChild = merge(left, right.leftChild);
            right.updateChildrenParent();
            right.parent = null;
            return right;
        }
    }

    class SplitResult {
        TreapNode left, right;
    }

    SplitResult split(TreapNode node, E key, boolean keyGoesToRight) {
        if (node == null) {
            return new SplitResult();
        }
        int comparisonResult = comparator.compare(node.value, key);
        boolean nodeGoesToRight =
                comparisonResult > 0 ||
                (comparisonResult == 0 && keyGoesToRight);
        if (nodeGoesToRight) {
            SplitResult result = split(node.leftChild, key, keyGoesToRight);
            node.leftChild = result.right;
            node.updateChildrenParent();
            result.right = node;
            return result;
        } else {
            SplitResult result = split(node.rightChild, key, keyGoesToRight);
            node.rightChild = result.left;
            node.updateChildrenParent();
            result.left = node;
            return result;
        }
    }
}
