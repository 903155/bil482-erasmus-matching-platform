package com.sangreyoro.matching.testkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Dependency-free test runner. Runs every {@link Test} method of the given classes, prints one line per test
 * with its traceable id and the SRS items it covers, and exits with code 1 if any test fails.
 * <p>
 * Usage: {@code java -cp build/classes:build/test-classes com.sangreyoro.matching.testkit.TestRunner <TestClass>...}
 */
public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        // Application logs are switched off so the report shows only results (use -Dtest.logs=INFO to see them).
        com.sangreyoro.matching.util.LoggingConfig.init(
                java.util.logging.Level.parse(System.getProperty("test.logs", "OFF")), false);
        record Case(Class<?> type, Method method, Test info) {
        }
        List<Case> cases = new ArrayList<>();
        for (String name : args) {
            Class<?> c = Class.forName(name);
            for (Method m : c.getDeclaredMethods()) {
                Test t = m.getAnnotation(Test.class);
                if (t != null) cases.add(new Case(c, m, t));
            }
        }
        cases.sort(Comparator.comparing(k -> k.info().id()));
        int passed = 0;
        List<String> failures = new ArrayList<>();
        long all = System.nanoTime();
        for (Case k : cases) {
            long start = System.nanoTime();
            String result;
            try {
                Object instance = k.type().getDeclaredConstructor().newInstance();
                k.method().setAccessible(true);
                k.method().invoke(instance);
                passed++;
                result = "PASS";
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                result = "FAIL";
                failures.add(k.info().id() + " " + k.method().getName() + ": " + cause);
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("%-4s %-8s %-62s %5d ms  covers %s%n", result, k.info().id(),
                    k.method().getName(), ms, k.info().covers());
        }
        System.out.printf("%n%d tests, %d passed, %d failed (%d ms)%n", cases.size(), passed, failures.size(),
                (System.nanoTime() - all) / 1_000_000);
        if (!failures.isEmpty()) {
            System.out.println("\nFailures:");
            failures.forEach(f -> System.out.println("  " + f));
            System.exit(1);
        }
        if (cases.isEmpty()) {
            System.out.println("No tests found in " + Arrays.toString(args));
            System.exit(1);
        }
    }
}
