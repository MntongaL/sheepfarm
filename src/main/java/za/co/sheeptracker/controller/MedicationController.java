package za.co.sheeptracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.dto.medication.MedicationCreateRequest;
import za.co.sheeptracker.dto.medication.MedicationCreateResponse;
import za.co.sheeptracker.dto.medication.MedicationUpdateRequest;
import za.co.sheeptracker.mapper.MedicationMapper;
import za.co.sheeptracker.service.MedicationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {
    private final MedicationService repo;

    public MedicationController(MedicationService repo, MedicationMapper medicationMapper) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<MedicationCreateResponse> create(@Valid @RequestBody MedicationCreateRequest r) {
        return repo.create(r);
    }

    @GetMapping
    public ResponseEntity<List<MedicationCreateResponse>> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return repo.getFarmList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationCreateResponse> get(@PathVariable UUID id) {
        return repo.get(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationCreateResponse> update(@PathVariable UUID id, @Valid @RequestBody MedicationUpdateRequest r) {
        return repo.updateMedication(id, r);
    }

//    @PatchMapping("/{id}/status")
//    public ResponseEntity<MedicationCreateResponse> status(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
//        MedicationCreateResponse  response = get(id).getBody();
//
//        Medication m =  medicationMapper.toEntity(get(id).getBody());
//        if (body.get("active") == null) throw new IllegalArgumentException("active is required");
//        m.setActive(body.get("active"));
//        return repo.save(m);
//    }
}
