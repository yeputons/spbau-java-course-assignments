package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Collections {
    public static <T, S> Iterable<S> map(final Function1<? super T, S> f, final Iterable<T> source) {
        return new Iterable<S>() {
            @Override
            public Iterator<S> iterator() {
                return new Iterator<S>() {
                    private final Iterator<T> it = source.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public S next() {
                        return f.apply(it.next());
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }
        };
    }

    public static <T> Iterable<T> filter(final Predicate<? super T> p, final Iterable<T> source) {
        /**
         * I was unable to come up with generator-like solution because of <code>remove</code>:
         * - It forces me to use a single iterator from <code>source</code> only
         * - It's possible to receive <code>hasNext</code> followed by <code>remove</code>.
         *   I should forward the inner iterator during the first call (to find out next satisfying
         *   element), but during the second call I should somehow 'return' it back to remove the
         *   last element. I don't think it's possible.
         */
        List<T> result = new ArrayList<>();
        for (T item : source) {
            if (p.apply(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public static <T> Iterable<T> takeWhile(final Predicate<? super T> p, final Iterable<T> source) {
        /**
         * Here we have the same problem as in <code>filter</code>: I cannot implement both
         * <code>hasNext</code> and <code>remove</code> simultaneously
         */
        List<T> result = new ArrayList<>();
        for (T item : source) {
            if (!p.apply(item)) {
                break;
            }
            result.add(item);
        }
        return result;
    }

    public static <T> Iterable<T> takeUnless(final Predicate<? super T> p, final Iterable<T> source) {
        return takeWhile(p.not(), source);
    }

    public static<T, Res> Res foldr(final Function2<? super T, ? super Res, Res> f, final Iterable<T> source, final Res initial) {
        /**
         * It's ok to store everything to ArrayList, as we have to retrieve all elements from the iterator
         * anyway before we will be able to make a single call of <code>f</code>
         */
        List<T> data = new ArrayList<>();
        Iterator<T> it = source.iterator();
        while (it.hasNext()) {
            data.add(it.next());
        }
        Res result = initial;
        for (int i = data.size() - 1; i >= 0; i--) {
            result = f.apply(data.get(i), result);
        }
        return result;
    }

    public static<T, Res> Res foldl(final Function2<? super Res, ? super T, Res> f, final Res initial, final Iterable<T> source) {
        Iterator<T> it = source.iterator();
        Res result = initial;
        while (it.hasNext()) {
            result = f.apply(result, it.next());
        }
        return result;
    }
}
