package ru.spbau.mit;

import org.junit.Test;
import ru.spbau.mit.testclasses.*;
import static ru.spbau.mit.testclasses.Val.*;

import static org.junit.Assert.*;

public class PredicateTest {
    @Test
    public void testAlwaysTrue() {
        assertTrue(Predicate.ALWAYS_TRUE.apply(null));
        assertTrue(Predicate.ALWAYS_TRUE.apply(false));
        assertTrue(Predicate.ALWAYS_TRUE.apply(Predicate.ALWAYS_TRUE));
    }

    @Test
    public void testAlwaysFalse() {
        assertFalse(Predicate.ALWAYS_FALSE.apply(null));
        assertFalse(Predicate.ALWAYS_FALSE.apply(false));
        assertFalse(Predicate.ALWAYS_FALSE.apply(Predicate.ALWAYS_FALSE));
    }

    @Test
    public void testNot() {
        PredicateValEquals<ValA> equals2 = new PredicateValEquals<>(2);
        Predicate<ValA> notEquals2 = equals2.not();
        assertEquals(0, equals2.getTimesCalled());

        assertFalse(notEquals2.apply(new ValA(2)));
        assertEquals(1, equals2.getTimesCalled());

        assertTrue(notEquals2.apply(new ValA(3)));
        assertEquals(2, equals2.getTimesCalled());
    }

    @Test
    public void testOrLazy() {
        PredicateValEquals<ValA> equals2a = new PredicateValEquals<>(2);
        PredicateValEquals<ValA> equals2b = new PredicateValEquals<>(2);
        PredicateValEquals<ValA> equals5 = new PredicateValEquals<>(5);

        Predicate<ValA> equals2Or5 = equals2a.or(equals2b).or(equals5);
        assertEquals(0, equals2a.getTimesCalled());
        assertEquals(0, equals2b.getTimesCalled());
        assertEquals(0, equals5.getTimesCalled());

        assertTrue(equals2Or5.apply(new ValA(2)));
        assertEquals(1, equals2a.getTimesCalled());
        assertEquals(0, equals2b.getTimesCalled());
        assertEquals(0, equals5.getTimesCalled());

        assertTrue(equals2Or5.apply(new ValA(5)));
        assertEquals(2, equals2a.getTimesCalled());
        assertEquals(1, equals2b.getTimesCalled());
        assertEquals(1, equals5.getTimesCalled());

        assertFalse(equals2Or5.apply(new ValA(3)));
        assertEquals(3, equals2a.getTimesCalled());
        assertEquals(2, equals2b.getTimesCalled());
        assertEquals(2, equals5.getTimesCalled());
    }

    @Test
    public void testOrInheritance() {
        PredicateValEquals<Val> equals2 = new PredicateValEquals<>(2);
        PredicateValEquals<ValA> equals5 = new PredicateValEquals<>(5);

        Predicate<ValA> equalsA = equals2.or(equals5);
        assertTrue(equalsA.apply(new ValA(2)));
        assertTrue(equalsA.apply(new ValA(5)));
        assertFalse(equalsA.apply(new ValA(3)));

        Predicate<ValA> equalsB = equals5.or(equals2);
        assertTrue(equalsB.apply(new ValA(2)));
        assertTrue(equalsB.apply(new ValA(5)));
        assertFalse(equalsB.apply(new ValA(3)));
    }

    @Test
    public void testAndLazy() {
        PredicateValGreaterOrEquals<ValA> ge2 = new PredicateValGreaterOrEquals<>(2);
        PredicateValLessOrEquals<ValA> le4 = new PredicateValLessOrEquals<>(4);

        Predicate<ValA> between2And4 = ge2.and(le4);
        assertEquals(0, ge2.getTimesCalled());
        assertEquals(0, le4.getTimesCalled());

        assertFalse(between2And4.apply(new ValA(1)));
        assertEquals(1, ge2.getTimesCalled());
        assertEquals(0, le4.getTimesCalled());

        assertTrue(between2And4.apply(new ValA(2)));
        assertEquals(2, ge2.getTimesCalled());
        assertEquals(1, le4.getTimesCalled());

        assertTrue(between2And4.apply(new ValA(3)));
        assertEquals(3, ge2.getTimesCalled());
        assertEquals(2, le4.getTimesCalled());

        assertTrue(between2And4.apply(new ValA(4)));
        assertEquals(4, ge2.getTimesCalled());
        assertEquals(3, le4.getTimesCalled());

        assertFalse(between2And4.apply(new ValA(5)));
        assertEquals(5, ge2.getTimesCalled());
        assertEquals(4, le4.getTimesCalled());
    }

    @Test
    public void testAndInheritance() {
        PredicateValGreaterOrEquals<ValA> ge2 = new PredicateValGreaterOrEquals<>(2);
        PredicateValLessOrEquals<Val> le4 = new PredicateValLessOrEquals<>(4);

        Predicate<ValA> cmpA = ge2.and(le4);
        assertTrue(cmpA.apply(new ValA(3)));
        assertFalse(cmpA.apply(new ValA(1)));
        assertFalse(cmpA.apply(new ValA(5)));

        Predicate<ValA> cmpB = le4.and(ge2);
        assertTrue(cmpB.apply(new ValA(3)));
        assertFalse(cmpB.apply(new ValA(1)));
        assertFalse(cmpB.apply(new ValA(5)));
    }
}