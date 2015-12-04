package ru.spbau.mit;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class SerializableStringSetTest {

    @Test
    public void testSimple() {
        StringSet stringSet = instance();

        assertTrue(stringSet.add("abc"));
        assertTrue(stringSet.contains("abc"));
        assertEquals(1, stringSet.size());
        assertEquals(1, stringSet.howManyStartsWithPrefix("abc"));
    }

    @Test
    public void testSimpleSerialization() {
        StringSet stringSet = instance();

        assertTrue(stringSet.add("abc"));
        assertTrue(stringSet.add("cde"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ((StreamSerializable) stringSet).serialize(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        StringSet newStringSet = instance();
        ((StreamSerializable) newStringSet).deserialize(inputStream);

        assertTrue(newStringSet.contains("abc"));
        assertTrue(newStringSet.contains("cde"));
    }


    @Test(expected=SerializationException.class)
    public void testSimpleSerializationFails() {
        StringSet stringSet = instance();

        assertTrue(stringSet.add("abc"));
        assertTrue(stringSet.add("cde"));

        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Fail");
            }
        };

        ((StreamSerializable) stringSet).serialize(outputStream);
    }

    @Test
    public void testIteratorIteratesInLexicographicalOrder() {
        StringSet actual = instance();

        assertTrue(actual.add("abc"));
        assertTrue(actual.add("aac"));
        assertTrue(actual.add("a"));
        assertTrue(actual.add("bac"));
        assertTrue(actual.add("ababc"));
        assertTrue(actual.add(""));

        List<String> expected = new ArrayList<>();
        expected.add("");
        expected.add("a");
        expected.add("aac");
        expected.add("ababc");
        expected.add("abc");
        expected.add("bac");

        Iterator<String> actualIterator = actual.iterator();
        Iterator<String> expectedIterator = expected.iterator();
        while (actualIterator.hasNext()) {
            assertEquals(expectedIterator.next(), actualIterator.next());
        }
        assertEquals(expectedIterator.hasNext(), actualIterator.hasNext());
    }

    public static StringSet instance() {
        try {
            return (StringSet) Class.forName("ru.spbau.mit.StringSetImpl").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Error while class loading");
    }
}
