package ru.spbau.mit;

import static org.junit.Assert.*;
import static ru.spbau.mit.testclasses.Funcs.*;
import static ru.spbau.mit.testclasses.Val.*;

import org.junit.Test;
import ru.spbau.mit.testclasses.*;

public class Function2Test {
    private Function2<ValA, ValB, ValC> aPlusBTimes3 = new Function2<ValA, ValB, ValC>() {
        @Override
        public ValC apply(ValA arg1, ValB arg2) {
            return new ValC(arg1.val + arg2.val * 3);
        }
    };

    @Test
    public void testComposeResultAndBounds() {
        Function2<ValA, ValB, Val> f = aPlusBTimes3.compose(times2);
        assertEquals(new Val(90), f.apply(new ValA(12), new ValB(11)));
    }

    @Test
    public void testBind1() {
        Function1<ValB, ValC> f = aPlusBTimes3.bind1(new ValA(12));
        assertEquals(new ValC(45), f.apply(new ValB(11)));
        assertEquals(new ValC(48), f.apply(new ValB(12)));
    }

    @Test
    public void testBind2() {
        Function1<ValA, ValC> f = aPlusBTimes3.bind2(new ValB(11));
        assertEquals(new ValC(45), f.apply(new ValA(12)));
        assertEquals(new ValC(46), f.apply(new ValA(13)));
    }

    @Test
    public void testCurry() {
        Function1<ValA, Function1<ValB, ValC>> f = aPlusBTimes3.curry();
        Function1<ValB, ValC> f12 = f.apply(new ValA(12));
        Function1<ValB, ValC> f13 = f.apply(new ValA(13));
        assertEquals(new ValC(45), f12.apply(new ValB(11)));
        assertEquals(new ValC(46), f13.apply(new ValB(11)));
    }

    @Test
    public void testFlip() {
        Function2<ValB, ValA, ValC> f = aPlusBTimes3.flip();
        assertEquals(new ValC(45), f.apply(new ValB(11), new ValA(12)));
    }
}