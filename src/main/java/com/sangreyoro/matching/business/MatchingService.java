package com.sangreyoro.matching.business;

import com.sangreyoro.matching.business.notification.MatchEvent;
import com.sangreyoro.matching.business.notification.MatchEventPublisher;
import com.sangreyoro.matching.business.notification.MatchEventType;
import com.sangreyoro.matching.business.strategy.MatchingStrategy;
import com.sangreyoro.matching.business.strategy.MatchingStrategyRegistry;
import com.sangreyoro.matching.data.MatchRepository;
import com.sangreyoro.matching.data.UserRepository;
import com.sangreyoro.matching.domain.MatchCandidate;
import com.sangreyoro.matching.domain.MatchCriterion;
import com.sangreyoro.matching.domain.MatchFilter;
import com.sangreyoro.matching.domain.MatchRequest;
import com.sangreyoro.matching.domain.MatchStatus;
import com.sangreyoro.matching.domain.Student;
import com.sangreyoro.matching.exception.DuplicateRequestException;
import com.sangreyoro.matching.exception.IncompleteProfileException;
import com.sangreyoro.matching.exception.InvalidMatchRequestException;
import com.sangreyoro.matching.exception.StudentNotFoundException;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Business Layer service of UC2 "Find a Match" (SRS 3.3, SDD 6).
 * <p>
 * STRATEGY pattern – role: <b>Context</b>. The service selects the strategy for the criterion chosen by the
 * user and delegates all scoring to it (UC2-S3, UC2-S5). The strategy is a local variable of each call, not a
 * mutable field set with setStrategy(): the service is shared by all web requests, so a field would let two
 * users with different criteria overwrite each other's strategy (thread-safety decision, SDD 9).
 * <p>
 * OBSERVER pattern – role: <b>client of the Subject</b>. Every state change of a match request is published
 * to {@link MatchEventPublisher}; the service does not know who receives it (UC2-S8, UC2-S9).
 * <p>
 * Dependencies are injected through the constructor (Dependency Injection), so tests replace the
 * repositories, the clock and the observers without changing this class (SDD "Design for Testability").
 */
public class MatchingService {
    private static final Logger LOG = Logger.getLogger(MatchingService.class.getName());

    /** Maximum number of candidates returned by one search (NFR-P.2, UI readability). */
    public static final int MAX_RESULTS = 10;

    private final UserRepository users;
    private final MatchRepository matches;
    private final MatchingStrategyRegistry strategies;
    private final MatchEventPublisher events;
    private final Clock clock;

    public MatchingService(UserRepository users, MatchRepository matches, MatchingStrategyRegistry strategies,
                           MatchEventPublisher events, Clock clock) {
        this.users = users;
        this.matches = matches;
        this.strategies = strategies;
        this.events = events;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------------ UC2-S3 .. UC2-S6

    /**
     * UC2-S3 to UC2-S6: returns the most compatible candidates for the student, ordered by score.
     * An empty list means UC2-E2 (no candidates).
     *
     * @throws IncompleteProfileException UC2-E1
     */
    public List<MatchCandidate> findMatches(String studentId, MatchCriterion criterion, MatchFilter filter) {
        long start = System.nanoTime();
        Student me = requireStudent(studentId);
        if (!me.isProfileComplete()) throw new IncompleteProfileException(studentId);

        MatchingStrategy strategy = strategies.get(criterion);          // UC2-S3: choose the strategy
        List<MatchRequest> myRequests = matches.findByStudent(me.id());

        List<MatchCandidate> result = new ArrayList<>();
        for (Student other : users.findAll()) {                          // UC2-S4: candidate profiles
            if (!isEligible(me, other, filter, criterion, myRequests)) continue;
            MatchCandidate c = strategy.score(me, other);                // UC2-S5: delegate to the strategy
            if (c.score() > 0) result.add(c);
        }
        result.sort(Comparator.comparingInt(MatchCandidate::score).reversed()
                .thenComparing(c -> c.student().name()));               // UC2-S6: rank by score
        List<MatchCandidate> top = result.size() > MAX_RESULTS ? List.copyOf(result.subList(0, MAX_RESULTS)) : result;

        long ms = (System.nanoTime() - start) / 1_000_000;
        LOG.fine(() -> "UC2 findMatches student=" + studentId + " criterion=" + criterion + " candidates="
                + top.size() + " time=" + ms + "ms");
        return top;
    }

    /**
     * Exclusion rules of UC2-S4 and FR-2.5: not myself, matching enabled, complete profile, nobody blocked,
     * the filters of UC2-S2, not already matched, and not rejected before for this criterion (UC2-A3).
     */
    private boolean isEligible(Student me, Student other, MatchFilter filter, MatchCriterion criterion,
                               List<MatchRequest> myRequests) {
        if (other.id().equals(me.id())) return false;
        if (!other.matchingEnabled() || !other.isProfileComplete()) return false;
        if (me.hasBlocked(other) || other.hasBlocked(me)) return false;
        if (!filter.accepts(me, other)) return false;
        for (MatchRequest r : myRequests) {
            if (!r.isBetween(me.id(), other.id())) continue;
            if (r.status() == MatchStatus.CONFIRMED) return false;
            if (r.status() == MatchStatus.REJECTED && r.criterion() == criterion) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------------------ UC2-S7 / UC2-S8

    /**
     * UC2-S7 and UC2-S8: stores a PENDING request and notifies the candidate.
     *
     * @throws DuplicateRequestException UC2-E3
     */
    public MatchRequest sendRequest(String requesterId, String candidateId, MatchCriterion criterion) {
        Student requester = requireStudent(requesterId);
        Student candidate = requireStudent(candidateId);
        if (requester.id().equals(candidate.id()))
            throw new InvalidMatchRequestException("DV-02", "A student cannot send a match request to themselves.");
        if (!requester.isProfileComplete()) throw new IncompleteProfileException(requesterId);
        if (requester.hasBlocked(candidate) || candidate.hasBlocked(requester) || !candidate.matchingEnabled())
            throw new InvalidMatchRequestException("DV-03", "This student is not available for matching.");
        for (MatchRequest r : matches.findByStudent(requesterId)) {
            if (r.isBetween(requesterId, candidateId) && r.status() != MatchStatus.REJECTED)
                throw new DuplicateRequestException(requesterId, candidateId);
        }
        int score = strategies.get(criterion).score(requester, candidate).score();
        MatchRequest request = new MatchRequest(matches.nextId(), requesterId, candidateId, criterion, score, clock.instant());
        matches.save(request);
        events.publish(new MatchEvent(MatchEventType.MATCH_REQUESTED, request, requester, candidate));
        return request;
    }

    // ------------------------------------------------------------------------------ UC2-S9 / UC2-A3

    /**
     * UC2-S9 (accept) or UC2-A3 (reject). Only the candidate of a pending request may answer it (DV-04).
     */
    public MatchRequest respond(String requestId, String responderId, boolean accept) {
        MatchRequest request = matches.findById(requestId)
                .orElseThrow(() -> new InvalidMatchRequestException("DV-04", "Unknown match request " + requestId));
        if (!request.candidateId().equals(responderId))
            throw new InvalidMatchRequestException("DV-04", "Only the invited student can answer this request.");
        if (request.status() != MatchStatus.PENDING)
            throw new InvalidMatchRequestException("DV-04", "This request was already answered.");
        if (accept) request.confirm(clock.instant());
        else request.reject(clock.instant());
        matches.save(request);
        events.publish(new MatchEvent(accept ? MatchEventType.MATCH_CONFIRMED : MatchEventType.MATCH_REJECTED,
                request, requireStudent(request.requesterId()), requireStudent(request.candidateId())));
        return request;
    }

    public List<MatchRequest> requestsOf(String studentId) {
        requireStudent(studentId);
        return matches.findByStudent(studentId);
    }

    public Student requireStudent(String id) {
        return users.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    }
}
