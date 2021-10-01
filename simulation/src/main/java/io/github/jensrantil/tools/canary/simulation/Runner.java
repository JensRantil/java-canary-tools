package io.github.jensrantil.tools.canary.simulation;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Runner {

    public static final Joiner SPACE_JOINER = Joiner.on(" ");

    public static void main(String[] args) {
        if (args.length==1 && Objects.equals(args[0], "--help")) {
            printHelp();
            return;
        }

        final SimulationParameters params = new SimulationParameters();
        try {
            params.parse(args);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println();
            printHelp();
            return;
        }

        final Instant start = Instant.now();
        List<Sample> samples = new Simulator().simulate(start, params);

        // Print downsamples:

        final Map<Duration, DownSample> downsamples = downsample(params, start, samples);
        printDownsamples(downsamples);
    }

    private static void printDownsamples(Map<Duration, DownSample> downsamples) {
        System.out.println(
                SPACE_JOINER.join(
                        "ms",
                        "oldImplCalls",
                        "newImplCalls",
                        "oldImplExceptions",
                        "newImplExceptions",
                        "nPhase1Samples",
                        "nPhase2Samples"));
        for (Map.Entry<Duration, DownSample> s : downsamples.entrySet()) {
            DownSample value = s.getValue();
            System.out.printf(
                    String.format(
                            "%s\n", SPACE_JOINER.join("%s", "%d", "%d", "%d", "%d", "%d", "%d")),
                    s.getKey().toMillis(),
                    value.oldImpl,
                    value.newImpl,
                    value.oldImplExceptions,
                    value.newImplExceptions,
                    value.phase1,
                    value.phase2);
        }
    }

    private static Map<Duration, DownSample> downsample(
            SimulationParameters params, Instant start, List<Sample> samples) {

        // FIXME: Empty timeslots will not be added to the map here. Also populate
        // those.
        final Map<Duration, DownSample> downsamples = Maps.newTreeMap();

        for (Sample sample : samples) {
            final long nanosSinceStart = Duration.between(start, sample.timestamp).toNanos();
            final long downSampledNanosSinceStart =
                    nanosSinceStart - (nanosSinceStart % params.outputResolution.toNanos());
            final Duration timestamp = Duration.ofNanos(downSampledNanosSinceStart);

            if (!downsamples.containsKey(timestamp)) {
                downsamples.put(timestamp, new DownSample());
            }

            DownSample value = downsamples.get(timestamp);
            if (sample.isPhase2) {
                value.phase2++;
            } else {
                value.phase1++;
            }
            if (sample.newImpl) {
                value.newImpl++;
                if (sample.exceptionThrown) {
                    value.newImplExceptions++;
                }
            } else {
                value.oldImpl++;
                if (sample.exceptionThrown) {
                    value.oldImplExceptions++;
                }
            }
        }
        return downsamples;
    }

    private static void printHelp() {
        System.out.println("Flags:");
        System.out.println();
        System.out.println("--phase-shift");
        System.out.println("--steps");
        System.out.println("--seed");
        System.out.println("--failure-penalty");
        System.out.println("--success-credits");
        System.out.println("--epsilon");
        System.out.println("--slots");
        System.out.println("--slot-duration");
        System.out.println("--duration-per-step");
        System.out.println("--phase1-orig-impl-error-ratio");
        System.out.println("--phase1-new-impl-error-ratio");
        System.out.println("--phase2-orig-impl-error-ratio");
        System.out.println("--phase2-new-impl-error-ratio");
    }
}
