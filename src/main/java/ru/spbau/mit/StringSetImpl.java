package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StringSetImpl implements StringSet, StreamSerializable {
    private TrieNode root = new TrieNode();

    @Override
    public boolean add(String element) {
        return root.dig(element, /* startFrom */ 0, /* enforcePath */ true, new TrieNode.DigListener<Boolean>() {
            @Override
            public Boolean nodeReached(TrieNode node) {
                if (node.isElementEnd) {
                    return false;
                }
                node.isElementEnd = true;
                node.elementsInSubtree++;
                return true;
            }

            @Override
            public Boolean nodeNotFound() {
                throw new IllegalStateException();
            }

            @Override
            public Boolean exitingInnerNode(TrieNode node, Boolean childResult) {
                if (childResult) {
                    node.elementsInSubtree++;
                }
                return childResult;
            }
        });
    }

    @Override
    public boolean contains(String element) {
        return root.dig(element, /* startFrom */ 0, /* enforcePath */ false, new TrieNode.DigListener<Boolean>() {
            @Override
            public Boolean nodeReached(TrieNode node) {
                return node.isElementEnd;
            }

            @Override
            public Boolean nodeNotFound() {
                return false;
            }

            @Override
            public Boolean exitingInnerNode(TrieNode node, Boolean childResult) {
                return childResult;
            }
        });
    }

    @Override
    public boolean remove(String element) {
        return root.dig(element, /* startFrom */ 0, /* enforcePath */ false, new TrieNode.DigListener<Boolean>() {
            @Override
            public Boolean nodeReached(TrieNode node) {
                if (!node.isElementEnd) {
                    return false;
                }
                node.isElementEnd = true;
                node.elementsInSubtree--;
                return true;
            }

            @Override
            public Boolean nodeNotFound() {
                return false;
            }

            @Override
            public Boolean exitingInnerNode(TrieNode node, Boolean childResult) {
                if (childResult) {
                    node.elementsInSubtree--;
                }
                return childResult;
            }
        });
    }

    @Override
    public int size() {
        return root.elementsInSubtree;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        return root.dig(prefix, /* startFrom */ 0, /* enforcePath */ false, new TrieNode.DigListener<Integer>() {
            @Override
            public Integer nodeReached(TrieNode node) {
                return node.elementsInSubtree;
            }

            @Override
            public Integer nodeNotFound() {
                return 0;
            }

            @Override
            public Integer exitingInnerNode(TrieNode node, Integer childResult) {
                return childResult;
            }
        });
    }

    @Override
    public void serialize(OutputStream out) {
        try {
            TrieNodeSerializer.serialize(root, out);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    @Override
    public void deserialize(InputStream in) {
        try {
            root = TrieNodeSerializer.deserialize(in);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }
}
