package ru.spbau.mit.testclasses;

import ru.spbau.mit.Function1;

public class Funcs {
    public static final Function1<Val, Val> inc = new Function1<Val, Val>() {
        @Override
        public Val apply(Val arg) {
            return new Val(arg.val + 1);
        }
    };

    public static final Function1<Val, Val> times2 = new Function1<Val, Val>() {
        @Override
        public Val apply(Val arg) {
            return new Val(2 * arg.val);
        }
    };

    public static final Function1<ValA, ValB> a2b = new Function1<ValA, ValB>() {
        @Override
        public ValB apply(ValA arg) {
            return new ValB(arg.val);
        }
    };

    public static final Function1<ValB, ValC> b2c = new Function1<ValB, ValC>() {
        @Override
        public ValC apply(ValB arg) {
            return new ValC(arg.val);
        }
    };
}
