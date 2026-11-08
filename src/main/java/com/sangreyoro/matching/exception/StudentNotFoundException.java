package com.sangreyoro.matching.exception;

/** Raised when a student id does not exist (data validation rule DV-01). */
public class StudentNotFoundException extends MatchingException {
    private static final long serialVersionUID = 1L;

    public StudentNotFoundException(String studentId) {
        super("DV-01", "Student not found: " + studentId);
    }

    @Override
    public int httpStatus() { return 404; }
}
