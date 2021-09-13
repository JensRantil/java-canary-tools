package io.github.jensrantil.tools.canary.simulation;

import java.util.Random;

class TestImplementation implements TestInterface {
    private final Random random;
    private final boolean newImpl;

    private double errorRatio;

    public TestImplementation(Random random, boolean newImpl, double errorRatio) {
        this.random = random;
        this.newImpl = newImpl;
        this.errorRatio = errorRatio;
    }

    @Override
    public boolean execute() {
        if (random.nextDouble() < errorRatio) {
            throw new TestException(newImpl);
        }
        return newImpl;
    }

    public void setErrorRatio(double errorRatio) {
        this.errorRatio = errorRatio;
    }
}
