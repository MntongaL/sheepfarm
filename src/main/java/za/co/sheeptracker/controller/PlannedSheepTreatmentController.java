package za.co.sheeptracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.dto.plannedsheeptreatment.PlannedSheepTreatmentRequest;
import za.co.sheeptracker.dto.plannedsheeptreatment.PlannedSheepTreatmentResponse;
import za.co.sheeptracker.entity.Treatment;
import za.co.sheeptracker.service.PlannedSheepTreatmentService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planned-sheep-treatment")
public class PlannedSheepTreatmentController {

    private final PlannedSheepTreatmentService service;

    public PlannedSheepTreatmentController(PlannedSheepTreatmentService service) {
        this.service = service;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PlannedSheepTreatmentResponse> create(@Valid @RequestBody PlannedSheepTreatmentRequest r) {
        return service.create(r);
    }

    @GetMapping
    public ResponseEntity<List<PlannedSheepTreatmentResponse>> list(@RequestParam(required = false) UUID farmId) {
        return service.getPlannedSheepTreatmentByFarmId(farmId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlannedSheepTreatmentResponse> get(@PathVariable UUID id) {
        return service.get(id);
    }
}
