package com.sangreyoro.matching.business.notification;

import com.sangreyoro.matching.domain.MatchRequest;
import com.sangreyoro.matching.domain.Student;

/** Data pushed to every observer (push model of the Observer pattern). */
public record MatchEvent(MatchEventType type, MatchRequest request, Student requester, Student candidate) {
}
