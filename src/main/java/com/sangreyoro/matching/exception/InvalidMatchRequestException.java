package com.sangreyoro.matching.exception;

/** Raised when a request breaks a business rule: self-request, blocked user, wrong responder, unknown id (DV-02 to DV-04). */
public class InvalidMatchRequestException extends MatchingException {
    private static final long serialVersionUID = 1L;

    public InvalidMatchRequestException(String ruleId, String message) {
        super(ruleId, message);
    }

    @Override
    public int httpStatus() { return 400; }
}
