package com.codemortem.service;

import com.codemortem.dto.Dtos.*;
import com.codemortem.entity.Debrief;
import com.codemortem.entity.Incident;
import com.codemortem.entity.User;
import com.codemortem.repository.DebriefRepository;
import com.codemortem.repository.IncidentRepository;
import com.codemortem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebriefService {

    private final DebriefRepository debriefRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public DebriefResponse createDebrief(Long incidentId, DebriefRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
        User currentUser = getCurrentUser();

        Debrief debrief = new Debrief();
        debrief.setIncident(incident);
        debrief.setAuthor(currentUser);
        debrief.setRootCause(request.getRootCause());
        debrief.setTimelineSummary(request.getTimelineSummary());
        debrief.setActionItems(request.getActionItems());
        debrief.setLessonsLearned(request.getLessonsLearned());

        return toResponse(debriefRepository.save(debrief));
    }

    public List<DebriefResponse> getDebriefsByIncident(Long incidentId) {
        return debriefRepository.findByIncidentId(incidentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DebriefResponse getDebriefById(Long id) {
        return toResponse(debriefRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debrief not found: " + id)));
    }

    public DebriefResponse updateDebrief(Long id, DebriefRequest request) {
        Debrief debrief = debriefRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debrief not found: " + id));

        debrief.setRootCause(request.getRootCause());
        debrief.setTimelineSummary(request.getTimelineSummary());
        debrief.setActionItems(request.getActionItems());
        debrief.setLessonsLearned(request.getLessonsLearned());

        return toResponse(debriefRepository.save(debrief));
    }

    private DebriefResponse toResponse(Debrief d) {
        DebriefResponse r = new DebriefResponse();
        r.setId(d.getId());
        r.setIncidentId(d.getIncident().getId());
        r.setRootCause(d.getRootCause());
        r.setTimelineSummary(d.getTimelineSummary());
        r.setActionItems(d.getActionItems());
        r.setLessonsLearned(d.getLessonsLearned());
        r.setAuthorUsername(d.getAuthor() != null ? d.getAuthor().getUsername() : null);
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
