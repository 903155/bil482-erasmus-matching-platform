package com.sangreyoro.matching.data;

import com.sangreyoro.matching.domain.MatchRequest;

import java.util.List;
import java.util.Optional;

/** Data Access Layer interface for match requests (FR-2.4). */
public interface MatchRepository {
    String nextId();

    void save(MatchRequest request);

    Optional<MatchRequest> findById(String id);

    /** All requests in which the student is requester or candidate. */
    List<MatchRequest> findByStudent(String studentId);
}
