package com.codemortem.service;

import com.codemortem.dto.Dtos.*;
import com.codemortem.entity.Incident;
import com.codemortem.entity.User;
import com.codemortem.repository.IncidentRepository;
import com.codemortem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public IncidentResponse createIncident(IncidentRequest request) {
        User currentUser = getCurrentUser();

        Incident incident = new Incident();
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());
        incident.setServiceName(request.getServiceName());
        incident.setFailureCategory(request.getFailureCategory());
        incident.setOccurredAt(request.getOccurredAt());
        incident.setReporter(currentUser);

        return IncidentResponse.from(incidentRepository.save(incident));
    }

    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(IncidentResponse::from)
                .collect(Collectors.toList());
    }

    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
        return IncidentResponse.from(incident);
    }

    public IncidentResponse resolveIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
        incident.setStatus(Incident.IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    public IncidentResponse updateStatus(Long id, Incident.IncidentStatus status) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
        incident.setStatus(status);
        if (status == Incident.IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
            incident.setResolvedAt(LocalDateTime.now());
        }
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    public List<IncidentResponse> getByService(String serviceName) {
        return incidentRepository.findByServiceName(serviceName)
                .stream().map(IncidentResponse::from).collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
