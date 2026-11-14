package za.co.sheeptracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.dto.treatment.TreatmentCreateRequest;
import za.co.sheeptracker.dto.treatment.TreatmentResponse;
import za.co.sheeptracker.entity.Treatment;
import za.co.sheeptracker.service.TreatmentService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {

        this.treatmentService = treatmentService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TreatmentResponse> create(@Valid @RequestBody TreatmentCreateRequest r) {
        return treatmentService.create(r);
    }

    @GetMapping("/farm/{id}")
    public ResponseEntity<List<TreatmentResponse>> getTreatmentListByFarmId(@RequestParam(required = false) UUID farmId) {
        return treatmentService.getFarmListByFarmId(farmId);
    }

    @GetMapping("/sheep/{id}")
    public ResponseEntity<List<TreatmentResponse>> getTreatmentListBySheepId(@RequestParam(required = false) UUID sheepId) {
        return treatmentService.getFarmListBySheepId(sheepId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentResponse> get(@PathVariable UUID id) {
        return treatmentService.get(id);
    }
}
