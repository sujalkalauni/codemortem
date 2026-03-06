package com.codemortem.controller;

import com.codemortem.dto.Dtos.*;
import com.codemortem.entity.Incident;
import com.codemortem.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Create and manage incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @Operation(summary = "Report a new incident")
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createIncident(request));
    }

    @GetMapping
    @Operation(summary = "List all incidents")
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Mark an incident as resolved")
    public ResponseEntity<IncidentResponse> resolveIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.resolveIncident(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update incident status")
    public ResponseEntity<IncidentResponse> updateStatus(@PathVariable Long id,
                                                          @RequestParam Incident.IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Get all incidents for a specific service")
    public ResponseEntity<List<IncidentResponse>> getByService(@PathVariable String serviceName) {
        return ResponseEntity.ok(incidentService.getByService(serviceName));
    }
}
