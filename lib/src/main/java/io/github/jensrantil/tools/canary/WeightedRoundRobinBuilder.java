package io.github.jensrantil.tools.canary;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Random;

/**
 * {@link WeightedRoundRobinBuilder} constructs a Java proxy that implements a Java interface shared
 * between different implementations. The proxy delegates to downstream implementations randomly
 * using weights. This class is useful if you'd like to try out a new Java implementation for a
 * subset of calls.
 *
 * @param <T> the Java interface that the builder wraps.
 */
public class WeightedRoundRobinBuilder<T> {
    private final Random random;
    private ImmutableList<WeightedImplementation<T>> weightedImplementations;

    public WeightedRoundRobinBuilder() {
        this(new Random(), ImmutableList.of());
    }

    private WeightedRoundRobinBuilder(
            Random random, ImmutableList<WeightedImplementation<T>> weightedImplementations) {
        this.random = random;
        this.weightedImplementations = weightedImplementations;
    }

    @VisibleForTesting
    public WeightedRoundRobinBuilder<T> random(Random random) {
        return new WeightedRoundRobinBuilder(random, this.weightedImplementations);
    }

    public WeightedRoundRobinBuilder<T> add(int weight, T implementation) {
        final ImmutableList.Builder<WeightedImplementation<T>> builder =
                ImmutableList.builderWithExpectedSize(weightedImplementations.size() + 1);

        return new WeightedRoundRobinBuilder(
                random,
                builder.addAll(weightedImplementations)
                        .add(new WeightedImplementation<T>(weight, implementation))
                        .build());
    }

    public T build(Class<T> type) {
        final WeightedRoundRobinSelector<T> selector =
                new WeightedRoundRobinSelector<>(random, weightedImplementations);
        return Delegator.build(type, selector);
    }
}
