package com.sangreyoro.matching.business.strategy;

import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.Student;

/**
 * ============================================================================================
 * DESIGN PATTERN: STRATEGY (behavioral) – role: Strategy interface            [UC2-S3, UC2-S5]
 * --------------------------------------------------------------------------------------------
 * Intent:      define a family of matching algorithms, encapsulate each one in its own class and
 *              make them interchangeable at runtime.
 * Motivation:  UC2 lets the student choose HOW to be matched (language exchange, hobbies,
 *              sports – FR-2.1). Every criterion computes the compatibility score differently,
 *              and new criteria are expected (e.g. "same degree", "music taste").
 * Improvement: MatchingService (the Context) only calls {@link #score}. It never contains
 *              criterion-specific code, so a criterion can be added, changed or tested alone.
 * Metrics:     - adding a criterion = 1 new class + 1 enum constant + 1 registration line;
 *                lines changed in MatchingService = 0 (verified by test UC2-T09, which plugs in a
 *                new strategy without touching the service);
 *              - cyclomatic complexity of MatchingService.findMatches stays constant (≈ 6) instead
 *                of growing by one branch per criterion with an if/else chain;
 *              - each strategy is unit-tested in isolation (UC2-T01..T03) with no mocks needed.
 * ============================================================================================
 */
public interface MatchingStrategy {

    /** The criterion this strategy implements; used by the registry to select it. */
    MatchCriterion criterion();

    /**
     * Calculates the compatibility of {@code candidate} for {@code searcher}.
     *
     * @return a candidate with a score from 0 (not compatible) to 100 and the reasons for the score
     */
    MatchCandidate score(Student searcher, Student candidate);
}
