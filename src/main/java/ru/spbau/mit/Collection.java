package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Collection {
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
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static <T> Iterable<T> filter(final Predicate<? super T> p, final Iterable<T> source) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private final Iterator<T> it = source.iterator();
                    private T value = null;
                    private boolean hasValue = false;

                    @Override
                    public boolean hasNext() {
                        findNext();
                        return hasValue;
                    }

                    @Override
                    public T next() {
                        findNext();
                        if (!hasValue) {
                            throw new NoSuchElementException();
                        }
                        T result = value;
                        value = null;
                        hasValue = false;
                        return result;
                    }

                    private void findNext() {
                        if (hasValue) {
                            return;
                        }
                        while (it.hasNext()) {
                            T current = it.next();
                            if (p.apply(current)) {
                                value = current;
                                hasValue = true;
                                return;
                            }
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static <T> Iterable<T> takeWhile(final Predicate<? super T> p, final Iterable<T> source) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private Iterator<T> it = source.iterator();
                    private T value = null;
                    private boolean hasValue = false;

                    @Override
                    public boolean hasNext() {
                        findNext();
                        return hasValue;
                    }

                    @Override
                    public T next() {
                        findNext();
                        if (!hasValue) {
                            throw new NoSuchElementException();
                        }
                        T result = value;
                        value = null;
                        hasValue = false;
                        return result;
                    }

                    private void findNext() {
                        if (hasValue) {
                            return;
                        }
                        if (it == null || !it.hasNext()) {
                            return;
                        }
                        T current = it.next();
                        if (p.apply(current)) {
                            value = current;
                            hasValue = true;
                        } else {
                            it = null;
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
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
