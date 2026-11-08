package com.sangreyoro.matching.testkit;

import java.util.Objects;

/** Minimal assertions (same names as JUnit 5, so the tests can be migrated by changing the imports). */
public final class Assert {
    private Assert() {
    }

    public static void assertTrue(boolean c, String msg) {
        if (!c) throw new AssertionError(msg);
    }

    public static void assertFalse(boolean c, String msg) {
        assertTrue(!c, msg);
    }

    public static void assertEquals(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual))
            throw new AssertionError(msg + " – expected <" + expected + "> but was <" + actual + ">");
    }

    public static <T extends Throwable> T assertThrows(Class<T> type, Runnable code, String msg) {
        try {
            code.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) return type.cast(t);
            throw new AssertionError(msg + " – expected " + type.getSimpleName() + " but got " + t, t);
        }
        throw new AssertionError(msg + " – expected " + type.getSimpleName() + " but nothing was thrown");
    }
}
