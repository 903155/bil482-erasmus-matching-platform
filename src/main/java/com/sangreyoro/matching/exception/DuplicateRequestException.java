package com.sangreyoro.matching.exception;

/** UC2-E3: a pending or confirmed request between the same two students already exists. */
public class DuplicateRequestException extends MatchingException {
    private static final long serialVersionUID = 1L;

    public DuplicateRequestException(String requesterId, String candidateId) {
        super("UC2-E3", "A match request between " + requesterId + " and " + candidateId + " already exists.");
    }

    @Override
    public int httpStatus() { return 409; }
}
