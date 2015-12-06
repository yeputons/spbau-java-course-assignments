package ru.spbau.mit;

public abstract class Function1<T, Res> {
    public abstract Res apply(T arg);

    public <Res2> Function1<T, Res2> compose(final Function1<? super Res, Res2> other) {
        return new Function1<T, Res2>() {
            @Override
            public Res2 apply(T arg) {
                return other.apply(Function1.this.apply(arg));
            }
        };
    }

    public static <T1, T2, Res> Function2<T1, T2, Res> uncurry(final Function1<T1, Function1<T2, Res>> f) {
        return new Function2<T1, T2, Res>() {
            @Override
            public Res apply(T1 arg1, T2 arg2) {
                return f.apply(arg1).apply(arg2);
            }
        };
    }
}
