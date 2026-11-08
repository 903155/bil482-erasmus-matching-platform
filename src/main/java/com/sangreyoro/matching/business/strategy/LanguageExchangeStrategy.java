package com.sangreyoro.matching.business.strategy;

import com.sangreyoro.matching.domain.LanguageLevel;
import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * STRATEGY pattern – ConcreteStrategy "Language Exchange" (UC2, FR-2.1).
 * <p>
 * Rewards complementary languages: the best partner speaks natively what I am learning and is learning
 * what I speak natively (a Spanish Erasmus student learning Turkish ↔ a Turkish student learning Spanish).
 * <ul>
 *   <li>+50 if the candidate is a native speaker of a language I am learning</li>
 *   <li>+50 if I am a native speaker of a language the candidate is learning</li>
 *   <li>+10 per language we are both learning (practice partners), at most +20</li>
 * </ul>
 * The score is capped at 100.
 */
public class LanguageExchangeStrategy implements MatchingStrategy {

    @Override
    public MatchCriterion criterion() {
        return MatchCriterion.LANGUAGE_EXCHANGE;
    }

    @Override
    public MatchCandidate score(Student me, Student other) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        String teachesMe = firstNativeMatch(other, me.learningLanguages());
        if (teachesMe != null) {
            score += 50;
            reasons.add(other.name() + " speaks " + cap(teachesMe) + ", which you are learning ("
                    + me.learningLanguages().get(teachesMe) + ")");
        }
        String iTeach = firstNativeMatch(me, other.learningLanguages());
        if (iTeach != null) {
            score += 50;
            reasons.add("You speak " + cap(iTeach) + ", which " + other.name() + " is learning ("
                    + other.learningLanguages().get(iTeach) + ")");
        }
        int common = 0;
        for (String lang : me.learningLanguages().keySet()) {
            if (other.learningLanguages().containsKey(lang) && common < 2) {
                common++;
                score += 10;
                reasons.add("You are both practising " + cap(lang));
            }
        }
        return new MatchCandidate(other, Math.min(100, score), reasons);
    }

    /** First language in {@code wanted} that {@code speaker} speaks natively, or null. */
    private static String firstNativeMatch(Student speaker, Map<String, LanguageLevel> wanted) {
        for (String lang : wanted.keySet()) if (speaker.nativeLanguages().contains(lang)) return lang;
        return null;
    }

    static String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
