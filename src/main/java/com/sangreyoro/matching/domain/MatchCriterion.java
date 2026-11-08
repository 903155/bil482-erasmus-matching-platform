package com.sangreyoro.matching.domain;

/**
 * Matching criteria offered to the student (UC2-S1, FR-2.1).
 * Each value is served by one interchangeable {@code MatchingStrategy} (Strategy pattern).
 */
public enum MatchCriterion {
    LANGUAGE_EXCHANGE("Language Exchange"),
    HOBBIES("Hobbies"),
    SPORTS("Sports");

    private final String label;

    MatchCriterion(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static MatchCriterion parse(String s) {
        return valueOf(s.trim().toUpperCase().replace(' ', '_').replace('-', '_'));
    }
}
