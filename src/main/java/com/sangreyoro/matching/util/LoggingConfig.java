package com.sangreyoro.matching.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logging set-up (SDD "Logging Mechanisms"): java.util.logging, one line per record, to the console and to a
 * rotating file logs/matching-%g.log (5 files × 1 MB).
 */
public final class LoggingConfig {
    private LoggingConfig() {
    }

    public static void init(Level level, boolean toFile) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$-7s [%3$s] %5$s%6$s%n");
        Logger root = Logger.getLogger("");
        for (var h : root.getHandlers()) root.removeHandler(h);
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(level);
        console.setFormatter(new SimpleFormatter());
        root.addHandler(console);
        root.setLevel(level);
        if (toFile) {
            try {
                Files.createDirectories(Path.of("logs"));
                FileHandler file = new FileHandler("logs/matching-%g.log", 1_000_000, 5, true);
                file.setFormatter(new SimpleFormatter());
                file.setLevel(level);
                root.addHandler(file);
            } catch (IOException e) {
                root.warning("File logging disabled: " + e.getMessage());
            }
        }
    }
}
