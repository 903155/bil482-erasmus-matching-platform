package com.sangreyoro.matching.data;

import com.sangreyoro.matching.domain.Student;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Layer interface for student profiles (SRS 6.3, NFR-PO.4).
 * The Business Layer depends only on this interface, so the CSV file can be replaced by a
 * relational database by writing one new implementation.
 */
public interface UserRepository {
    Optional<Student> findById(String id);

    List<Student> findAll();
}
