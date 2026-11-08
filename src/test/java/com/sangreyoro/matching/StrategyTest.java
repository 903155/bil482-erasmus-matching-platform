package com.sangreyoro.matching;

import com.sangreyoro.matching.business.strategy.HobbyMatchingStrategy;
import com.sangreyoro.matching.business.strategy.LanguageExchangeStrategy;
import com.sangreyoro.matching.business.strategy.SportsMatchingStrategy;
import com.sangreyoro.matching.domain.LanguageLevel;
import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.Student;
import com.sangreyoro.matching.domain.StudentType;
import com.sangreyoro.matching.testkit.Test;

import static com.sangreyoro.matching.testkit.Assert.assertEquals;
import static com.sangreyoro.matching.testkit.Assert.assertTrue;

/** Unit tests of the three ConcreteStrategies, each in isolation (Strategy pattern). */
public class StrategyTest {
    private final Student maria = Student.builder("E01", "María", StudentType.ERASMUS).speaks("spanish")
            .learns("turkish", LanguageLevel.A2).learns("english", LanguageLevel.C1)
            .hobbies("photography", "cooking", "hiking").sports("football", "running")
            .availableOn("weekday-evenings", "weekends").build();
    private final Student emre = Student.builder("L01", "Emre", StudentType.LOCAL).speaks("turkish")
            .learns("spanish", LanguageLevel.B1).learns("english", LanguageLevel.B2)
            .hobbies("photography", "hiking", "programming").sports("football", "running")
            .availableOn("weekday-evenings", "weekends").build();
    private final Student zeynep = Student.builder("L02", "Zeynep", StudentType.LOCAL).speaks("turkish")
            .learns("italian", LanguageLevel.A2).learns("english", LanguageLevel.C1)
            .hobbies("drawing", "museums").sports("yoga").availableOn("weekday-evenings").build();

    @Test(id = "UC2-T01", covers = "UC2-S5, FR-2.1, FR-2.2 (LanguageExchangeStrategy)")
    void languageExchangeRewardsComplementaryLanguages() {
        MatchCandidate withEmre = new LanguageExchangeStrategy().score(maria, emre);
        MatchCandidate withZeynep = new LanguageExchangeStrategy().score(maria, zeynep);
        assertEquals(100, withEmre.score(), "Turkish↔Spanish in both directions (50+50, +10 capped at 100)");
        assertEquals(60, withZeynep.score(), "Zeynep teaches Turkish (+50) and both practise English (+10)");
        assertTrue(withEmre.reasons().get(0).contains("Turkish"), "reason explains the score: " + withEmre.reasons());
    }

    @Test(id = "UC2-T02", covers = "UC2-S5, FR-2.2 (HobbyMatchingStrategy)")
    void hobbyScoreIsJaccardSimilarity() {
        // {photography, cooking, hiking} vs {photography, hiking, programming}: 2 shared / 4 total = 0.5
        assertEquals(50, new HobbyMatchingStrategy().score(maria, emre).score(), "Jaccard 2/4");
        assertEquals(0, new HobbyMatchingStrategy().score(maria, zeynep).score(), "no shared hobby");
    }

    @Test(id = "UC2-T03", covers = "UC2-S5, FR-2.2 (SportsMatchingStrategy)")
    void sportsNeedASharedSportAndCommonTime() {
        assertEquals(100, new SportsMatchingStrategy().score(maria, emre).score(), "same sports and same slots");
        assertEquals(0, new SportsMatchingStrategy().score(maria, zeynep).score(),
                "common free time alone does not create a sports match");
    }
}
