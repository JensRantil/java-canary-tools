package io.github.jensrantil.tools.canary;

import com.google.common.base.Preconditions;

/**
 * Weighted implementation.
 *
 * <p>Immutable class.
 *
 * @param <T>
 */
class WeightedImplementation<T> {
    public final int weight;
    public final T implementation;

    public WeightedImplementation(int weight, T implementation) {
        Preconditions.checkArgument(weight > 0, "weight must be strictly larger than zero.");
        this.weight = weight;
        this.implementation = Preconditions.checkNotNull(implementation);
    }
}
