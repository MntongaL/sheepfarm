package za.co.sheeptracker.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import za.co.sheeptracker.dto.treatment.TreatmentCreateRequest;
import za.co.sheeptracker.dto.treatment.TreatmentResponse;
import za.co.sheeptracker.entity.*;
import za.co.sheeptracker.enums.FarmEventType;
import za.co.sheeptracker.enums.PlannedTreatmentStatus;
import za.co.sheeptracker.mapper.TreatmentMapper;
import za.co.sheeptracker.repository.MedicationRepository;
import za.co.sheeptracker.repository.PlannedSheepTreatmentRepository;
import za.co.sheeptracker.repository.SheepRepository;
import za.co.sheeptracker.repository.TreatmentRepository;
import za.co.sheeptracker.security.CurrentUser;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper treatmentMapper;
    private final SheepRepository sheep;
    private final MedicationRepository medications;
    private final PlannedSheepTreatmentRepository plannedRepository;
    private final FarmEventService events;

    public TreatmentService(TreatmentRepository treatmentRepository, TreatmentMapper treatmentMapper,
                            SheepRepository sheepRepository, MedicationRepository medications,
                            PlannedSheepTreatmentRepository plannedRepository, FarmEventService events) {
        this.treatmentRepository = treatmentRepository;
        this.treatmentMapper = treatmentMapper;
        this.medications = medications;
        this.sheep = sheepRepository;
        this.plannedRepository = plannedRepository;
        this.events = events;
    }

    /**
     * Records an actual treatment. recorded_by is always the authenticated user. When plannedSheepTreatmentId is
     * given, that planned administration is completed and linked to this treatment, and a TREATMENT_COMPLETED event
     * is written. A TREATMENT_ADMINISTERED event is written for every treatment.
     */
    @Transactional
    public ResponseEntity<TreatmentResponse> create(@Valid @RequestBody TreatmentCreateRequest request) {
        Sheep a = sheep.findById(request.sheepId()).orElseThrow(() -> new NoSuchElementException("Sheep not found"));
        if (!a.getFarmId().equals(request.farmId()))
            throw new IllegalArgumentException("Sheep does not belong to supplied farm");
        Medication m = medications.findById(request.medicationId()).orElseThrow(() -> new NoSuchElementException("Medication not found"));

        PlannedSheepTreatment planned = null;
        if (request.plannedSheepTreatmentId() != null) {
            planned = plannedRepository.findById(request.plannedSheepTreatmentId())
                    .orElseThrow(() -> new NoSuchElementException("Planned treatment not found"));
            requirePlannedMatches(planned, request);
        }

        UUID userId = CurrentUser.idOrNull();
        Treatment t = treatmentMapper.toEntity(request);
        t.setFarmId(request.farmId());
        t.setSheepId(request.sheepId());
        t.setMedicationId(request.medicationId());
        t.setRecordedBy(userId);
        Integer wd = m.getWithdrawalDays();
        if (wd != null && wd > 0) {
            t.setWithdrawalDaysApplied(wd);
            t.setWithdrawalUntil(request.administeredAt().plusDays(wd));
        }
        log.info("Creating treatment");
        Treatment saved;
        try {
            saved = treatmentRepository.save(t);
        } catch (RuntimeException e) {
            log.error("Failed to create treatment. administeredBy={}", request.administeredBy(), e);
            throw e;
        }

        FarmEvent administered = FarmEvent.of(saved.getFarmId(), FarmEventType.TREATMENT_ADMINISTERED,
                "Treatment administered: " + m.getName());
        administered.setSheepId(saved.getSheepId());
        administered.setTreatmentId(saved.getId());
        administered.setPlannedTreatmentId(planned == null ? null : planned.getId());
        administered.setOccurredAt(saved.getAdministeredAt());
        administered.setDescription(saved.getNotes());
        administered.setMetadata(treatmentMeta(m, saved));
        events.record(administered);

        if (planned != null) {
            planned.setStatus(PlannedTreatmentStatus.COMPLETED);
            planned.setCompletedTreatment(saved);
            planned.setCompletedAt(Instant.now());
            planned.setCompletedBy(userId);
            plannedRepository.save(planned);

            FarmEvent completed = FarmEvent.of(saved.getFarmId(), FarmEventType.TREATMENT_COMPLETED,
                    "Planned treatment completed: " + m.getName());
            completed.setSheepId(saved.getSheepId());
            completed.setTreatmentId(saved.getId());
            completed.setPlannedTreatmentId(planned.getId());
            completed.setOccurredAt(saved.getAdministeredAt());
            completed.setMetadata(treatmentMeta(m, saved));
            events.record(completed);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(treatmentMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<TreatmentResponse>> getFarmListBySheepId(UUID sheepId) {
        return ResponseEntity.ok(treatmentMapper.toResponseList(treatmentRepository.findBySheepId(sheepId)));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<TreatmentResponse>> getFarmListByFarmId(UUID farmId) {
        return ResponseEntity.ok(treatmentMapper.toResponseList(treatmentRepository.findByFarmId(farmId)));
    }

    @Transactional
    public ResponseEntity<TreatmentResponse> get(UUID id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Treatment not found with ID: " + id
                ));

        return ResponseEntity.ok(treatmentMapper.toResponse(treatment));
    }

    /** Friendly errors before the database trigger/constraints would reject the link with a 409. */
    private static void requirePlannedMatches(PlannedSheepTreatment p, TreatmentCreateRequest r) {
        if (!p.getFarm().getId().equals(r.farmId()))
            throw new IllegalArgumentException("Planned treatment does not belong to supplied farm");
        if (p.getStatus() != PlannedTreatmentStatus.PLANNED)
            throw new IllegalArgumentException("Planned treatment is already " + p.getStatus());
        if (!p.getTreatmentPlanSheep().getSheep().getId().equals(r.sheepId()))
            throw new IllegalArgumentException("Planned treatment is for a different sheep");
        if (!p.getTreatmentPlanItem().getMedication().getId().equals(r.medicationId()))
            throw new IllegalArgumentException("Planned treatment is for a different medication");
    }

    private static Map<String, Object> treatmentMeta(Medication m, Treatment t) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("medicationId", m.getId().toString());
        meta.put("medicationName", m.getName());
        if (t.getDoseAmount() != null) meta.put("doseAmount", t.getDoseAmount().toPlainString());
        if (t.getDoseUnit() != null) meta.put("doseUnit", t.getDoseUnit());
        if (t.getWithdrawalUntil() != null) meta.put("withdrawalUntil", t.getWithdrawalUntil().toString());
        return meta;
    }
}
