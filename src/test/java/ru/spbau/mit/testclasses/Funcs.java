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

    public static final Function1<Val.ValA, Val.ValB> a2b = new Function1<Val.ValA, Val.ValB>() {
        @Override
        public Val.ValB apply(Val.ValA arg) {
            return new Val.ValB(arg.val);
        }
    };

    public static final Function1<Val.ValB, Val.ValC> b2c = new Function1<Val.ValB, Val.ValC>() {
        @Override
        public Val.ValC apply(Val.ValB arg) {
            return new Val.ValC(arg.val);
        }
    };
}
