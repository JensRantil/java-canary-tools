package io.github.jensrantil.tools.canary;

import com.google.common.hash.HashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WeightedShardedBuilderTest {
    // https://en.wikipedia.org/wiki/42_(number)#The_Hitchhiker's_Guide_to_the_Galaxy
    private static final HashCode TEST_HASH_CODE = HashCode.fromInt(42);

    @Test
    public void testBasicCreation() {
        TestInterface proxy =
                new WeightedShardedBuilder<TestInterface>()
                        .add(1, new TestImpl())
                        .add(100, new TestImpl())
                        .build(TestInterface.class, TEST_HASH_CODE);
        Assertions.assertNotNull(proxy);
    }

    interface TestInterface {
        void method(String key);
    }

    private static class TestImpl implements TestInterface {

        @Override
        public void method(String key) {
            // deliberately left empty.
        }
    }
}
