package ru.spbau.mit;

public abstract class Function2<T1, T2, Res> {
    public abstract Res apply(T1 arg1, T2 arg2);

    public <Res2> Function2<T1, T2, Res2> compose(final Function1<? super Res, Res2> other) {
        return new Function2<T1, T2, Res2>() {
            @Override
            public Res2 apply(T1 arg1, T2 arg2) {
                return other.apply(Function2.this.apply(arg1, arg2));
            }
        };
    }

    public Function1<T2, Res> bind1(final T1 arg1) {
        return new Function1<T2, Res>() {
            @Override
            public Res apply(T2 arg2) {
                return Function2.this.apply(arg1, arg2);
            }
        };
    }

    public Function1<T1, Res> bind2(final T2 arg2) {
        return new Function1<T1, Res>() {
            @Override
            public Res apply(T1 arg1) {
                return Function2.this.apply(arg1, arg2);
            }
        };
    }

    public Function1<T1, Function1<T2, Res>> curry() {
        return new Function1<T1, Function1<T2, Res>>() {
            @Override
            public Function1<T2, Res> apply(T1 arg1) {
                return bind1(arg1);
            }
        };
    }
}
