package com.sangreyoro.matching;

import com.sangreyoro.matching.business.MatchingService;
import com.sangreyoro.matching.business.notification.InAppNotificationObserver;
import com.sangreyoro.matching.business.notification.MatchAuditLogObserver;
import com.sangreyoro.matching.business.notification.MatchEventPublisher;
import com.sangreyoro.matching.business.strategy.MatchingStrategyRegistry;
import com.sangreyoro.matching.business.translation.MockTranslationApiClient;
import com.sangreyoro.matching.business.translation.ProfileDescriptionService;
import com.sangreyoro.matching.business.translation.TranslationAdapter;
import com.sangreyoro.matching.data.CsvUserRepository;
import com.sangreyoro.matching.data.InMemoryMatchRepository;
import com.sangreyoro.matching.data.InMemoryNotificationRepository;
import com.sangreyoro.matching.data.UserRepository;
import com.sangreyoro.matching.presentation.MatchController;
import com.sangreyoro.matching.presentation.WebServer;
import com.sangreyoro.matching.util.LoggingConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.logging.Level;

/**
 * Composition root: creates every component of the three layers and connects them (manual Dependency
 * Injection). This is the only class that knows the concrete implementations.
 * <p>
 * Usage: {@code java -jar matching-uc2.jar [port] [students.csv]} (defaults: 8080, data/students.csv,
 * or the copy packaged in the jar).
 */
public final class MatchingApplication {
    private MatchingApplication() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Path data = Path.of(args.length > 1 ? args[1] : System.getenv().getOrDefault("STUDENTS_FILE", "data/students.csv"));
        LoggingConfig.init(Level.parse(System.getenv().getOrDefault("LOG_LEVEL", "INFO")), true);

        // Data Access Layer
        UserRepository users = Files.exists(data) ? CsvUserRepository.fromFile(data)
                : CsvUserRepository.fromClasspath("/data/students.csv");
        var matches = new InMemoryMatchRepository();
        var notifications = new InMemoryNotificationRepository();

        // Business Layer – Observer: attach the observers to the subject
        Clock clock = Clock.systemDefaultZone();
        MatchEventPublisher events = new MatchEventPublisher();
        events.attach(new InAppNotificationObserver(notifications, clock));
        events.attach(new MatchAuditLogObserver());

        // Business Layer – Strategy: register the three matching criteria
        MatchingService matching = new MatchingService(users, matches, MatchingStrategyRegistry.withDefaults(), events, clock);

        // Business Layer – Adapter: the mock third-party API behind our TranslationService interface
        ProfileDescriptionService descriptions = new ProfileDescriptionService(users,
                new TranslationAdapter(new MockTranslationApiClient()));

        // Presentation Layer
        WebServer server = new WebServer(port, new MatchController(matching, descriptions, users, notifications));
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
