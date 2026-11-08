package com.sangreyoro.matching.data;

import com.sangreyoro.matching.domain.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe in-memory notification store. */
public class InMemoryNotificationRepository implements NotificationRepository {
    private final Map<String, List<Notification>> byStudent = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public String nextId() {
        return "N-" + sequence.incrementAndGet();
    }

    @Override
    public void save(Notification n) {
        byStudent.computeIfAbsent(n.studentId(), k -> new CopyOnWriteArrayList<>()).add(n);
    }

    @Override
    public List<Notification> findByStudent(String studentId) {
        List<Notification> list = new ArrayList<>(byStudent.getOrDefault(studentId, List.of()));
        list.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        return list;
    }
}
