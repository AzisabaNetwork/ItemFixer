package net.azisaba.itemFixer.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Chain<T> {
    private final T value;
    private final boolean notNull;

    private Chain(T value, boolean notNull) {
        this.value = value;
        this.notNull = notNull;
    }

    public Chain(T value) {
        this(value, false);
    }

    public Chain<T> apply(Consumer<T> action) {
        if (notNull && value == null) return this;
        action.accept(value);
        return this;
    }

    public <R> Chain<R> let(Function<T, R> function) {
        if (notNull && value == null) return new Chain<>(null);
        return new Chain<>(function.apply(value));
    }

    /**
     * Make sure the methods like apply and let will not be called on next invocation if value is null.
     * @return "NotNull" Chain
     */
    public Chain<T> notNull() {
        if (notNull) return this;
        return new Chain<>(value, true);
    }

    public T getValue() {
        return value;
    }

    public static <T> Chain<T> of(T value) {
        return new Chain<>(value);
    }
}
