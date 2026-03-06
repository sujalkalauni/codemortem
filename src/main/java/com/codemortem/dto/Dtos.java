package com.codemortem.dto;

import com.codemortem.entity.Incident;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class Dtos {

    // ── Auth ──────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank @Email private String email;
        @NotBlank private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        private String email;

        public AuthResponse(String token, String username, String email) {
            this.token = token;
            this.username = username;
            this.email = email;
        }
    }

    // ── Incident ──────────────────────────────────────────

    @Data
    public static class IncidentRequest {
        @NotBlank private String title;
        private String description;
        @NotNull private Incident.Severity severity;
        @NotBlank private String serviceName;
        private String failureCategory;
        @NotNull private LocalDateTime occurredAt;
    }

    @Data
    public static class IncidentResponse {
        private Long id;
        private String title;
        private String description;
        private Incident.Severity severity;
        private Incident.IncidentStatus status;
        private String serviceName;
        private String failureCategory;
        private LocalDateTime occurredAt;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;
        private String reporterUsername;

        public static IncidentResponse from(Incident i) {
            IncidentResponse r = new IncidentResponse();
            r.id = i.getId();
            r.title = i.getTitle();
            r.description = i.getDescription();
            r.severity = i.getSeverity();
            r.status = i.getStatus();
            r.serviceName = i.getServiceName();
            r.failureCategory = i.getFailureCategory();
            r.occurredAt = i.getOccurredAt();
            r.resolvedAt = i.getResolvedAt();
            r.createdAt = i.getCreatedAt();
            r.reporterUsername = i.getReporter() != null ? i.getReporter().getUsername() : null;
            return r;
        }
    }

    // ── Debrief ──────────────────────────────────────────

    @Data
    public static class DebriefRequest {
        @NotBlank private String rootCause;
        private String timelineSummary;
        private String actionItems;
        private String lessonsLearned;
    }

    @Data
    public static class DebriefResponse {
        private Long id;
        private Long incidentId;
        private String rootCause;
        private String timelineSummary;
        private String actionItems;
        private String lessonsLearned;
        private String authorUsername;
        private LocalDateTime createdAt;
    }

    // ── Analytics ────────────────────────────────────────

    @Data
    public static class FailurePatternDto {
        private String failureCategory;
        private String serviceName;
        private Long occurrenceCount;

        public FailurePatternDto(String failureCategory, String serviceName, Long occurrenceCount) {
            this.failureCategory = failureCategory;
            this.serviceName = serviceName;
            this.occurrenceCount = occurrenceCount;
        }
    }

    @Data
    public static class MttrDto {
        private String serviceName;
        private Double averageResolutionSeconds;
        private String averageResolutionFormatted;

        public MttrDto(String serviceName, Double averageResolutionSeconds) {
            this.serviceName = serviceName;
            this.averageResolutionSeconds = averageResolutionSeconds;
            this.averageResolutionFormatted = formatSeconds(averageResolutionSeconds);
        }

        private String formatSeconds(Double seconds) {
            if (seconds == null) return "N/A";
            long s = seconds.longValue();
            long hours = s / 3600;
            long minutes = (s % 3600) / 60;
            return String.format("%dh %dm", hours, minutes);
        }
    }

    @Data
    public static class AnalyticsSummaryDto {
        private int totalIncidents;
        private int openIncidents;
        private int resolvedIncidents;
        private int criticalIncidents;
        private Double overallMttrSeconds;
        private String overallMttrFormatted;
    }
}
