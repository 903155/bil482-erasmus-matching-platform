package com.sangreyoro.matching.domain;

import java.time.Instant;

/** A match request between two students (UC2-S7 to UC2-S9). Status changes only through accept/reject. */
public final class MatchRequest {
    private final String id;
    private final String requesterId;
    private final String candidateId;
    private final MatchCriterion criterion;
    private final int score;
    private final Instant createdAt;
    private MatchStatus status = MatchStatus.PENDING;
    private Instant answeredAt;

    public MatchRequest(String id, String requesterId, String candidateId, MatchCriterion criterion, int score, Instant createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.candidateId = candidateId;
        this.criterion = criterion;
        this.score = score;
        this.createdAt = createdAt;
    }

    public void confirm(Instant when) {
        requirePending();
        status = MatchStatus.CONFIRMED;
        answeredAt = when;
    }

    public void reject(Instant when) {
        requirePending();
        status = MatchStatus.REJECTED;
        answeredAt = when;
    }

    private void requirePending() {
        if (status != MatchStatus.PENDING) throw new IllegalStateException("Request " + id + " is already " + status);
    }

    public boolean involves(String studentId) {
        return requesterId.equals(studentId) || candidateId.equals(studentId);
    }

    public boolean isBetween(String a, String b) {
        return (requesterId.equals(a) && candidateId.equals(b)) || (requesterId.equals(b) && candidateId.equals(a));
    }

    public String id() { return id; }
    public String requesterId() { return requesterId; }
    public String candidateId() { return candidateId; }
    public MatchCriterion criterion() { return criterion; }
    public int score() { return score; }
    public Instant createdAt() { return createdAt; }
    public MatchStatus status() { return status; }
    public Instant answeredAt() { return answeredAt; }
}
