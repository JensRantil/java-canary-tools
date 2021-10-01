package io.github.jensrantil.tools.canary.simulation;

public class PhaseParams {
    public static final String ORIG_IMPL = "orig-impl-";
    public static final String NEW_IMPL = "new-impl-";

    public ImplementationParam origImpl;
    public ImplementationParam newImpl;

    public PhaseParams(ImplementationParam origImpl, ImplementationParam newImpl) {
        this.origImpl = origImpl;
        this.newImpl = newImpl;
    }

    public void parse(String key, String value) {
        if (key.startsWith(ORIG_IMPL)) {
            origImpl.parse(value.substring(ORIG_IMPL.length()), value);
            return;
        }
        if (key.startsWith(NEW_IMPL)) {
            newImpl.parse(value.substring(NEW_IMPL.length()), value);
            return;
        }
        throw new IllegalArgumentException(String.format("Unrecognized flag: %s", key));
    }

    public void validate() {
        try {
            this.origImpl.validate();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("original implementation failed validation.", e);
        }
        try {
            this.newImpl.validate();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("new implementation failed validation.", e);
        }
    }
}
