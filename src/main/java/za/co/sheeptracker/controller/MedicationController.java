package za.co.sheeptracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.entity.Medication;
import za.co.sheeptracker.repository.MedicationRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {
    private final MedicationRepository repo;

    public MedicationController(MedicationRepository repo) {
        this.repo = repo;
    }

    public record MedicationRequest(@NotBlank String name, String category, String administrationType,
                                    String standardDosage, @Min(0) Integer withdrawalDays, String dosingFrequency,
                                    String treats, String notes) {
    }

    @PostMapping
    public ResponseEntity<Medication> create(@Valid @RequestBody MedicationRequest r) {
        if (repo.findByNameIgnoreCase(r.name()).isPresent())
            throw new IllegalArgumentException("Medication name already exists");
        Medication m = map(r, new Medication());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(m));
    }

    @GetMapping
    public List<Medication> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return includeInactive ? repo.findAll() : repo.findByActiveTrueOrderByNameAsc();
    }

    @GetMapping("/{id}")
    public Medication get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Medication not found: " + id));
    }

    @PutMapping("/{id}")
    public Medication update(@PathVariable UUID id, @Valid @RequestBody MedicationRequest r) {
        Medication m = get(id);
        if (repo.findByNameIgnoreCase(r.name()).filter(x -> !x.getId().equals(id)).isPresent())
            throw new IllegalArgumentException("Medication name already exists");
        return repo.save(map(r, m));
    }

    @PatchMapping("/{id}/status")
    public Medication status(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        Medication m = get(id);
        if (body.get("active") == null) throw new IllegalArgumentException("active is required");
        m.setActive(body.get("active"));
        return repo.save(m);
    }

    private Medication map(MedicationRequest r, Medication m) {
        m.setName(r.name().trim());
        m.setCategory(r.category());
        m.setAdministrationType(r.administrationType());
        m.setStandardDosage(r.standardDosage());
        m.setWithdrawalDays(r.withdrawalDays());
        m.setDosingFrequency(r.dosingFrequency());
        m.setTreats(r.treats());
        m.setNotes(r.notes());
        return m;
    }
}
