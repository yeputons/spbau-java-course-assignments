package ru.spbau.mit.testClasses;

public class ClassWithTwoClassDependencies {
    public final ClassWithoutDependencies a, b;

    public ClassWithTwoClassDependencies(ClassWithoutDependencies a, ClassWithoutDependencies b) {
        this.a = a;
        this.b = b;
    }
}
