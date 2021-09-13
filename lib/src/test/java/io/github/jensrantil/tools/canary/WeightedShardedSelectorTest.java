package io.github.jensrantil.tools.canary;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import java.util.Objects;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WeightedShardedSelectorTest {

    // https://en.wikipedia.org/wiki/42_(number)#The_Hitchhiker's_Guide_to_the_Galaxy
    private static final int TEST_SEED = 42;
    private static final HashCode TEST_HASH_CODE_SEED = HashCode.fromInt(43);

    @Test
    public void testBasicCall() {
        ImmutableList<WeightedImplementation<TestInterface>> wImplementations =
                ImmutableList.of(
                        new WeightedImplementation(1, new TestImplementation(1)),
                        new WeightedImplementation(999, new TestImplementation(2)));
        WeightedShardedSelector<TestInterface> selector =
                new WeightedShardedSelector(
                        TestInterface.class,
                        TEST_HASH_CODE_SEED,
                        WeightedShardedSelector.FIRST_PARAM_SELECTOR,
                        wImplementations);

        TestInterface delegate = selector.select(null, new Object[] {"key"});
        Assertions.assertNotNull(delegate);
    }

    @Test
    public void testDistribution() {
        ImmutableList<WeightedImplementation<TestInterface>> wImplementations =
                ImmutableList.of(
                        new WeightedImplementation(1, new TestImplementation(1)),
                        new WeightedImplementation(99, new TestImplementation(2)));
        WeightedShardedSelector<TestInterface> selector =
                new WeightedShardedSelector(
                        TestInterface.class,
                        TEST_HASH_CODE_SEED,
                        WeightedShardedSelector.FIRST_PARAM_SELECTOR,
                        wImplementations);

        HashMultiset<TestInterface> counts = HashMultiset.create();
        int iterations = 500000;
        Random random = new Random(TEST_SEED);
        for (int i = 0; i < iterations; i++) {
            TestInterface delegate = selector.select(null, new Object[] {random.nextInt()});
            Assertions.assertNotNull(delegate);
            counts.add(delegate);
        }

        Integer callsToFirstImplementation =
                counts.entrySet().stream()
                        .filter(e -> e.getElement() == wImplementations.get(0).implementation)
                        .map(e -> e.getCount())
                        .findFirst()
                        .get();
        Assertions.assertEquals(0.01, 1.0 * callsToFirstImplementation / counts.size(), 0.001);
    }

    @Test
    public void testIllegalArgumentOnWrappingMethodWithNoArguments() {
        ImmutableList<WeightedImplementation<InvalidInterface>> implementations =
                ImmutableList.of(
                        new WeightedImplementation<>(1, new InvalidImpl()),
                        new WeightedImplementation<>(100, new InvalidImpl()));
        try {
            new WeightedShardedSelector<>(
                    InvalidInterface.class,
                    TEST_HASH_CODE_SEED,
                    WeightedShardedSelector.FIRST_PARAM_SELECTOR,
                    implementations);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assertions.fail("expected IllegalArgumentException to be thrown.");
    }

    @Test
    public void testIllegalArgumentOnWrappingExtendedMethodWithNoArguments() {
        ImmutableList<WeightedImplementation<IndirectInvalidInterface>> implementations =
                ImmutableList.of(
                        new WeightedImplementation<>(1, new IndirectInvalidImpl()),
                        new WeightedImplementation<>(100, new IndirectInvalidImpl()));
        try {
            new WeightedShardedSelector<>(
                    IndirectInvalidInterface.class,
                    TEST_HASH_CODE_SEED,
                    WeightedShardedSelector.FIRST_PARAM_SELECTOR,
                    implementations);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assertions.fail("expected IllegalArgumentException to be thrown.");
    }

    interface IndirectInvalidInterface extends InvalidInterface {
        void methodWithParameter(String param);
    }

    private static class IndirectInvalidImpl implements IndirectInvalidInterface {

        @Override
        public void methodWithParameter(String param) {
            // deliberately left empty
        }

        @Override
        public void methodWithoutParameter() {
            // deliberately left empty
        }
    }

    interface InvalidInterface {
        void methodWithoutParameter();
    }

    private static class InvalidImpl implements InvalidInterface {
        @Override
        public void methodWithoutParameter() {
            // deliberately left empty
        }
    }

    interface TestInterface {
        int method(Integer param);
    }

    private static class TestImplementation implements TestInterface {
        public TestImplementation(int result) {
            this.result = result;
        }

        private int result;

        @Override
        public int method(Integer param) {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestImplementation that = (TestImplementation) o;
            return result == that.result;
        }

        @Override
        public int hashCode() {
            return Objects.hash(result);
        }
    }
}
