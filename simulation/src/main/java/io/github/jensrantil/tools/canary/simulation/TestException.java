package io.github.jensrantil.tools.canary.simulation;

class TestException extends RuntimeException {
    public final boolean newImpl;

    public TestException(boolean newImpl) {
        super("test exception");
        this.newImpl = newImpl;
    }
}
