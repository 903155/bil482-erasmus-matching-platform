package com.sangreyoro.matching.exception;

/** UC2-E1: the searching student's profile is incomplete, so matching is not allowed (FR-1.4). */
public class IncompleteProfileException extends MatchingException {
    private static final long serialVersionUID = 1L;

    public IncompleteProfileException(String studentId) {
        super("UC2-E1", "Profile of student " + studentId + " is incomplete: add at least one hobby or sport and one language to practise.");
    }

    @Override
    public int httpStatus() { return 409; }
}
