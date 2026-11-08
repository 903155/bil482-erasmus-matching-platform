package com.sangreyoro.matching.exception;

/**
 * Base class of all UC2 business errors. Each subclass carries the exception-flow id of the SRS
 * (e.g. UC2-E1) so that logs and HTTP responses can be traced back to the requirements.
 */
public abstract class MatchingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String flowId;

    protected MatchingException(String flowId, String message) {
        super(message);
        this.flowId = flowId;
    }

    public String flowId() {
        return flowId;
    }

    /** HTTP status used by the presentation layer. */
    public abstract int httpStatus();
}
