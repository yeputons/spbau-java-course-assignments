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
}
