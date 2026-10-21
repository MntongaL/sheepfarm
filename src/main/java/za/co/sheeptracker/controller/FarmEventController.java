package za.co.sheeptracker.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.dto.farmevent.FarmEventRequest;
import za.co.sheeptracker.dto.farmevent.FarmEventResponse;
import za.co.sheeptracker.enums.FarmEventType;
import za.co.sheeptracker.service.FarmEventService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/farm-events")
public class FarmEventController {

    private final FarmEventService service;

    public FarmEventController(FarmEventService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FarmEventResponse> create(@Valid @RequestBody FarmEventRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    /** Newest first, at most 500 per call. */
    @GetMapping
    public List<FarmEventResponse> list(
            @RequestParam UUID farmId,
            @RequestParam(required = false) UUID sheepId,
            @RequestParam(required = false) UUID healthIncidentId,
            @RequestParam(required = false) UUID treatmentId,
            @RequestParam(required = false) FarmEventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "100") int limit) {
        return service.list(farmId, sheepId, healthIncidentId, treatmentId, eventType, from, to, limit);
    }
}
