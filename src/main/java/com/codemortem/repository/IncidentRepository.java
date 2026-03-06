package com.codemortem.repository;

import com.codemortem.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByServiceName(String serviceName);

    List<Incident> findByFailureCategory(String failureCategory);

    List<Incident> findBySeverity(Incident.Severity severity);

    // For recurring failure pattern detection — group by category and service
    @Query("SELECT i.failureCategory, i.serviceName, COUNT(i) as count " +
           "FROM Incident i " +
           "WHERE i.occurredAt >= :since " +
           "GROUP BY i.failureCategory, i.serviceName " +
           "HAVING COUNT(i) > 1 " +
           "ORDER BY count DESC")
    List<Object[]> findRecurringFailurePatterns(@Param("since") LocalDateTime since);

    // For MTTR calculation — only resolved incidents
    @Query("SELECT i FROM Incident i " +
           "WHERE i.status = 'RESOLVED' " +
           "AND i.resolvedAt IS NOT NULL " +
           "AND i.occurredAt >= :since")
    List<Incident> findResolvedIncidentsSince(@Param("since") LocalDateTime since);

    // MTTR per service
    @Query("SELECT i.serviceName, AVG(TIMESTAMPDIFF(SECOND, i.occurredAt, i.resolvedAt)) " +
           "FROM Incident i " +
           "WHERE i.status = 'RESOLVED' AND i.resolvedAt IS NOT NULL " +
           "GROUP BY i.serviceName")
    List<Object[]> findMttrPerService();

    @Query("SELECT i FROM Incident i WHERE i.occurredAt BETWEEN :start AND :end")
    List<Incident> findIncidentsBetween(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}
