package com.sangreyoro.matching.business.notification;

import java.util.logging.Logger;

/**
 * OBSERVER pattern – ConcreteObserver: writes every match event to the application log
 * (SDD section "Error Handling and Logging"). Added without any change to MatchingService.
 */
public class MatchAuditLogObserver implements MatchObserver {
    private static final Logger LOG = Logger.getLogger("matching.audit");

    @Override
    public void update(MatchEvent e) {
        LOG.info(() -> String.format("%s request=%s requester=%s candidate=%s criterion=%s score=%d status=%s",
                e.type(), e.request().id(), e.requester().id(), e.candidate().id(),
                e.request().criterion(), e.request().score(), e.request().status()));
    }
}
