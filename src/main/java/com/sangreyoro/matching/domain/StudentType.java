package com.sangreyoro.matching.domain;

/** Type of student (SRS 2.3). Erasmus students are matched with local students by default (UC2-S2). */
public enum StudentType {
    ERASMUS, LOCAL;

    public StudentType opposite() {
        return this == ERASMUS ? LOCAL : ERASMUS;
    }
}
