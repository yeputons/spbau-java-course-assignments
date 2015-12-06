package ru.spbau.mit;

import org.junit.Test;
import ru.spbau.mit.testclasses.Val;
import ru.spbau.mit.testclasses.ValA;
import ru.spbau.mit.testclasses.ValB;
import ru.spbau.mit.testclasses.ValC;

import java.util.*;

import static org.junit.Assert.*;

public class CollectionTest {
    private <T> List<T> toList(Iterable<T> it) {
        List<T> result = new ArrayList<>();
        for (T x : it) {
            result.add(x);
        }
        return result;
    }

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
            assertEquals(expected, toList(res));
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
            assertEquals(expected, toList(res));
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
            assertEquals(expected, toList(res));
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
            assertEquals(expected, toList(res));
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

    @Test
    public void testConcatMap() {
        final List<Integer[]> data = Arrays.asList(
                new Integer[] { 1, 2, 3 },
                new Integer[] { 4 },
                new Integer[] { },
                new Integer[] { 5, 6 }
        );
        Function1<Integer, Iterable<Integer>> f = new Function1<Integer, Iterable<Integer>>() {
            @Override
            public Iterable<Integer> apply(Integer arg) {
                return Arrays.asList(data.get(arg));
            }
        };

        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), toList(Collection.concatMap(f, Arrays.asList(0, 1, 2, 3))));
        assertEquals(Arrays.asList(5, 6, 4), toList(Collection.concatMap(f, Arrays.asList(2, 3, 1))));
    }

    private static class IteratingFunction extends Function1<Integer, Integer> {
        private int nextCall = 0;

        public int getNextCall() {
            return nextCall;
        }

        @Override
        public Integer apply(Integer arg) {
            assertEquals(nextCall, (int) arg);
            nextCall++;
            return nextCall;
        }
    };

    @Test
    public void testIterate() {
        IteratingFunction f = new IteratingFunction();
        Iterator<Integer> it = Collection.iterate(f, 0, 5).iterator();
        for (int i = 0; i < 5; i++) {
            assertTrue(it.hasNext());
            assertEquals(Math.max(0, i - 1), f.getNextCall());
            assertEquals(i, (int) it.next());
        }
        assertFalse(it.hasNext());
        assertEquals(4, f.getNextCall());
    }

    @Test
    public void testZipWith() {
        List<ValA> as = Arrays.asList(new ValA(1), new ValA(2), new ValA(3));
        List<ValB> bs = Arrays.asList(new ValB(4), new ValB(5), new ValB(6), new ValB(7));
        List<ValC> expected = Arrays.asList(new ValC(4), new ValC(10), new ValC(18));

        Function2<ValA, ValB, ValC> fabc = new Function2<ValA, ValB, ValC>() {
            @Override
            public ValC apply(ValA arg1, ValB arg2) {
                return new ValC(arg1.val * arg2.val);
            }
        };
        assertEquals(expected, toList(Collection.zipWith(fabc, as, bs)));

        Function2<ValB, ValA, ValC> fbac = new Function2<ValB, ValA, ValC>() {
            @Override
            public ValC apply(ValB arg1, ValA arg2) {
                return new ValC(arg1.val * arg2.val);
            }
        };
        assertEquals(expected, toList(Collection.zipWith(fbac, bs, as)));
    }
}
