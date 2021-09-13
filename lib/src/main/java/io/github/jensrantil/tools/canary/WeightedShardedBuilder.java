package io.github.jensrantil.tools.canary;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;

/**
 * {@link WeightedShardedBuilder} constructs a Java proxy that implements a Java interface shared
 * between different implementations. The proxy delegates to downstream implementations using {@link
 * Object#hashCode()} of the first method argument, weighted by the downstream implementation. This
 * class is useful if you would like to try out a new Java implementation for a subset of, for
 * example, users.
 *
 * @param <T> the Java interface that the builder wraps.
 */
public class WeightedShardedBuilder<T> {
    private final ImmutableList<WeightedImplementation<T>> weightedImplementations;
    private final WeightedShardedSelector.ParameterSelector paramSelector;

    public WeightedShardedBuilder setParamSelector(
            WeightedShardedSelector.ParameterSelector paramSelector) {
        return new WeightedShardedBuilder(weightedImplementations, paramSelector);
    }

    public WeightedShardedBuilder() {
        this(ImmutableList.of(), WeightedShardedSelector.FIRST_PARAM_SELECTOR);
    }

    private WeightedShardedBuilder(
            ImmutableList<WeightedImplementation<T>> weightedImplementations,
            WeightedShardedSelector.ParameterSelector paramSelector) {
        this.weightedImplementations = weightedImplementations;
        this.paramSelector = paramSelector;
    }

    public WeightedShardedBuilder<T> add(int weight, T implementation) {
        final ImmutableList.Builder<WeightedImplementation<T>> builder =
                ImmutableList.builderWithExpectedSize(weightedImplementations.size() + 1);

        return new WeightedShardedBuilder(
                builder.addAll(weightedImplementations)
                        .add(new WeightedImplementation<T>(weight, implementation))
                        .build(),
                paramSelector);
    }

    /**
     * Build a proxy that delegates to the weighted implementations.
     *
     * @param type the Java interface that the proxy class should implement.
     * @param seed a seed to make sure that different experiment proxy instances don't map the same.
     *             Without setting this, there's a risk that all interfaces
     *             using a user identifier, have a high likelihood of segregating the users the same. This
     *             means that certain users always will get new features and/or certain users will see
     *             crashes more often than others.
     * @return a Proxy instance the delegates to the underlying weighted implementations based on the
     *             parameter selector.
     */
    public T build(Class<T> type, HashCode seed) {
        final WeightedShardedSelector<T> selector =
                new WeightedShardedSelector(type, seed, paramSelector, weightedImplementations);
        return Delegator.build(type, selector);
    }
}
