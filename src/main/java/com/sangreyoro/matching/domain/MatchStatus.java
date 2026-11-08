package com.sangreyoro.matching.domain;

/** States of a match request (FR-2.4): PENDING (UC2-S8), CONFIRMED (UC2-S9) or REJECTED (UC2-A3). */
public enum MatchStatus {
    PENDING, CONFIRMED, REJECTED
}
