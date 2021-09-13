package io.github.jensrantil.tools.canary;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

class WeightedRoundRobinSelector<T> implements Delegator.DelegateSelector {
    private final Random random;
    private final int total;
    private final ImmutableSortedMap<Integer, T> indexByWeightInterval;

    public WeightedRoundRobinSelector(
            Random random, List<WeightedImplementation<T>> implementations) {

        final TreeMap<Integer, T> indexByWeightInterval = Maps.newTreeMap();
        int total = 0;
        for (WeightedImplementation<T> implementation : implementations) {
            // Without this precondition, we risk overwriting alternatives in the map.
            Preconditions.checkState(implementation.weight > 0);

            indexByWeightInterval.put(total + implementation.weight, implementation.implementation);
            total += implementation.weight;
        }

        this.indexByWeightInterval = ImmutableSortedMap.copyOf(indexByWeightInterval);
        this.random = random;
        this.total = total;
    }

    @Override
    public T select(Method method, Object[] args) {
        final int randomValue = random.nextInt(total);
        return indexByWeightInterval.tailMap(randomValue, false).firstEntry().getValue();
    }
}
