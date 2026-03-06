package com.codemortem.analytics;

import com.codemortem.dto.Dtos.*;
import com.codemortem.entity.Incident;
import com.codemortem.entity.User;
import com.codemortem.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testdev");
        testUser.setEmail("test@dev.com");
        testUser.setPassword("encoded");
    }

    @Test
    void getSummary_returnsCorrectCounts() {
        Incident open = makeIncident(1L, Incident.IncidentStatus.OPEN, Incident.Severity.HIGH, null);
        Incident resolved = makeIncident(2L, Incident.IncidentStatus.RESOLVED, Incident.Severity.CRITICAL,
                LocalDateTime.now().minusHours(2));
        Incident critical = makeIncident(3L, Incident.IncidentStatus.OPEN, Incident.Severity.CRITICAL, null);

        when(incidentRepository.findAll()).thenReturn(List.of(open, resolved, critical));

        AnalyticsSummaryDto summary = analyticsService.getSummary();

        assertThat(summary.getTotalIncidents()).isEqualTo(3);
        assertThat(summary.getOpenIncidents()).isEqualTo(2);
        assertThat(summary.getResolvedIncidents()).isEqualTo(1);
        assertThat(summary.getCriticalIncidents()).isEqualTo(2);
    }

    @Test
    void getSummary_calculatesMttr_forResolvedIncidents() {
        LocalDateTime occurred = LocalDateTime.now().minusHours(4);
        LocalDateTime resolved = LocalDateTime.now().minusHours(2);

        Incident i = makeIncident(1L, Incident.IncidentStatus.RESOLVED, Incident.Severity.HIGH, resolved);
        i.setOccurredAt(occurred);

        when(incidentRepository.findAll()).thenReturn(List.of(i));

        AnalyticsSummaryDto summary = analyticsService.getSummary();

        assertThat(summary.getOverallMttrSeconds()).isNotNull();
        // 2 hours = 7200 seconds
        assertThat(summary.getOverallMttrSeconds()).isEqualTo(7200.0);
        assertThat(summary.getOverallMttrFormatted()).isEqualTo("2h 0m");
    }

    @Test
    void getSummary_noMttr_whenNoResolvedIncidents() {
        Incident open = makeIncident(1L, Incident.IncidentStatus.OPEN, Incident.Severity.LOW, null);
        when(incidentRepository.findAll()).thenReturn(List.of(open));

        AnalyticsSummaryDto summary = analyticsService.getSummary();

        assertThat(summary.getOverallMttrSeconds()).isNull();
    }

    @Test
    void getRecurringPatterns_delegatesToRepository() {
        Object[] row = new Object[]{"DATABASE_TIMEOUT", "payment-service", 3L};
        when(incidentRepository.findRecurringFailurePatterns(any())).thenReturn(List.of(row));

        List<FailurePatternDto> patterns = analyticsService.getRecurringFailurePatterns(90);

        assertThat(patterns).hasSize(1);
        assertThat(patterns.get(0).getFailureCategory()).isEqualTo("DATABASE_TIMEOUT");
        assertThat(patterns.get(0).getServiceName()).isEqualTo("payment-service");
        assertThat(patterns.get(0).getOccurrenceCount()).isEqualTo(3L);
    }

    @Test
    void getMttrPerService_formatsCorrectly() {
        Object[] row = new Object[]{"auth-service", 3600.0};
        when(incidentRepository.findMttrPerService()).thenReturn(List.of(row));

        List<MttrDto> mttr = analyticsService.getMttrPerService();

        assertThat(mttr).hasSize(1);
        assertThat(mttr.get(0).getServiceName()).isEqualTo("auth-service");
        assertThat(mttr.get(0).getAverageResolutionFormatted()).isEqualTo("1h 0m");
    }

    // ── Helpers ──────────────────────────────────────────

    private Incident makeIncident(Long id, Incident.IncidentStatus status,
                                   Incident.Severity severity, LocalDateTime resolvedAt) {
        Incident i = new Incident();
        i.setId(id);
        i.setTitle("Test Incident " + id);
        i.setStatus(status);
        i.setSeverity(severity);
        i.setServiceName("test-service");
        i.setFailureCategory("TEST_FAILURE");
        i.setOccurredAt(LocalDateTime.now().minusHours(5));
        i.setResolvedAt(resolvedAt);
        i.setReporter(testUser);
        return i;
    }
}
