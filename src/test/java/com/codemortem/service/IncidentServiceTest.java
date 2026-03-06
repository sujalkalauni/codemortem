package com.codemortem.service;

import com.codemortem.dto.Dtos.*;
import com.codemortem.entity.Incident;
import com.codemortem.entity.User;
import com.codemortem.repository.IncidentRepository;
import com.codemortem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private IncidentService incidentService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testdev");
        testUser.setEmail("test@dev.com");
        testUser.setPassword("encoded");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testdev");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("testdev")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createIncident_savesAndReturnsResponse() {
        IncidentRequest request = new IncidentRequest();
        request.setTitle("DB Connection Pool Exhausted");
        request.setSeverity(Incident.Severity.HIGH);
        request.setServiceName("payment-service");
        request.setFailureCategory("DATABASE_TIMEOUT");
        request.setOccurredAt(LocalDateTime.now());

        Incident saved = new Incident();
        saved.setId(1L);
        saved.setTitle(request.getTitle());
        saved.setSeverity(request.getSeverity());
        saved.setServiceName(request.getServiceName());
        saved.setFailureCategory(request.getFailureCategory());
        saved.setOccurredAt(request.getOccurredAt());
        saved.setStatus(Incident.IncidentStatus.OPEN);
        saved.setReporter(testUser);

        when(incidentRepository.save(any(Incident.class))).thenReturn(saved);

        IncidentResponse response = incidentService.createIncident(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("DB Connection Pool Exhausted");
        assertThat(response.getStatus()).isEqualTo(Incident.IncidentStatus.OPEN);
        assertThat(response.getReporterUsername()).isEqualTo("testdev");
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    void resolveIncident_setsResolvedAtAndStatus() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle("Test");
        incident.setStatus(Incident.IncidentStatus.OPEN);
        incident.setOccurredAt(LocalDateTime.now().minusHours(3));
        incident.setReporter(testUser);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResponse response = incidentService.resolveIncident(1L);

        assertThat(response.getStatus()).isEqualTo(Incident.IncidentStatus.RESOLVED);
        assertThat(response.getResolvedAt()).isNotNull();
    }

    @Test
    void getIncidentById_throwsWhenNotFound() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.getIncidentById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Incident not found");
    }

    @Test
    void getAllIncidents_returnsMappedList() {
        Incident i1 = new Incident();
        i1.setId(1L); i1.setTitle("One"); i1.setReporter(testUser);
        i1.setOccurredAt(LocalDateTime.now()); i1.setStatus(Incident.IncidentStatus.OPEN);
        i1.setSeverity(Incident.Severity.LOW); i1.setServiceName("svc");

        Incident i2 = new Incident();
        i2.setId(2L); i2.setTitle("Two"); i2.setReporter(testUser);
        i2.setOccurredAt(LocalDateTime.now()); i2.setStatus(Incident.IncidentStatus.OPEN);
        i2.setSeverity(Incident.Severity.HIGH); i2.setServiceName("svc");

        when(incidentRepository.findAll()).thenReturn(List.of(i1, i2));

        List<IncidentResponse> responses = incidentService.getAllIncidents();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("One");
        assertThat(responses.get(1).getTitle()).isEqualTo("Two");
    }
}
