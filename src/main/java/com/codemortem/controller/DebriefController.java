package com.codemortem.controller;

import com.codemortem.dto.Dtos.*;
import com.codemortem.service.DebriefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/debriefs")
@RequiredArgsConstructor
@Tag(name = "Debriefs", description = "Post-incident debrief management")
public class DebriefController {

    private final DebriefService debriefService;

    @PostMapping
    @Operation(summary = "Submit a post-incident debrief")
    public ResponseEntity<DebriefResponse> createDebrief(@PathVariable Long incidentId,
                                                          @Valid @RequestBody DebriefRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(debriefService.createDebrief(incidentId, request));
    }

    @GetMapping
    @Operation(summary = "Get all debriefs for an incident")
    public ResponseEntity<List<DebriefResponse>> getDebriefs(@PathVariable Long incidentId) {
        return ResponseEntity.ok(debriefService.getDebriefsByIncident(incidentId));
    }

    @GetMapping("/{debriefId}")
    @Operation(summary = "Get a specific debrief")
    public ResponseEntity<DebriefResponse> getDebrief(@PathVariable Long incidentId,
                                                       @PathVariable Long debriefId) {
        return ResponseEntity.ok(debriefService.getDebriefById(debriefId));
    }

    @PutMapping("/{debriefId}")
    @Operation(summary = "Update a debrief")
    public ResponseEntity<DebriefResponse> updateDebrief(@PathVariable Long incidentId,
                                                          @PathVariable Long debriefId,
                                                          @Valid @RequestBody DebriefRequest request) {
        return ResponseEntity.ok(debriefService.updateDebrief(debriefId, request));
    }
}
