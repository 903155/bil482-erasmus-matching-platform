package com.sangreyoro.matching;

import com.sangreyoro.matching.business.strategy.MatchingStrategy;
import com.sangreyoro.matching.data.CsvUserRepository;
import com.sangreyoro.matching.domain.LanguageLevel;
import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.MatchFilter;
import com.sangreyoro.matching.domain.MatchRequest;
import com.sangreyoro.matching.domain.MatchStatus;
import com.sangreyoro.matching.domain.Student;
import com.sangreyoro.matching.domain.StudentType;
import com.sangreyoro.matching.exception.DuplicateRequestException;
import com.sangreyoro.matching.exception.IncompleteProfileException;
import com.sangreyoro.matching.exception.InvalidMatchRequestException;
import com.sangreyoro.matching.testkit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.sangreyoro.matching.testkit.Assert.assertEquals;
import static com.sangreyoro.matching.testkit.Assert.assertFalse;
import static com.sangreyoro.matching.testkit.Assert.assertThrows;
import static com.sangreyoro.matching.testkit.Assert.assertTrue;

/** Tests of MatchingService (Strategy Context) against the demo data in data/students.csv. */
public class MatchingServiceTest {

    private static List<String> ids(List<MatchCandidate> list) {
        List<String> out = new ArrayList<>();
        for (MatchCandidate c : list) out.add(c.student().id());
        return out;
    }

    @Test(id = "UC2-T04", covers = "AT-UC2-01, UC2-S1..S6, FR-2.1, FR-2.2, FR-2.5")
    void matchPerCriterionRanksAndExcludesCorrectly() {
        Fixture f = new Fixture();
        for (MatchCriterion c : MatchCriterion.values()) {
            List<MatchCandidate> r = f.service.findMatches("E01", c, MatchFilter.none());
            assertFalse(r.isEmpty(), c + " returns candidates");
            for (int i = 1; i < r.size(); i++) assertTrue(r.get(i - 1).score() >= r.get(i).score(), c + " ordered by score");
            for (MatchCandidate m : r) {
                assertEquals(StudentType.LOCAL, m.student().type(), "default filter = opposite type (UC2-S2)");
                assertTrue(m.score() > 0 && m.score() <= 100, "score in 1..100");
            }
            List<String> ids = ids(r);
            assertFalse(ids.contains("L07"), "Burak blocked María – never shown (FR-2.5)");
            assertFalse(ids.contains("L06"), "Ayşe disabled matching – never shown (FR-2.5)");
        }
        List<MatchCandidate> le = f.service.findMatches("E01", MatchCriterion.LANGUAGE_EXCHANGE, MatchFilter.none());
        assertEquals(100, le.get(0).score(), "best language partner has the maximum score");
        boolean emreTop = false;
        for (MatchCandidate c : le) if (c.student().id().equals("L01")) emreTop = c.score() == 100;
        assertTrue(emreTop, "Emre (Turkish, learns Spanish) is one of María's best language partners");
    }

    @Test(id = "UC2-T05", covers = "UC2-E1, FR-1.4")
    void incompleteProfileCannotSearch() {
        Fixture f = new Fixture();
        IncompleteProfileException e = assertThrows(IncompleteProfileException.class,
                () -> f.service.findMatches("E07", MatchCriterion.HOBBIES, MatchFilter.none()), "Sofía has no hobbies");
        assertEquals("UC2-E1", e.flowId(), "exception carries the SRS flow id");
        assertFalse(ids(f.service.findMatches("L01", MatchCriterion.HOBBIES, MatchFilter.none())).contains("E07"),
                "incomplete profiles are never candidates");
    }

    @Test(id = "UC2-T06", covers = "AT-UC2-03, UC2-E2")
    void noCandidatesReturnsEmptyList() {
        Fixture f = new Fixture();
        List<MatchCandidate> r = f.service.findMatches("E01", MatchCriterion.HOBBIES, MatchFilter.of(null, null, "Medicine", null));
        assertTrue(r.isEmpty(), "no student studies Medicine");
    }

    @Test(id = "UC2-T07", covers = "AT-UC2-02, UC2-S7..S9, FR-2.4, FR-3.4")
    void acceptedRequestBecomesConfirmedAndNotifiesBoth() {
        Fixture f = new Fixture();
        MatchRequest r = f.service.sendRequest("E01", "L01", MatchCriterion.LANGUAGE_EXCHANGE);
        assertEquals(MatchStatus.PENDING, r.status(), "UC2-S8 pending");
        assertEquals(1, f.notifications.findByStudent("L01").size(), "candidate notified of the request");
        f.service.respond(r.id(), "L01", true);
        assertEquals(MatchStatus.CONFIRMED, f.matches.findById(r.id()).get().status(), "UC2-S9 confirmed");
        assertEquals(1, f.notifications.findByStudent("E01").size(), "requester notified of the match");
        assertEquals(2, f.notifications.findByStudent("L01").size(), "candidate notified of the match");
        assertFalse(ids(f.service.findMatches("E01", MatchCriterion.LANGUAGE_EXCHANGE, MatchFilter.none())).contains("L01"),
                "a confirmed partner is not suggested again");
    }

    @Test(id = "UC2-T08", covers = "UC2-E3, DV-02, DV-03, DV-04")
    void invalidRequestsAreRejected() {
        Fixture f = new Fixture();
        MatchRequest r = f.service.sendRequest("E01", "L01", MatchCriterion.HOBBIES);
        assertThrows(DuplicateRequestException.class, () -> f.service.sendRequest("E01", "L01", MatchCriterion.SPORTS), "UC2-E3 duplicate");
        assertThrows(DuplicateRequestException.class, () -> f.service.sendRequest("L01", "E01", MatchCriterion.SPORTS), "UC2-E3 reverse duplicate");
        assertThrows(InvalidMatchRequestException.class, () -> f.service.sendRequest("E01", "E01", MatchCriterion.SPORTS), "DV-02 self request");
        assertThrows(InvalidMatchRequestException.class, () -> f.service.sendRequest("E01", "L07", MatchCriterion.SPORTS), "DV-03 blocked");
        assertThrows(InvalidMatchRequestException.class, () -> f.service.respond(r.id(), "E01", true), "DV-04 only the candidate answers");
        f.service.respond(r.id(), "L01", false);
        assertThrows(InvalidMatchRequestException.class, () -> f.service.respond(r.id(), "L01", true), "DV-04 answered once");
    }

    @Test(id = "UC2-T09", covers = "Strategy pattern – Open/Closed metric (0 lines changed in MatchingService)")
    void newStrategyIsUsedWithoutChangingTheService() {
        Fixture f = new Fixture();
        MatchingStrategy sameDepartment = new MatchingStrategy() {          // a criterion written only in this test
            public MatchCriterion criterion() { return MatchCriterion.HOBBIES; }
            public MatchCandidate score(Student me, Student other) {
                return new MatchCandidate(other, me.department().equals(other.department()) ? 100 : 0, List.of("Same department"));
            }
        };
        f.strategies.register(sameDepartment);
        List<MatchCandidate> r = f.service.findMatches("E01", MatchCriterion.HOBBIES, MatchFilter.none());
        for (MatchCandidate c : r) assertEquals("Computer Engineering", c.student().department(), "service used the new strategy");
        assertTrue(r.size() >= 2, "Emre and Can study Computer Engineering");
    }

    @Test(id = "UC2-T10", covers = "UC2-A3, FR-2.4")
    void rejectedCandidateIsHiddenOnlyForThatCriterion() {
        Fixture f = new Fixture();
        MatchRequest r = f.service.sendRequest("E01", "L01", MatchCriterion.HOBBIES);
        f.service.respond(r.id(), "L01", false);
        assertFalse(ids(f.service.findMatches("E01", MatchCriterion.HOBBIES, MatchFilter.none())).contains("L01"), "hidden for HOBBIES");
        assertTrue(ids(f.service.findMatches("E01", MatchCriterion.SPORTS, MatchFilter.none())).contains("L01"), "still shown for SPORTS");
        assertEquals(0, f.notifications.findByStudent("E01").size(), "privacy: requester is not told about the rejection");
    }

    @Test(id = "UC2-T14", covers = "UC2-S2, FR-2.3 (filters)")
    void filtersRestrictTheCandidates() {
        Fixture f = new Fixture();
        for (MatchCandidate c : f.service.findMatches("E01", MatchCriterion.HOBBIES, MatchFilter.of(null, null, "Computer Engineering", null)))
            assertEquals("Computer Engineering", c.student().department(), "department filter");
        for (MatchCandidate c : f.service.findMatches("E01", MatchCriterion.LANGUAGE_EXCHANGE, MatchFilter.of(null, StudentType.ERASMUS, null, null)))
            assertEquals(StudentType.ERASMUS, c.student().type(), "explicit type filter");
        for (MatchCandidate c : f.service.findMatches("E01", MatchCriterion.SPORTS, MatchFilter.of(null, null, null, "mornings")))
            assertTrue(c.student().availability().contains("mornings"), "availability filter");
        for (MatchCandidate c : f.service.findMatches("L01", MatchCriterion.LANGUAGE_EXCHANGE, MatchFilter.of("Italian", null, null, null)))
            assertTrue(c.student().nativeLanguages().contains("italian") || c.student().learningLanguages().containsKey("italian"), "language filter");
    }

    @Test(id = "UC2-T16", covers = "NFR-P.2 (results in < 3 s with 1,000 users)")
    void findMatchesIsFastWithAThousandStudents() {
        Random rnd = new Random(42);
        String[] langs = {"turkish", "spanish", "english", "german", "italian", "french", "polish"};
        String[] hobbies = {"cinema", "music", "cooking", "hiking", "photography", "board games", "travel", "drawing", "programming"};
        String[] sports = {"football", "running", "basketball", "tennis", "yoga", "swimming", "volleyball"};
        String[] slots = {"mornings", "weekday-evenings", "weekends"};
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Student.Builder b = Student.builder("S" + i, "Student " + i, i % 2 == 0 ? StudentType.ERASMUS : StudentType.LOCAL)
                    .department("Dept " + rnd.nextInt(8)).speaks(langs[rnd.nextInt(langs.length)])
                    .learns(langs[rnd.nextInt(langs.length)], LanguageLevel.values()[rnd.nextInt(6)])
                    .availableOn(slots[rnd.nextInt(3)]);
            for (int k = 0; k < 3; k++) b.hobbies(hobbies[rnd.nextInt(hobbies.length)]).sports(sports[rnd.nextInt(sports.length)]);
            list.add(b.build());
        }
        Fixture f = new Fixture(CsvUserRepository.of(list));
        f.service.findMatches("S0", MatchCriterion.HOBBIES, MatchFilter.none());   // warm-up (JIT)
        long start = System.nanoTime();
        int searches = 0;
        for (MatchCriterion c : MatchCriterion.values()) {
            for (int s = 0; s < 20; s++, searches++) f.service.findMatches("S" + s, c, MatchFilter.none());
        }
        double avgMs = (System.nanoTime() - start) / 1e6 / searches;
        System.out.printf("        [metric] average findMatches time with 1,000 students: %.2f ms (limit 3,000 ms)%n", avgMs);
        assertTrue(avgMs < 3000, "NFR-P.2 limit");
    }
}
