package com.sangreyoro.matching.domain;

import java.util.Optional;

/**
 * Optional filters chosen at UC2-S2 (FR-2.3): language, student type, department and availability.
 * When no student type is given, the opposite type of the searching student is used.
 */
public final class MatchFilter {
    private final String language;
    private final StudentType studentType;
    private final String department;
    private final String availability;

    private MatchFilter(String language, StudentType studentType, String department, String availability) {
        this.language = blankToNull(language);
        this.studentType = studentType;
        this.department = blankToNull(department);
        this.availability = blankToNull(availability);
    }

    public static MatchFilter none() {
        return new MatchFilter(null, null, null, null);
    }

    public static MatchFilter of(String language, StudentType type, String department, String availability) {
        return new MatchFilter(language, type, department, availability);
    }

    /** Validation rule DV-05: filter values are trimmed and compared case-insensitively. */
    public boolean accepts(Student searcher, Student candidate) {
        StudentType wanted = studentType != null ? studentType : searcher.type().opposite();
        if (candidate.type() != wanted) return false;
        if (language != null) {
            String l = language.toLowerCase();
            if (!candidate.nativeLanguages().contains(l) && !candidate.learningLanguages().containsKey(l)) return false;
        }
        if (department != null && !candidate.department().equalsIgnoreCase(department)) return false;
        return availability == null || candidate.availability().contains(availability.toLowerCase());
    }

    public Optional<StudentType> studentType() {
        return Optional.ofNullable(studentType);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
