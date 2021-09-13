package io.github.jensrantil.tools.canary;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class FixedWindowObserver implements Observer {
    private final ImmutableList<RatioSlot> slots;
    private final Duration slotDuration;
    private final Clock clock;

    private final AtomicLong slotIncrementer = new AtomicLong();
    private final AtomicReference<Instant> nextRoll;
    private final Duration totalSlotDuration;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("summary", getSummary())
                .add("slotDuration", slotDuration)
                .add("slotIncrementer", slotIncrementer)
                .add("nextRoll", nextRoll)
                .toString();
    }

    public FixedWindowObserver(Clock clock, int slots, Duration slotDuration) {
        Preconditions.checkArgument(slots > 0, "slots must be strictly positive");
        Preconditions.checkArgument(
                !slotDuration.isNegative(), "negative slotDuration not supported");

        ImmutableList.Builder<RatioSlot> builder = ImmutableList.builder();
        for (int i = 0; i < slots; i++) {
            builder.add(new RatioSlot());
        }
        this.slots = builder.build();

        this.nextRoll = new AtomicReference(clock.instant().plus(slotDuration));
        this.slotDuration = slotDuration;
        this.clock = clock;
        this.totalSlotDuration = Duration.ofMillis(slots * slotDuration.toNanos());
    }

    @Override
    public void registerFailure() {
        moveSlotIfNecessary();
        this.slots.get(getCursor(this.slotIncrementer.get())).registerFailure();
    }

    @Override
    public void registerSuccess() {
        moveSlotIfNecessary();
        this.slots.get(getCursor(this.slotIncrementer.get())).registerSuccess();
    }

    private int getCursor(long slotIncrementer) {
        long longSize = this.slots.size();
        long longCursor = slotIncrementer % longSize;
        return (int) longCursor; // safe - longSize is < MAX_INT since size returns int.
    }

    private void moveSlotIfNecessary() {
        Instant nextRoll = this.nextRoll.get();

        Instant now = this.clock.instant();
        if (now.isAfter(nextRoll.plus(this.totalSlotDuration))) {
            // Check if we should reset all slots. This is an optimization if there's been a really
            // long time since this method was called.

            if (!this.nextRoll.compareAndSet(nextRoll, now.plus(this.slotDuration))) {
                // Make sure only a single thread resets the slots.
                return;
            }
            // Reset all. Optimisation if noone has called this method for a long time.
            for (RatioSlot slot : this.slots) {
                slot.reset();
            }
            return;
        }

        while (true) {
            // Progress one slot at a time.

            if (this.clock.instant().isBefore(nextRoll)) {
                return;
            }

            Instant newNextRoll = nextRoll.plus(this.slotDuration);
            if (this.nextRoll.compareAndSet(nextRoll, newNextRoll)) {
                moveCurrentSlotAndReset();
                return;
            }

            nextRoll = newNextRoll;
        }
    }

    /**
     * Move current cursor to the next slot, modulus length of slots. Since this isn't simply an
     * increment (supported by AtomicLong), we are relying on compare-and-swap here.
     */
    private void moveCurrentSlotAndReset() {
        long newSlot = this.slotIncrementer.incrementAndGet();

        // There is a small race condition here, that there #registerFailure or #registerSuccess are
        // called here. When we call #reset below, those registrations will get dropped. We are fine
        // with dropping some registrations at the
        // benefit of lock-free concurrency.
        this.slots.get(getCursor(newSlot)).reset();
    }

    @Override
    public Summary getSummary() {
        moveSlotIfNecessary();

        Summary aggregate = Summary.EMPTY;
        for (RatioSlot slot : this.slots) {
            aggregate = aggregate.merge(slot.getSummary());
        }
        return aggregate;
    }
}
