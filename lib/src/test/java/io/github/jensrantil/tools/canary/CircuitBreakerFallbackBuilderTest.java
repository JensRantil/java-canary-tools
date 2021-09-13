package io.github.jensrantil.tools.canary;

import java.time.Instant;
import java.util.List;
import io.github.jensrantil.tools.canary.simulation.Sample;
import io.github.jensrantil.tools.canary.simulation.SimulationParameters;
import io.github.jensrantil.tools.canary.simulation.Simulator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CircuitBreakerFallbackBuilderTest {

    @Test
    public void testBasicCreation() {
        TestImpl oldImplementation = new TestImpl();
        TestImpl newImplementation = new TestImpl();
        TestInterface proxy =
                new CircuitBreakerFallbackBuilder()
                        .build(
                                TestInterface.class,
                                oldImplementation,
                                newImplementation);
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

    @Test
    public void testInitialCallsUseNewImplementation() {
        final SimulationParameters params = new SimulationParameters();
        final Instant start = Instant.now();
        List<Sample> samples = new Simulator().simulate(start, params);

        Assertions.assertTrue(samples.size() > 0);
        List<Sample> samplesFromPhase1 = samples.subList(0, params.phaseShift);
        long nNewImplCalls = samplesFromPhase1.stream().filter(s -> s.newImpl).count();
        double ratioNewImplCalls = 1.0 * nNewImplCalls / samplesFromPhase1.size();
        Assertions.assertTrue(ratioNewImplCalls > 0.95);
    }

    @Test
    public void testInitialCallsUseOldImplementation() {
        final SimulationParameters params = new SimulationParameters();
        final Instant start = Instant.now();
        List<Sample> samples = new Simulator().simulate(start, params);

        Assertions.assertTrue(samples.size() > 0);
        List<Sample> samplesFromPhase2 = samples.subList(params.phaseShift, params.steps);
        long nOldImplCalls = samplesFromPhase2.stream().filter(s -> !s.newImpl).count();
        double ratioOldImplCalls = 1.0 * nOldImplCalls / samplesFromPhase2.size();
        Assertions.assertTrue(ratioOldImplCalls > 0.95);
    }
}
