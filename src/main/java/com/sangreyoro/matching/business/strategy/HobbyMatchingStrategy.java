package com.sangreyoro.matching.business.strategy;

import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * STRATEGY pattern – ConcreteStrategy "Hobbies" (UC2, FR-2.1).
 * <p>
 * Score = Jaccard similarity of the two hobby sets × 100. Two students with the same three hobbies get 100;
 * two students with one hobby in common out of four different ones get 25.
 */
public class HobbyMatchingStrategy implements MatchingStrategy {

    @Override
    public MatchCriterion criterion() {
        return MatchCriterion.HOBBIES;
    }

    @Override
    public MatchCandidate score(Student me, Student other) {
        Set<String> shared = SetSimilarity.intersection(me.hobbies(), other.hobbies());
        int score = (int) Math.round(SetSimilarity.jaccard(me.hobbies(), other.hobbies()) * 100);
        List<String> reasons = new ArrayList<>();
        if (!shared.isEmpty()) reasons.add("Shared hobbies: " + String.join(", ", shared));
        return new MatchCandidate(other, score, reasons);
    }
}
