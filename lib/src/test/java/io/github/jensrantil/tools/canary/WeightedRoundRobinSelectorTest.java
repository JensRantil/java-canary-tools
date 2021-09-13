package io.github.jensrantil.tools.canary;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.Objects;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WeightedRoundRobinSelectorTest {
    @Test
    public void testBasicCall() {
        ImmutableList<WeightedImplementation<TestInterface>> wImplementations =
                ImmutableList.of(
                        new WeightedImplementation(1, new TestImplementation(1)),
                        new WeightedImplementation(999, new TestImplementation(2)));
        WeightedRoundRobinSelector<TestInterface> selector =
                new WeightedRoundRobinSelector(new Random(42), wImplementations);

        TestInterface delegate = selector.select(null, new Object[] {});
        Assertions.assertNotNull(delegate);
    }

    @Test
    public void testDistribution() {
        ImmutableList<WeightedImplementation<TestInterface>> wImplementations =
                ImmutableList.of(
                        new WeightedImplementation(1, new TestImplementation(1)),
                        new WeightedImplementation(99, new TestImplementation(2)));
        WeightedRoundRobinSelector<TestInterface> selector =
                new WeightedRoundRobinSelector(new Random(42), wImplementations);

        HashMultiset<TestInterface> counts = HashMultiset.create();
        int iterations = 500000;
        for (int i = 0; i < iterations; i++) {
            TestInterface delegate = selector.select(null, new Object[] {});
            Assertions.assertNotNull(delegate);
            counts.add(delegate);
        }

        Assertions.assertEquals(wImplementations.size(), counts.elementSet().size());
        for (Multiset.Entry<TestInterface> e : counts.entrySet()) {
            System.out.println(e.getElement() + " " + e.getCount() + " " + e.getElement().method());
        }

        Integer callsToFirstImplementation =
                counts.entrySet().stream()
                        .filter(e -> e.getElement().method() == 1)
                        .map(e -> e.getCount())
                        .findFirst()
                        .get();
        Assertions.assertEquals(0.01, 1.0 * callsToFirstImplementation / counts.size(), 0.001);
    }

    interface TestInterface {
        int method();
    }

    private static class TestImplementation implements TestInterface {
        public TestImplementation(int result) {
            this.result = result;
        }

        private int result;

        @Override
        public int method() {
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

        @Override
        public String toString() {
            return com.google.common.base.MoreObjects.toStringHelper(this)
                    .add("result", result)
                    .toString();
        }
    }
}
