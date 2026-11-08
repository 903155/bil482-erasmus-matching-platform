package com.sangreyoro.matching.data;

import com.sangreyoro.matching.domain.Notification;

import java.util.List;

/** Stores in-app notifications so they are never lost, even if the user is offline (NFR-R.4, UC3-E5). */
public interface NotificationRepository {
    String nextId();

    void save(Notification n);

    /** Newest first. */
    List<Notification> findByStudent(String studentId);
}
