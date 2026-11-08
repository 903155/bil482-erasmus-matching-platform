package com.sangreyoro.matching;

import com.sangreyoro.matching.business.MatchingService;
import com.sangreyoro.matching.business.notification.InAppNotificationObserver;
import com.sangreyoro.matching.business.notification.MatchEventPublisher;
import com.sangreyoro.matching.business.strategy.MatchingStrategyRegistry;
import com.sangreyoro.matching.data.CsvUserRepository;
import com.sangreyoro.matching.data.InMemoryMatchRepository;
import com.sangreyoro.matching.data.InMemoryNotificationRepository;
import com.sangreyoro.matching.data.UserRepository;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Builds the UC2 object graph for tests with Dependency Injection: same classes as production, but a fixed
 * clock and fresh in-memory repositories for every test, so tests never share state.
 */
final class Fixture {
    final UserRepository users;
    final InMemoryMatchRepository matches = new InMemoryMatchRepository();
    final InMemoryNotificationRepository notifications = new InMemoryNotificationRepository();
    final MatchEventPublisher events = new MatchEventPublisher();
    final MatchingStrategyRegistry strategies = MatchingStrategyRegistry.withDefaults();
    final Clock clock = Clock.fixed(Instant.parse("2026-11-06T10:00:00Z"), ZoneOffset.UTC);
    final MatchingService service;

    Fixture() {
        this(CsvUserRepository.fromFile(Path.of("data/students.csv")));
    }

    Fixture(UserRepository users) {
        this.users = users;
        events.attach(new InAppNotificationObserver(notifications, clock));
        service = new MatchingService(users, matches, strategies, events, clock);
    }
}
