package io.github.jensrantil.tools.canary;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.util.Random;

/**
 * {@link CircuitBreakerFallbackBuilder} allows you to do safe rollout of new Java `interface`
 * implementations. Use {@link CircuitBreakerFallbackBuilder} to construct a Java Proxy class that
 * wraps two implementations of the same Java interfaces, the _old_ implementation and the _new_
 * implementation. As long as the new implementation never throws any exception, the Proxy class
 * will start using it more and more instead of _old_. If _new_ starts throwing exceptions, the
 * Proxy class will quickly roll back to use the _old_ implementation.
 *
 * <p>This class is immutable. That is, it is thread-safe and you can create a builder as a template
 * and build upon it in real-time.
 */
public class CircuitBreakerFallbackBuilder implements Cloneable {
    // The fields here are not private to be accessible by ExperimentInvocationHandler.
    final Observer observer;
    final double epsilon;
    final Random random;
    final Clock clock;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("observer", observer)
                .add("epsilon", epsilon)
                .toString();
    }

    /** Instantiates a new {@link CircuitBreakerFallbackBuilder} with defaults. */
    public CircuitBreakerFallbackBuilder() {
        this(0.01, new Random(), Clock.systemUTC());
    }

    // Constructor needed mostly to use the same clock for this class as well as as the
    // FixedWindowObserver.
    private CircuitBreakerFallbackBuilder(
            double epsilon, Random random, Clock clock) {

        this(
                new FixedWindowObserver(clock, 10, Duration.ofSeconds(30)),
                epsilon,
                random,
                clock);
    }

    private CircuitBreakerFallbackBuilder(
            Observer observer, double epsilon, Random random, Clock clock) {

        this.observer = observer;
        this.epsilon = epsilon;
        this.random = random;
        this.clock = clock;
    }

    public CircuitBreakerFallbackBuilder observer(Observer b) {
        return new CircuitBreakerFallbackBuilder(
                b, this.epsilon, this.random, this.clock);
    }

    /**
     * Epsilon is the ratio of calls that will randomly explore a new implementation. Without
     * exploration, we risk getting stuck in the same implementation. Typically, epsilon is set to a
     * few percentage.
     *
     * @param epsilon a value between 0 <= epsilon <= 1.
     * @return a new copy of a {@link CircuitBreakerFallbackBuilder}.
     */
    public CircuitBreakerFallbackBuilder epsilon(double epsilon) {
        Preconditions.checkArgument(epsilon >= 0, "epsilon must be in range [0,1]");
        Preconditions.checkArgument(epsilon <= 1, "epsilon must be in range [0,1]");

        return new CircuitBreakerFallbackBuilder(
                this.observer, epsilon, this.random, this.clock);
    }

    @VisibleForTesting
    public CircuitBreakerFallbackBuilder random(Random random) {
        return new CircuitBreakerFallbackBuilder(
                this.observer, this.epsilon, random, this.clock);
    }

    @VisibleForTesting
    public CircuitBreakerFallbackBuilder clock(Clock clock) {
        return new CircuitBreakerFallbackBuilder(
                this.observer, this.epsilon, this.random, clock);
    }

    /**
     * Build an experiment.
     *
     * @param type the interface to wrap
     * @param oldImplementation the old implementation which we will migrate back to in case new
     *     implementation fails
     * @param newImplementation the new implementation which we will migrate to if the new
     *     implementation doesn't fail
     * @param <T> the interface type that the proxy implements
     * @return a wrapped implementation of type.
     */
    public <T> T build(
            Class<T> type,
            T oldImplementation,
            T newImplementation) {
        Preconditions.checkArgument(type.isInterface(), "T must be an interface");
        return (T)
                Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[] {type},
                        new CircuitBreakerFallbackDelegator(this, oldImplementation, newImplementation));
    }
}
