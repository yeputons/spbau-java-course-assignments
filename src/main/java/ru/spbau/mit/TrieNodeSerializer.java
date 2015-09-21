package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * There is a separate class instead of making <code>TrieNode</code>
 * implement <code>StreamSerializable</code>, because that way
 * serialization of <code>null</code> nodes is significantly
 * easier and can be performed in a single place of code.
 */
public class TrieNodeSerializer {
    // These constants are "header" of every node
    protected final static byte NO_NODE = 0;
    protected final static byte NODE_NO_END = 1;
    protected final static byte NODE_WITH_END = 2;

    /**
     * Serializes <code>node</code> to <code>out</code>
     * @param node Node to save into stream
     * @param out Stream to serialize into
     * @throws IOException
     */
    static void serialize(TrieNode node, OutputStream out) throws IOException {
        if (node == null) {
            out.write(new byte[] { NO_NODE });
            return;
        }
        out.write(new byte[]{ node.isElementEnd ? NODE_WITH_END : NODE_NO_END });
        for (TrieNode child : node.children) {
            serialize(child, out);
        }
    }

    /**
     * Loads <code>TrieNode</code> from <code>in</code>.
     * Works in pair with <code>serialize</code>.
     * Does not require explicit end mark and automatically stops processing
     * after reading of node is done.
     * @param in Stream to load <code>TrieNode</code> from
     * @return Newly constructed <code>TrieNode</code> which was read from <code>in</code>
     * @throws IOException
     */
    static TrieNode deserialize(InputStream in) throws IOException {
        int type = in.read();
        if (type == -1) {
            throw new SerializationException();
        }
        if (type == NO_NODE) {
            return null;
        }
        TrieNode result = new TrieNode();
        if (type == NODE_WITH_END) {
            result.isElementEnd = true;
            result.elementsInSubtree++;
        } else if (type == NODE_NO_END) {
            result.isElementEnd = false;
        } else {
            throw new SerializationException();
        }
        for (int i = 0; i < result.children.length; i++) {
            result.children[i] = deserialize(in);
            if (result.children[i] != null) {
                result.elementsInSubtree += result.children[i].elementsInSubtree;
            }
        }
        return result;
    }
}
