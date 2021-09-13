package io.github.jensrantil.tools.canary;

import java.lang.reflect.Method;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DelegatorTest {

    // https://en.wikipedia.org/wiki/42_(number)#The_Hitchhiker's_Guide_to_the_Galaxy
    private static final int TEST_SEED = 42;

    @Test
    public void testDelegator() {
        TestSelector selector = new TestSelector();
        TestInterface proxy = Delegator.build(TestInterface.class, selector);
        Random random = new Random(TEST_SEED);

        int iterations = 50000;
        for (int i = 0; i < iterations; i++) {
            int expectResult = random.nextInt();
            selector.result = new TestImplementation(expectResult);
            int invocationResult = proxy.method();

            Assertions.assertEquals(expectResult, invocationResult);
        }
    }

    private static class TestSelector implements Delegator.DelegateSelector<TestInterface> {
        TestInterface result;

        @Override
        public TestInterface select(Method method, Object[] args) {
            return result;
        }
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
    }
}
