package com.sangreyoro.matching.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Student profile created in UC1 and read by UC2 (FR-1.3).
 * Immutable: the matching code can never change a profile by accident.
 */
public final class Student {
    private final String id;
    private final String name;
    private final StudentType type;
    private final String department;
    private final String descriptionLanguage;
    private final String description;
    private final Set<String> nativeLanguages;
    private final Map<String, LanguageLevel> learningLanguages;
    private final Set<String> hobbies;
    private final Set<String> sports;
    private final Set<String> availability;
    private final boolean matchingEnabled;
    private final Set<String> blockedIds;

    private Student(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.type = b.type;
        this.department = b.department;
        this.descriptionLanguage = b.descriptionLanguage;
        this.description = b.description;
        this.nativeLanguages = Collections.unmodifiableSet(new LinkedHashSet<>(b.nativeLanguages));
        this.learningLanguages = Collections.unmodifiableMap(new LinkedHashMap<>(b.learningLanguages));
        this.hobbies = Collections.unmodifiableSet(new LinkedHashSet<>(b.hobbies));
        this.sports = Collections.unmodifiableSet(new LinkedHashSet<>(b.sports));
        this.availability = Collections.unmodifiableSet(new LinkedHashSet<>(b.availability));
        this.matchingEnabled = b.matchingEnabled;
        this.blockedIds = Collections.unmodifiableSet(new LinkedHashSet<>(b.blockedIds));
    }

    /**
     * A profile is complete when it has at least one hobby or sport and at least one language to
     * practise (UC1-S8). Incomplete profiles cannot search (UC2-E1) and are never shown as candidates.
     */
    public boolean isProfileComplete() {
        return !(hobbies.isEmpty() && sports.isEmpty()) && !learningLanguages.isEmpty() && !nativeLanguages.isEmpty();
    }

    public boolean hasBlocked(Student other) {
        return blockedIds.contains(other.id);
    }

    public String id() { return id; }
    public String name() { return name; }
    public StudentType type() { return type; }
    public String department() { return department; }
    public String descriptionLanguage() { return descriptionLanguage; }
    public String description() { return description; }
    public Set<String> nativeLanguages() { return nativeLanguages; }
    public Map<String, LanguageLevel> learningLanguages() { return learningLanguages; }
    public Set<String> hobbies() { return hobbies; }
    public Set<String> sports() { return sports; }
    public Set<String> availability() { return availability; }
    public boolean matchingEnabled() { return matchingEnabled; }
    public Set<String> blockedIds() { return blockedIds; }

    @Override
    public String toString() {
        return name + " (" + id + ", " + type + ")";
    }

    public static Builder builder(String id, String name, StudentType type) {
        return new Builder(id, name, type);
    }

    /** Builder: a student has many optional fields, so a fluent builder keeps tests and the CSV loader readable. */
    public static final class Builder {
        private final String id;
        private final String name;
        private final StudentType type;
        private String department = "";
        private String descriptionLanguage = "en";
        private String description = "";
        private final Set<String> nativeLanguages = new LinkedHashSet<>();
        private final Map<String, LanguageLevel> learningLanguages = new LinkedHashMap<>();
        private final Set<String> hobbies = new LinkedHashSet<>();
        private final Set<String> sports = new LinkedHashSet<>();
        private final Set<String> availability = new LinkedHashSet<>();
        private boolean matchingEnabled = true;
        private final Set<String> blockedIds = new LinkedHashSet<>();

        private Builder(String id, String name, StudentType type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public Builder department(String d) { this.department = d; return this; }
        public Builder description(String lang, String text) { this.descriptionLanguage = lang; this.description = text; return this; }
        public Builder speaks(String... langs) { for (String l : langs) nativeLanguages.add(norm(l)); return this; }
        public Builder learns(String lang, LanguageLevel level) { learningLanguages.put(norm(lang), level); return this; }
        public Builder hobbies(String... h) { for (String x : h) hobbies.add(norm(x)); return this; }
        public Builder sports(String... s) { for (String x : s) sports.add(norm(x)); return this; }
        public Builder availableOn(String... slots) { for (String x : slots) availability.add(norm(x)); return this; }
        public Builder matchingEnabled(boolean e) { this.matchingEnabled = e; return this; }
        public Builder blocks(String... ids) { for (String x : ids) blockedIds.add(x.trim()); return this; }

        public Student build() {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Student id is required");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Student name is required");
            if (type == null) throw new IllegalArgumentException("Student type is required");
            return new Student(this);
        }

        private static String norm(String s) {
            return s.trim().toLowerCase();
        }
    }
}
