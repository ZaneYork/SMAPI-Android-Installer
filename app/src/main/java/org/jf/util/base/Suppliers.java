package org.jf.util.base;


import java.io.Serializable;
import java.util.function.Supplier;

public class Suppliers {
    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        if (delegate instanceof Suppliers.MemoizingSupplier) {
            return delegate;
        }
        return new MemoizingSupplier<T>(delegate);
    }
    static class MemoizingSupplier<T> implements Supplier<T>, Serializable {
        final Supplier<T> delegate;
        transient volatile boolean initialized;
        transient T value;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            return "Suppliers.memoize("
                    + (initialized ? "<supplier that returned " + value + ">" : delegate)
                    + ")";
        }

        private static final long serialVersionUID = 0;
    }
}
