package com.codemortem.analytics;

import com.codemortem.dto.Dtos.*;
import com.codemortem.entity.Incident;
import com.codemortem.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final IncidentRepository incidentRepository;

    /**
     * Detects recurring failure patterns — same category + service appearing more than once
     * in the given lookback window (default: last 90 days).
     */
    public List<FailurePatternDto> getRecurringFailurePatterns(int lookbackDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(lookbackDays);
        List<Object[]> results = incidentRepository.findRecurringFailurePatterns(since);

        return results.stream()
                .map(row -> new FailurePatternDto(
                        (String) row[0],   // failureCategory
                        (String) row[1],   // serviceName
                        (Long) row[2]      // count
                ))
                .collect(Collectors.toList());
    }

    /**
     * Mean Time To Resolve (MTTR) per service — in seconds and human-readable format.
     */
    public List<MttrDto> getMttrPerService() {
        List<Object[]> results = incidentRepository.findMttrPerService();

        return results.stream()
                .map(row -> new MttrDto(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * Overall analytics summary — total counts, open/resolved split, overall MTTR.
     */
    public AnalyticsSummaryDto getSummary() {
        List<Incident> all = incidentRepository.findAll();

        AnalyticsSummaryDto summary = new AnalyticsSummaryDto();
        summary.setTotalIncidents(all.size());
        summary.setOpenIncidents((int) all.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.OPEN).count());
        summary.setResolvedIncidents((int) all.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.RESOLVED).count());
        summary.setCriticalIncidents((int) all.stream()
                .filter(i -> i.getSeverity() == Incident.Severity.CRITICAL).count());

        // Overall MTTR across all resolved incidents
        OptionalDouble overallMttr = all.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.RESOLVED
                        && i.getResolvedAt() != null)
                .mapToDouble(i -> java.time.Duration.between(i.getOccurredAt(), i.getResolvedAt()).getSeconds())
                .average();

        if (overallMttr.isPresent()) {
            summary.setOverallMttrSeconds(overallMttr.getAsDouble());
            summary.setOverallMttrFormatted(formatSeconds((long) overallMttr.getAsDouble()));
        }

        return summary;
    }

    private String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%dh %dm", hours, minutes);
    }
}
