package io.github.jensrantil.tools.canary;

import com.google.common.base.MoreObjects;

public class Summary {
    public static final Summary EMPTY = new Summary(0, 0);

    public final long successes;
    public final long total;

    public Summary(long successes, long total) {
        this.successes = successes;
        this.total = total;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("successes", successes)
                .add("total", total)
                .add("ratio", total > 0 ? calculateSuccessRatio() : "+Inf")
                .toString();
    }

    public Summary merge(Summary other) {
        return new Summary(this.successes + other.successes, this.total + other.total);
    }

    public double calculateSuccessRatio() {
        return 1.0 * successes / total;
    }
}
