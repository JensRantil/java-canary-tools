package io.github.jensrantil.tools.canary;

import com.google.common.base.MoreObjects;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class RatioSlot implements Observer {
    private final LongAdder successes = new LongAdder();
    private final LongAdder total = new LongAdder();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("successes", successes)
                .add("total", total)
                .toString();
    }

    @Override
    public void registerFailure() {
        lock.readLock().lock();
        try {
            this.total.increment();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void registerSuccess() {
        lock.readLock().lock();
        try {
            // Yes, this is not an atomic operation. To avoid division by zero we at least increment
            // the total first.
            this.total.increment();
            this.successes.increment();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Summary getSummary() {
        lock.readLock().lock();
        try {
            return new Summary(this.successes.longValue(), this.total.longValue());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void reset() {
        lock.writeLock().lock();
        try {
            this.successes.reset();
            this.total.reset();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
