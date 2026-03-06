package com.codemortem.controller;

import com.codemortem.analytics.AnalyticsService;
import com.codemortem.dto.Dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Failure pattern detection and MTTR metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/patterns")
    @Operation(summary = "Detect recurring failure patterns",
               description = "Returns failure categories + services that have appeared more than once in the lookback window")
    public ResponseEntity<List<FailurePatternDto>> getRecurringPatterns(
            @RequestParam(defaultValue = "90") int lookbackDays) {
        return ResponseEntity.ok(analyticsService.getRecurringFailurePatterns(lookbackDays));
    }

    @GetMapping("/mttr")
    @Operation(summary = "Mean Time To Resolve per service",
               description = "Returns average resolution time in seconds and human-readable format per service")
    public ResponseEntity<List<MttrDto>> getMttr() {
        return ResponseEntity.ok(analyticsService.getMttrPerService());
    }

    @GetMapping("/summary")
    @Operation(summary = "Overall analytics summary",
               description = "Total incidents, open/resolved counts, critical count, and overall MTTR")
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
