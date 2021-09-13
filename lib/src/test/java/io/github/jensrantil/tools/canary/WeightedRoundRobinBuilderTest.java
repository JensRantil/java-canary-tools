package io.github.jensrantil.tools.canary;

import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WeightedRoundRobinBuilderTest {
    @Test
    public void testBasicCreation() {
        TestInterface proxy =
                new WeightedRoundRobinBuilder<TestInterface>()
                        .add(1, new TestImpl())
                        .add(100, new TestImpl())
                        .random(new Random(42))
                        .build(TestInterface.class);
        Assertions.assertNotNull(proxy);
    }

    interface TestInterface {
        void method();
    }

    private static class TestImpl implements TestInterface {

        @Override
        public void method() {
            // deliberately left empty.
        }
    }
}
