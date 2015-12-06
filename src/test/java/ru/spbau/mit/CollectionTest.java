package ru.spbau.mit;

import org.junit.Test;
import ru.spbau.mit.testclasses.Val;
import ru.spbau.mit.testclasses.ValA;

import java.util.*;

import static org.junit.Assert.*;

public class CollectionTest {
    @Test
    public void testMap() {
        List<ValA> source = Arrays.asList(new ValA(1), new ValA(2), new ValA(3));
        List<Val> expected = Arrays.asList(new Val(1), new Val(4), new Val(9));

        Iterable<Val> res = Collection.map(new Function1<Val, Val>() {
            @Override
            public Val apply(Val arg) {
                return new Val(arg.val * arg.val);
            }
        }, source);
        for (int step = 0; step < 2; step++) {
            List<Val> got = new LinkedList<>();
            for (Val x : res) {
                got.add(x);
            }
            assertEquals(expected, got);
        }
    }

    @Test
    public void testFilter() {
        List<Integer> source = Arrays.asList(1, 2, 1, null, 1, 4);
        List<Integer> expected = Arrays.asList(1, 2, 1, 1, 4);

        Iterable<Integer> res = Collection.filter(new Predicate<Number>() {
            @Override
            public Boolean apply(Number arg) {
                return arg != null;
            }
        }, source);
        for (int step = 0; step < 2; step++) {
            List<Integer> got = new LinkedList<>();
            for (Integer x : res) {
                got.add(x);
            }
            assertEquals(expected, got);
        }
    }

    @Test
    public void testTakeWhile() {
        List<Integer> source = Arrays.asList(1, 2, 1, null, 1, 4);
        List<Integer> expected = Arrays.asList(1, 2, 1);

        Iterable<Integer> res = Collection.takeWhile(new Predicate<Number>() {
            @Override
            public Boolean apply(Number arg) {
                return arg != null;
            }
        }, source);
        for (int step = 0; step < 2; step++) {
            List<Integer> got = new LinkedList<>();
            for (Integer x : res) {
                got.add(x);
            }
            assertEquals(expected, got);
        }
    }

    @Test
    public void testTakeUnless() {
        List<Integer> source = Arrays.asList(1, 2, 1, null, 1, 4);
        List<Integer> expected = Arrays.asList(1, 2, 1);

        Iterable<Integer> res = Collection.takeUnless(new Predicate<Number>() {
            @Override
            public Boolean apply(Number arg) {
                return arg == null;
            }
        }, source);
        for (int step = 0; step < 2; step++) {
            List<Integer> got = new LinkedList<>();
            for (Integer x : res) {
                got.add(x);
            }
            assertEquals(expected, got);
        }
    }

    @Test
    public void testFoldr() {
        List<Integer> source = Arrays.asList(1, 2, 3);
        LinkedList<String> initial = new LinkedList<>();
        initial.add("init");

        LinkedList<String> result = Collection.foldr(new Function2<Object, List<String>, LinkedList<String>>() {
            @Override
            public LinkedList<String> apply(Object arg1, List<String> arg2) {
                LinkedList<String> result = new LinkedList<String>();
                result.add(String.format("%s+(%s)", arg1.toString(), arg2.get(0)));
                for (String s : arg2) {
                    result.add(s);
                }
                return result;
            }
        }, source, initial);

        assertArrayEquals(new String[]{
                "1+(2+(3+(init)))",
                "2+(3+(init))",
                "3+(init)",
                "init"
        }, result.toArray());
    }

    @Test
    public void testFoldl() {
        List<Integer> source = Arrays.asList(1, 2, 3);
        LinkedList<String> initial = new LinkedList<>();
        initial.add("init");

        LinkedList<String> result = Collection.foldl(new Function2<List<String>, Object, LinkedList<String>>() {
            @Override
            public LinkedList<String> apply(List<String> arg1, Object arg2) {
                LinkedList<String> result = new LinkedList<String>();
                result.add(String.format("(%s)+%s", arg1.get(0), arg2.toString()));
                for (String s : arg1) {
                    result.add(s);
                }
                return result;
            }
        }, initial, source);

        assertArrayEquals(new String[]{
                "(((init)+1)+2)+3",
                "((init)+1)+2",
                "(init)+1",
                "init"
        }, result.toArray());
    }

    private final List<Boolean> trues = Arrays.asList(true, true, true);
    private final List<Boolean> falses = Arrays.asList(false, false, false);
    private final List<Boolean> mixed = Arrays.asList(false, true, false);

    @Test
    public void testOr() {
        assertTrue(Collection.or(trues));
        assertFalse(Collection.or(falses));
        assertTrue(Collection.or(mixed));
    }

    @Test
    public void testAnd() {
        assertTrue(Collection.and(trues));
        assertFalse(Collection.and(falses));
        assertFalse(Collection.and(mixed));
    }
}
