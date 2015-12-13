package ru.spbau.mit;

import static org.junit.Assert.*;
import static ru.spbau.mit.testclasses.Funcs.*;
import static ru.spbau.mit.testclasses.Val.*;

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

    @Test
    public void testUncurry() {
        Function1<ValA, Function1<ValB, ValC> > f = new Function1<ValA, Function1<ValB, ValC>>() {
            @Override
            public Function1<ValB, ValC> apply(final ValA arg1) {
                return new Function1<ValB, ValC>() {
                    @Override
                    public ValC apply(ValB arg2) {
                        return new ValC(arg1.val + arg2.val * 3);
                    }
                };
            }
        };
        Function2<ValA, ValB, ValC> f2 = Function1.uncurry(f);
        assertEquals(new ValC(45), f2.apply(new ValA(12), new ValB(11)));
    }
}