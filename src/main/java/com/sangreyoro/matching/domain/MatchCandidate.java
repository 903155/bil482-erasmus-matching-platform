package com.sangreyoro.matching.domain;

import java.util.List;

/**
 * One row of the ranked result list (UC2-S6): the candidate, a compatibility score between 0 and 100
 * and the reasons that explain the score (FR-2.2).
 */
public record MatchCandidate(Student student, int score, List<String> reasons) {
    public MatchCandidate {
        if (score < 0 || score > 100) throw new IllegalArgumentException("Score must be between 0 and 100: " + score);
        reasons = List.copyOf(reasons);
    }
}
