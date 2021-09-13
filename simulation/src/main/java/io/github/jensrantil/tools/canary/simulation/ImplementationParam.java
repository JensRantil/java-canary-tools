package io.github.jensrantil.tools.canary.simulation;

import com.google.common.base.Preconditions;

public class ImplementationParam {
    public static final String ERROR_RATIO_VALIDATION_ERROR =
            "Error ratio must be betewen [0,1]. Was:";
    public double errorRatio = 0;

    public ImplementationParam(double errorRatio) {
        this.errorRatio = errorRatio;
    }

    public void parse(String key, String value) {
        switch (key) {
            case "error-ratio":
                this.errorRatio = Double.parseDouble(value);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unrecognized flag: %s", key));
        }
    }

    public void validate() {
        Preconditions.checkArgument(
                this.errorRatio >= 0, ERROR_RATIO_VALIDATION_ERROR, this.errorRatio);
        Preconditions.checkArgument(
                this.errorRatio <= 1, ERROR_RATIO_VALIDATION_ERROR, this.errorRatio);
    }
}
