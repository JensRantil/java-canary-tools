package io.github.jensrantil.tools.canary.simulation;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

class SimulationClock extends Clock {
    private Clock clock;

    public SimulationClock(Clock clock) {
        this.clock = clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return clock;
    }

    @Override
    public ZoneId getZone() {
        return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return clock.withZone(zone);
    }

    @Override
    public Instant instant() {
        return clock.instant();
    }
}
