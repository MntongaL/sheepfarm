package za.co.sheeptracker.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.dto.healthincident.*;
import za.co.sheeptracker.enums.HealthSeverity;
import za.co.sheeptracker.enums.IncidentStatus;
import za.co.sheeptracker.service.FarmHealthIncidentService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/health-incidents")
public class FarmHealthIncidentController {

    private final FarmHealthIncidentService service;

    public FarmHealthIncidentController(FarmHealthIncidentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FarmHealthIncidentResponse> create(@Valid @RequestBody FarmHealthIncidentRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    /** followUpBefore lists open incidents whose follow-up is due on or before that time. */
    @GetMapping
    public List<FarmHealthIncidentResponse> list(
            @RequestParam UUID farmId,
            @RequestParam(required = false) UUID sheepId,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) HealthSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime followUpBefore) {
        return service.list(farmId, sheepId, status, severity, followUpBefore);
    }

    @GetMapping("/{id}")
    public FarmHealthIncidentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public FarmHealthIncidentResponse update(@PathVariable UUID id, @Valid @RequestBody FarmHealthIncidentUpdateRequest r) {
        return service.update(id, r);
    }

    @PostMapping("/{id}/resolve")
    public FarmHealthIncidentResponse resolve(@PathVariable UUID id,
                                              @RequestBody(required = false) FarmHealthIncidentResolveRequest r) {
        return service.resolve(id, r);
    }

    @PostMapping("/{id}/follow-up")
    public FarmHealthIncidentResponse followUp(@PathVariable UUID id,
                                               @RequestBody(required = false) FarmHealthIncidentFollowUpRequest r) {
        return service.recordFollowUp(id, r);
    }
}
