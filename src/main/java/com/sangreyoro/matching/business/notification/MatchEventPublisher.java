package com.sangreyoro.matching.business.notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ============================================================================================
 * DESIGN PATTERN: OBSERVER (behavioral) – role: Subject                     [UC2-S8, UC2-S9]
 * --------------------------------------------------------------------------------------------
 * Intent:      define a one-to-many dependency so that when a match request is created, confirmed
 *              or rejected, all interested components are notified automatically.
 * Motivation:  a new match must reach several receivers (in-app notification panel, audit log and,
 *              in later iterations, e-mail or push). If MatchingService called each of them directly
 *              it would depend on all of them and every new channel would change the service.
 * Improvement: MatchingService only calls {@link #publish}; receivers attach themselves. The service
 *              has 1 dependency (this publisher) instead of N channel classes.
 * Metrics:     - adding a notification channel = 1 new MatchObserver class + 1 attach() call;
 *                lines changed in MatchingService = 0 (test UC2-T12 attaches an extra observer);
 *              - fault isolation: an exception in one observer does not stop the others
 *                (test UC2-T13), so a failing channel cannot lose the in-app notification (NFR-R.4).
 * ============================================================================================
 */
public class MatchEventPublisher {
    private static final Logger LOG = Logger.getLogger(MatchEventPublisher.class.getName());

    /** CopyOnWriteArrayList: observers can attach/detach while another thread is notifying. */
    private final List<MatchObserver> observers = new CopyOnWriteArrayList<>();

    public void attach(MatchObserver o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    public void detach(MatchObserver o) {
        observers.remove(o);
    }

    public int observerCount() {
        return observers.size();
    }

    /** notify(): calls update() on every observer; one failing observer is logged and skipped. */
    public void publish(MatchEvent event) {
        for (MatchObserver o : observers) {
            try {
                o.update(event);
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Observer " + o.getClass().getSimpleName() + " failed for "
                        + event.type() + " " + event.request().id(), e);
            }
        }
    }
}
