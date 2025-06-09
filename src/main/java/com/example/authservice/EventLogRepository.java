package com.example.authservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    long countByEventType(EventType eventType);
    java.util.List<EventLog> findTop20ByOrderByTimestampDesc();

    @org.springframework.data.jpa.repository.Query("select function('date', e.timestamp) as day, count(e) from EventLog e where e.eventType = :type group by function('date', e.timestamp) order by day")
    java.util.List<Object[]> countPerDay(EventType type);
}
