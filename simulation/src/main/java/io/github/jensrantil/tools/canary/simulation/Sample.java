package io.github.jensrantil.tools.canary.simulation;

import java.time.Instant;

public class Sample {
    public final Instant timestamp;
    public final boolean newImpl;
    public final boolean isPhase2;
    public final boolean exceptionThrown;

    public Sample(Instant timestamp, boolean newImpl, boolean isPhase2, boolean exceptionThrown) {
        this.timestamp = timestamp;
        this.newImpl = newImpl;
        this.isPhase2 = isPhase2;
        this.exceptionThrown = exceptionThrown;
    }
}
