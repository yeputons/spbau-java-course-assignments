package ru.spbau.mit;

import static org.junit.Assert.*;
import static ru.spbau.mit.testclasses.Funcs.*;

import org.junit.Test;
import ru.spbau.mit.testclasses.*;

public class Function1Test {
    @Test
    public void testComposeOrder() {
        Function1<Val, Val> f = inc.compose(times2);
        assertEquals(new Val(6), f.apply(new Val(2)));
    }

    @Test
    public void testComposeTypes() {
        Function1<ValA, ValC> f = a2b.compose(b2c);
        assertEquals(new ValC(10), f.apply(new ValA(10)));
    }

    @Test
    public void testComposeTypesBounds() {
        Function1<ValA, Val> f = a2b.compose(inc);
        assertEquals(new Val(11), f.apply(new ValA(10)));
    }
}