package io.github.jensrantil.tools.canary;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.lang.reflect.Method;
import java.util.List;
import java.util.TreeMap;

class WeightedShardedSelector<T> implements Delegator.DelegateSelector {
    private final int total;
    private final ImmutableSortedMap<Integer, T> indexByWeightInterval;
    private final HashCode seed;
    private final ParameterSelector paramSelector;

    public static final ParameterSelector FIRST_PARAM_SELECTOR = new FirstParamSelector();

    interface ParameterSelector {
        Object pick(Method method, Object[] args);

        void validateType(Class<?> clazz);
    }

    public WeightedShardedSelector(
            Class<T> type,
            HashCode seed,
            ParameterSelector paramSelector,
            List<WeightedImplementation<T>> implementations) {

        final TreeMap<Integer, T> indexByWeightInterval = Maps.newTreeMap();
        int total = 0;
        for (WeightedImplementation<T> implementation : implementations) {
            // Without this precondition, we risk overwriting alternatives in the map.
            Preconditions.checkState(implementation.weight > 0);

            indexByWeightInterval.put(total + implementation.weight, implementation.implementation);
            total += implementation.weight;
        }
        this.indexByWeightInterval = ImmutableSortedMap.copyOf(indexByWeightInterval);

        this.seed = seed;
        this.total = total;
        this.paramSelector = paramSelector;

        this.paramSelector.validateType(type);
    }

    @Override
    public T select(Method method, Object[] args) {
        final Object firstArgument = paramSelector.pick(method, args);
        HashCode hashCode =
                Hashing.combineOrdered(
                        ImmutableList.of(seed, HashCode.fromInt(firstArgument.hashCode())));
        final int bucket = Hashing.consistentHash(hashCode, total);
        return indexByWeightInterval.tailMap(bucket, false).firstEntry().getValue();
    }
}

class FirstParamSelector implements WeightedShardedSelector.ParameterSelector {
    public Object pick(Method method, Object[] args) {
        return args[0];
    }

    public void validateType(Class<?> type) {
        for (Method method : type.getMethods()) {
            Preconditions.checkArgument(
                    method.getParameterCount() > 0,
                    "Type %s has method %s with no argument. Must have a single argument as the first argument is used to know which implementation to delegate to.",
                    type,
                    method);
        }
    }
}

