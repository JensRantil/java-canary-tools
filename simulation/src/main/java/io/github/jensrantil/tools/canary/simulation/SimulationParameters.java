package io.github.jensrantil.tools.canary.simulation;

import com.google.common.base.Preconditions;
import java.time.Duration;

public class SimulationParameters {
    public static final String PHASE_1_PREFIX = "--phase1-";
    public static final String PHASE_2_PREFIX = "--phase2-";
    public static final String EPSILON_VALIDATION_ERROR = "Epsilon must be between [0,1].";

    public int phaseShift = 500;
    public int steps = 1000;
    public PhaseParams phase1 =
            new PhaseParams(new ImplementationParam(0), new ImplementationParam(0));
    public PhaseParams phase2 =
            new PhaseParams(new ImplementationParam(0), new ImplementationParam(1));

    // https://en.wikipedia.org/wiki/42_(number)#The_Hitchhiker's_Guide_to_the_Galaxy
    public long seed = 42;

    public Double epsilon = null;

    public int slots = 6;
    public Duration slotDuration = Duration.ofSeconds(30);
    public Duration durationPerStep = Duration.ofSeconds(1);
    public Duration outputResolution = Duration.ofMinutes(1);

    /**
     * Parse CLI parameters. Poor man's parsing since I didn't want to spend time on learning a new
     * flag parsing library during hackathon.
     *
     * @param args the CLI flags given to the application.
     */
    public void parse(String[] args) {
        if (args.length % 2 == 1) {
            throw new IllegalArgumentException("arguments should come in pairs.");
        }

        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            String value = args[i + 1];

            if (key.startsWith(PHASE_1_PREFIX)) {
                this.phase1.parse(key.substring(PHASE_1_PREFIX.length()), value);
                continue;
            }
            if (key.startsWith(PHASE_2_PREFIX)) {
                this.phase2.parse(key.substring(PHASE_2_PREFIX.length()), value);
                continue;
            }

            switch (key) {
                case "--phase-shift":
                    this.phaseShift = Integer.parseInt(value);
                    break;
                case "--steps":
                    this.steps = Integer.parseInt(value);
                    break;
                case "--seed":
                    this.seed = Long.parseLong(value);
                    break;
                case "--epsilon":
                    this.epsilon = Double.parseDouble(value);
                    break;
                case "--slots":
                    this.slots = Integer.parseInt(value);
                    break;
                case "--slot-duration":
                    this.slotDuration = Duration.parse(value);
                    break;
                case "--duration-per-step":
                    this.durationPerStep = Duration.parse(value);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unrecognized flag: %s", key));
            }
        }

        validate();
    }

    private void validate() {
        Preconditions.checkArgument(this.slots > 0, "Number of slots must be positive.");
        Preconditions.checkArgument(this.steps > 0, "Number of steps must be positive.");

        Preconditions.checkArgument(
                this.epsilon == null || this.epsilon >= 0, EPSILON_VALIDATION_ERROR);
        Preconditions.checkArgument(
                this.epsilon == null || this.epsilon <= 0, EPSILON_VALIDATION_ERROR);

        try {
            this.phase1.validate();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("phase1 failed validation.", e);
        }
        try {
            this.phase2.validate();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("phase2 failed validation.", e);
        }
    }
}
