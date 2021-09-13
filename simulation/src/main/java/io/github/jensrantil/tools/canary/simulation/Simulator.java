package io.github.jensrantil.tools.canary.simulation;

import com.google.common.collect.Lists;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import io.github.jensrantil.tools.canary.CircuitBreakerFallbackBuilder;
import io.github.jensrantil.tools.canary.FixedWindowObserver;

/**
 * Simulator runs a simulation of an experiment. Call {@link #simulate(Instant,
 * SimulationParameters)} to run the actual simulation.
 */
public class Simulator {

    /**
     * Runs the simulation. The simulation is done in two phases to test how the feedback loop works
     * when something starts failing.
     *
     * @param start the time the simulation should start.
     * @param params the parameters used for the simulation.
     * @return a list simulation invocations and informations about them.
     */
    public List<Sample> simulate(final Instant start, SimulationParameters params) {
        final Random random = new Random(params.seed);
        final SimulationClock simulationClock =
                new SimulationClock(Clock.fixed(start, ZoneOffset.UTC));

        final TestImplementation origImpl =
                new TestImplementation(random, false, params.phase1.origImpl.errorRatio);
        final TestImplementation newImpl =
                new TestImplementation(random, true, params.phase1.newImpl.errorRatio);

        CircuitBreakerFallbackBuilder builder =
                new CircuitBreakerFallbackBuilder()
                        .random(random)
                        .clock(simulationClock)
                        .observer(
                                new FixedWindowObserver(
                                        simulationClock, params.slots, params.slotDuration));
        if (params.epsilon != null) {
            builder = builder.epsilon(params.epsilon);
        }
        final TestInterface proxy =
                builder.build(
                        TestInterface.class,
                        origImpl,
                        newImpl);

        // Simulate:

        final ArrayList<Sample> samples = Lists.newArrayListWithCapacity(params.steps);
        for (int i = 0; i < params.phaseShift; i++) {
            try {
                samples.add(new Sample(simulationClock.instant(), proxy.execute(), false, false));
            } catch (TestException e) {
                samples.add(new Sample(simulationClock.instant(), e.newImpl, false, true));
            }

            simulationClock.setClock(
                    Clock.fixed(
                            start.plus(Duration.ofNanos(i * params.durationPerStep.toNanos())),
                            ZoneOffset.UTC));
        }

        // Change the parameters:
        origImpl.setErrorRatio(params.phase2.origImpl.errorRatio);
        newImpl.setErrorRatio(params.phase2.newImpl.errorRatio);

        // Continue simulating:

        for (int i = params.phaseShift; i < params.steps; i++) {
            try {
                samples.add(new Sample(simulationClock.instant(), proxy.execute(), true, false));
            } catch (TestException e) {
                samples.add(new Sample(simulationClock.instant(), e.newImpl, true, true));
            }

            simulationClock.setClock(
                    Clock.fixed(
                            start.plus(Duration.ofNanos(i * params.durationPerStep.toNanos())),
                            ZoneOffset.UTC));
        }

        return samples;
    }
}
