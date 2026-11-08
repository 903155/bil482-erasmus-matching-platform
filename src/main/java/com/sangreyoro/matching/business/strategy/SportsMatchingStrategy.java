package com.sangreyoro.matching.business.strategy;

import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * STRATEGY pattern – ConcreteStrategy "Sports" (UC2, FR-2.1).
 * <p>
 * A sports partner must share a sport AND be free at the same time:
 * score = 70 × Jaccard(sports) + 30 × Jaccard(availability). Without a shared sport the score is 0,
 * because common free time alone is not a reason to do sport together.
 */
public class SportsMatchingStrategy implements MatchingStrategy {

    @Override
    public MatchCriterion criterion() {
        return MatchCriterion.SPORTS;
    }

    @Override
    public MatchCandidate score(Student me, Student other) {
        Set<String> sharedSports = SetSimilarity.intersection(me.sports(), other.sports());
        if (sharedSports.isEmpty()) return new MatchCandidate(other, 0, List.of());
        Set<String> sharedSlots = SetSimilarity.intersection(me.availability(), other.availability());
        double value = 70 * SetSimilarity.jaccard(me.sports(), other.sports())
                + 30 * SetSimilarity.jaccard(me.availability(), other.availability());
        List<String> reasons = new ArrayList<>();
        reasons.add("Shared sports: " + String.join(", ", sharedSports));
        if (!sharedSlots.isEmpty()) reasons.add("Both available: " + String.join(", ", sharedSlots));
        return new MatchCandidate(other, (int) Math.round(value), reasons);
    }
}
