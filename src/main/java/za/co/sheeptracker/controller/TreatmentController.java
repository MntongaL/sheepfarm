package za.co.sheeptracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.entity.Medication;
import za.co.sheeptracker.repository.MedicationRepository;
import za.co.sheeptracker.entity.Sheep;
import za.co.sheeptracker.repository.SheepRepository;
import za.co.sheeptracker.entity.Treatment;
import za.co.sheeptracker.repository.TreatmentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/treatments")
public class TreatmentController {
    private final TreatmentRepository treatments;
    private final SheepRepository sheep;
    private final MedicationRepository medications;

    public TreatmentController(TreatmentRepository t, SheepRepository s, MedicationRepository m) {
        treatments = t;
        sheep = s;
        medications = m;
    }

    public record TreatmentRequest(@NotNull UUID farmId, @NotNull UUID sheepId, @NotNull UUID medicationId,
                                   @NotNull LocalDate administeredAt,
                                   @NotNull @DecimalMin(value = "0.001") BigDecimal doseAmount,
                                   @NotBlank String doseUnit, String administeredBy, String notes) {
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Treatment> create(@Valid @RequestBody TreatmentRequest r) {
        Sheep a = sheep.findById(r.sheepId()).orElseThrow(() -> new NoSuchElementException("Sheep not found"));
        if (!a.getFarmId().equals(r.farmId()))
            throw new IllegalArgumentException("Sheep does not belong to supplied farm");
        Medication m = medications.findById(r.medicationId()).orElseThrow(() -> new NoSuchElementException("Medication not found"));
        Treatment t = new Treatment();
        t.setFarmId(r.farmId());
        t.setSheepId(r.sheepId());
        t.setMedicationId(r.medicationId());
        t.setAdministeredAt(r.administeredAt());
        t.setDoseAmount(r.doseAmount());
        t.setDoseUnit(r.doseUnit().trim());
        t.setAdministeredBy(r.administeredBy());
        t.setNotes(r.notes());
        Integer wd = m.getWithdrawalDays();
        if (wd != null && wd > 0) {
            t.setWithdrawalDaysApplied(wd);
            t.setWithdrawalUntil(r.administeredAt().plusDays(wd));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(treatments.save(t));
    }

    @GetMapping
    public List<Treatment> list(@RequestParam(required = false) UUID farmId, @RequestParam(required = false) UUID sheepId, @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) {
        if (sheepId != null) return treatments.findBySheepIdOrderByAdministeredAtDesc(sheepId);
        if (farmId != null && from != null && to != null)
            return treatments.findByFarmIdAndAdministeredAtBetweenOrderByAdministeredAtDesc(farmId, from, to);
        return treatments.findAll();
    }

    @GetMapping("/{id}")
    public Treatment get(@PathVariable UUID id) {
        return treatments.findById(id).orElseThrow(() -> new NoSuchElementException("Treatment not found: " + id));
    }
}
