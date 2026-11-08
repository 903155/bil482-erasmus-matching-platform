package com.sangreyoro.matching.business.notification;

/**
 * OBSERVER pattern – role: Observer interface (UC2-S8, UC2-S9; reuses the UC3 notification mechanism).
 * Every component that must react to a match event implements {@link #update}.
 */
public interface MatchObserver {
    void update(MatchEvent event);
}
