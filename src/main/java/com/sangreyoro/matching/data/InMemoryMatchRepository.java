package com.sangreyoro.matching.data;

import com.sangreyoro.matching.domain.MatchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe in-memory implementation; the web server handles requests on several threads. */
public class InMemoryMatchRepository implements MatchRepository {
    private final Map<String, MatchRequest> byId = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public String nextId() {
        return "MR-" + sequence.incrementAndGet();
    }

    @Override
    public void save(MatchRequest request) {
        byId.put(request.id(), request);
    }

    @Override
    public Optional<MatchRequest> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<MatchRequest> findByStudent(String studentId) {
        List<MatchRequest> out = new ArrayList<>();
        for (MatchRequest r : byId.values()) if (r.involves(studentId)) out.add(r);
        out.sort((a, b) -> a.createdAt().compareTo(b.createdAt()));
        return out;
    }
}
