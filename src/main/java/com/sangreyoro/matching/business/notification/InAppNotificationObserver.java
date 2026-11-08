package com.sangreyoro.matching.business.notification;

import com.sangreyoro.matching.data.NotificationRepository;
import com.sangreyoro.matching.domain.Notification;

import java.time.Clock;

/**
 * OBSERVER pattern – ConcreteObserver: writes the messages shown in the in-app notification panel
 * (FR-3.4, FR-3.5). Notifications are stored, so a user who is offline sees them later (NFR-R.4).
 * <p>
 * Privacy rule (UC2-A3): when a request is rejected the requester is NOT told who rejected it.
 */
public class InAppNotificationObserver implements MatchObserver {
    private final NotificationRepository repository;
    private final Clock clock;

    public InAppNotificationObserver(NotificationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void update(MatchEvent e) {
        switch (e.type()) {
            case MATCH_REQUESTED -> save(e.candidate().id(), e.requester().name() + " sent you a match request ("
                    + e.request().criterion().label() + ", score " + e.request().score() + ").", e);
            case MATCH_CONFIRMED -> {
                save(e.requester().id(), "You and " + e.candidate().name() + " are now matched! You can see each other's contact details.", e);
                save(e.candidate().id(), "You and " + e.requester().name() + " are now matched! You can see each other's contact details.", e);
            }
            case MATCH_REJECTED -> { /* no message to the requester – privacy rule UC2-A3 */ }
        }
    }

    private void save(String studentId, String message, MatchEvent e) {
        repository.save(new Notification(repository.nextId(), studentId, message, e.request().id(), clock.instant()));
    }
}
