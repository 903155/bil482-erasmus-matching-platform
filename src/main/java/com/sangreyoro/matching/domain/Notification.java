package com.sangreyoro.matching.domain;

import java.time.Instant;

/** In-app notification shown in the notification panel (FR-3.4, FR-3.5). */
public final class Notification {
    private final String id;
    private final String studentId;
    private final String message;
    private final String requestId;
    private final Instant createdAt;
    private boolean read;

    public Notification(String id, String studentId, String message, String requestId, Instant createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.message = message;
        this.requestId = requestId;
        this.createdAt = createdAt;
    }

    public void markRead() { read = true; }
    public String id() { return id; }
    public String studentId() { return studentId; }
    public String message() { return message; }
    public String requestId() { return requestId; }
    public Instant createdAt() { return createdAt; }
    public boolean isRead() { return read; }
}
