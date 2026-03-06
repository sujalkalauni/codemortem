package com.codemortem.repository;

import com.codemortem.entity.Debrief;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebriefRepository extends JpaRepository<Debrief, Long> {
    List<Debrief> findByIncidentId(Long incidentId);
    Optional<Debrief> findByIncidentIdAndAuthorId(Long incidentId, Long authorId);
}
