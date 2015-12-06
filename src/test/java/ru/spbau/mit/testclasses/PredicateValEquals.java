package ru.spbau.mit.testclasses;

import ru.spbau.mit.Predicate;

public class PredicateValEquals<T extends Val> extends Predicate<T> {
    private final int value;
    private int timesCalled;

    public PredicateValEquals(int value) {
        this.value = value;
    }

    @Override
    public Boolean apply(T arg) {
        timesCalled++;
        return arg.val == value;
    }

    public int getTimesCalled() {
        return timesCalled;
    }
}
