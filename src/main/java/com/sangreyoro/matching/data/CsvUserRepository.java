package com.sangreyoro.matching.data;

import com.sangreyoro.matching.domain.LanguageLevel;
import com.sangreyoro.matching.domain.Student;
import com.sangreyoro.matching.domain.StudentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Read-only student repository backed by a semicolon-separated file (data/students.csv).
 * Profiles are written by UC1; in this UC2 prototype they are loaded once at start-up and kept in memory,
 * which gives O(1) look-ups by id.
 *
 * File format (one student per line, lists separated by commas):
 * id;name;type;department;nativeLanguages;learningLanguages(lang:level);hobbies;sports;availability;matchingEnabled;blockedIds;descriptionLanguage;description
 */
public class CsvUserRepository implements UserRepository {
    private static final Logger LOG = Logger.getLogger(CsvUserRepository.class.getName());
    private static final int FIELDS = 13;

    private final Map<String, Student> students;

    private CsvUserRepository(Map<String, Student> students) {
        this.students = Collections.unmodifiableMap(students);
    }

    public static CsvUserRepository fromFile(Path file) {
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parse(r, file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    public static CsvUserRepository fromClasspath(String resource) {
        InputStream in = CsvUserRepository.class.getResourceAsStream(resource);
        if (in == null) throw new IllegalStateException("Resource not found: " + resource);
        try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return parse(r, resource);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }

    public static CsvUserRepository of(List<Student> list) {
        Map<String, Student> map = new LinkedHashMap<>();
        for (Student s : list) map.put(s.id(), s);
        return new CsvUserRepository(map);
    }

    private static CsvUserRepository parse(Reader reader, String source) throws IOException {
        Map<String, Student> map = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(reader);
        String line;
        int lineNo = 0, skipped = 0;
        while ((line = br.readLine()) != null) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            try {
                Student s = parseLine(line);
                if (map.putIfAbsent(s.id(), s) != null) throw new IllegalArgumentException("duplicate id " + s.id());
            } catch (RuntimeException e) {
                // Data validation rule DV-06: a malformed line is skipped and logged, it never stops the system.
                skipped++;
                final int n = lineNo;
                LOG.warning(() -> source + ":" + n + " skipped – " + e.getMessage());
            }
        }
        final int loaded = map.size(), bad = skipped;
        LOG.info(() -> "Loaded " + loaded + " students from " + source + (bad > 0 ? " (" + bad + " lines skipped)" : ""));
        return new CsvUserRepository(map);
    }

    static Student parseLine(String line) {
        String[] f = line.split(";", -1);
        if (f.length != FIELDS) throw new IllegalArgumentException("expected " + FIELDS + " fields but found " + f.length);
        Student.Builder b = Student.builder(f[0].trim(), f[1].trim(), StudentType.valueOf(f[2].trim().toUpperCase()))
                .department(f[3].trim())
                .speaks(list(f[4]))
                .hobbies(list(f[6]))
                .sports(list(f[7]))
                .availableOn(list(f[8]))
                .matchingEnabled(Boolean.parseBoolean(f[9].trim()))
                .blocks(list(f[10]))
                .description(f[11].trim().isEmpty() ? "en" : f[11].trim(), f[12].trim());
        for (String pair : list(f[5])) {
            String[] p = pair.split(":");
            if (p.length != 2) throw new IllegalArgumentException("bad language level '" + pair + "'");
            b.learns(p[0], LanguageLevel.parse(p[1]));
        }
        return b.build();
    }

    private static String[] list(String field) {
        if (field.isBlank()) return new String[0];
        List<String> out = new ArrayList<>();
        for (String s : field.split(",")) if (!s.isBlank()) out.add(s.trim());
        return out.toArray(new String[0]);
    }

    @Override
    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    @Override
    public List<Student> findAll() {
        return List.copyOf(students.values());
    }
}
