package io.github.jensrantil.tools.canary;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An InvocationHandler that delegates calls to the old implementation or new implementations. If
 * new implementations start to throw exceptions, calls will instead of regulated to be delegated to
 * the old implementation instead. In essence, this class acts as a circuit breaker allowing for
 * quick automated rollback to old interface implementation without operator intervention.
 *
 * <p>To create a new experiment, use {@link CircuitBreakerFallbackBuilder}.
 *
 * @param <T> the interface that the implementations are implementing.
 */
class CircuitBreakerFallbackDelegator<T> implements InvocationHandler {
    private final CircuitBreakerFallbackBuilder params;
    private final ImplementationWithFeedback oldImplementation;
    private final ImplementationWithFeedback newImplementation;

    public CircuitBreakerFallbackDelegator(
            CircuitBreakerFallbackBuilder params,
            T oldImplementation,
            T newImplementation) {

        this.params = params;
        this.oldImplementation =
                new ImplementationWithFeedback(
                        oldImplementation,
                        Observer.EMPTY_OBSERVER);
        this.newImplementation =
                new ImplementationWithFeedback(
                        newImplementation, params.observer);
    }

    // Immutable.
    private class ImplementationWithFeedback {
        public final T implementation;
        public final Observer observer;

        public ImplementationWithFeedback(T implementation, Observer observer) {

            this.implementation = implementation;
            this.observer = observer;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("implementation", implementation)
                    .add("observer", observer)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("params", params)
                .add("newImplementation", newImplementation)
                .add("oldImplementation", oldImplementation)
                .toString();
    }

    private ImmutableSet<Integer> buildIndexSet(int size) {
        ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
        for (int i = 0; i < size; i++) {
            builder.add(i);
        }
        return builder.build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final ImplementationWithFeedback implementation = pickImplementation();

        Object result;
        try {
            result = method.invoke(implementation.implementation, args);
        } catch (InvocationTargetException e) {
            implementation.observer.registerFailure();

            final Throwable actualUnwrappedException = e.getTargetException();
            throw actualUnwrappedException;
        }

        implementation.observer.registerSuccess();

        return result;
    }

    private ImplementationWithFeedback pickImplementation() {
        final boolean shouldExplore = params.random.nextDouble() < params.epsilon;
        if (shouldExplore) {
            return pickFullyRandomly();
        }
        return pickWeightedRandomly();
    }

    private ImplementationWithFeedback pickWeightedRandomly() {
        final Summary summary = newImplementation.observer.getSummary();
        if (summary.total == 0) {
            return pickFullyRandomly();
        }

        final double ratio = summary.calculateSuccessRatio();
        if (params.random.nextDouble() < ratio) {
            return newImplementation;
        }
        return oldImplementation;
    }

    private ImplementationWithFeedback pickFullyRandomly() {
        if (params.random.nextBoolean()) {
            return this.newImplementation;
        }
        return this.oldImplementation;
    }
}
