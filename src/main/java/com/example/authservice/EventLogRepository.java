package com.example.authservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    long countByEventType(EventType eventType);
    java.util.List<EventLog> findTop20ByOrderByTimestampDesc();
}
