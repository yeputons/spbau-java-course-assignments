package ru.spbau.mit.testclasses;

public class Val {
    public final int val;

    public Val(int val) {
        this.val = val;
    }

    @Override
    public int hashCode() {
        return val * 239017 + getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", getClass().getSimpleName(), val);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Val other = (Val) obj;
        return val == other.val;
    }

    public static class ValA extends Val {
        public ValA(int val) {
            super(val);
        }
    }

    public static class ValB extends Val {
        public ValB(int val) {
            super(val);
        }
    }

    public static class ValC extends Val {
        public ValC(int val) {
            super(val);
        }
    }
}
