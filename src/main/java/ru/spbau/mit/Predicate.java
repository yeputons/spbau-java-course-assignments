package ru.spbau.mit;

public abstract class Predicate<T> extends Function1<T, Boolean> {
    public static Predicate<Object> ALWAYS_TRUE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object arg) {
            return true;
        }
    };

    public static Predicate<Object> ALWAYS_FALSE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object arg) {
            return false;
        }
    };

    public <R extends T> Predicate<R> or(final Predicate<? super R> other) {
        return new Predicate<R>() {
            @Override
            public Boolean apply(R arg) {
                return Predicate.this.apply(arg) || other.apply(arg);
            }
        };
    }

    public <R extends T> Predicate<R> and(final Predicate<? super R> other) {
        return new Predicate<R>() {
            @Override
            public Boolean apply(R arg) {
                return Predicate.this.apply(arg) && other.apply(arg);
            }
        };
    }

    public Predicate<T> not() {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T arg) {
                return !Predicate.this.apply(arg);
            }
        };
    }
}
