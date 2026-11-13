package za.co.sheeptracker.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.entity.Sheep;
import za.co.sheeptracker.repository.SheepRepository;

import java.util.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/sheep")
public class SheepController {
    private final SheepRepository repo;

    public SheepController(SheepRepository repo) {
        this.repo = repo;
    }

    public record SheepRequest(@NotNull UUID farmId, @NotBlank String tagNumber, String birthMonth, Integer birthYear,
                               String sex, String notes) {
    }

    @PostMapping
    public ResponseEntity<Sheep> create(@Valid @RequestBody SheepRequest r) {
        Sheep s = new Sheep();
        s.setFarmId(r.farmId());
        s.setTagNumber(r.tagNumber().trim());
        s.setBirthMonth(r.birthMonth());
        s.setBirthYear(r.birthYear());
        s.setSex(r.sex());
        s.setNotes(r.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(s));
    }

    @GetMapping
    public List<Sheep> list(@RequestParam(required = false) UUID farmId) {
        return farmId == null ? repo.findAll() : repo.findByFarmId(farmId);
    }

    @GetMapping("/{id}")
    public Sheep get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Sheep not found: " + id));
    }

    @PatchMapping("/{id}/status")
    public Sheep status(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Sheep s = get(id);
        String status = body.get("status");
        if (status == null || !Set.of("ACTIVE", "DECEASED", "SOLD", "TRANSFERRED", "INACTIVE").contains(status.toUpperCase()))
            throw new IllegalArgumentException("status must be ACTIVE, DECEASED, SOLD, TRANSFERRED, or INACTIVE");
        s.setStatus(status.toUpperCase());
        return repo.save(s);
    }
}
