package za.co.sheeptracker.service;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import za.co.sheeptracker.dto.plannedsheeptreatment.PlannedSheepTreatmentRequest;
import za.co.sheeptracker.dto.plannedsheeptreatment.PlannedSheepTreatmentResponse;
import za.co.sheeptracker.entity.Medication;
import za.co.sheeptracker.entity.PlannedSheepTreatment;
import za.co.sheeptracker.entity.Sheep;
import za.co.sheeptracker.mapper.PlannedSheepTreatmentMapper;
import za.co.sheeptracker.repository.MedicationRepository;
import za.co.sheeptracker.repository.PlannedSheepTreatmentRepository;
import za.co.sheeptracker.repository.SheepRepository;
import za.co.sheeptracker.repository.TreatmentRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PlannedSheepTreatmentService {

    private final PlannedSheepTreatmentRepository repository;
    private final PlannedSheepTreatmentMapper mapper;
    private final SheepRepository sheepRepository;
    private final MedicationRepository medicationRepository;
    private final TreatmentRepository treatmentRepository;

    public PlannedSheepTreatmentService(PlannedSheepTreatmentMapper mapper, PlannedSheepTreatmentRepository repository, SheepRepository sheepRepository,
                                        MedicationRepository medicationRepository, TreatmentRepository treatmentRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.medicationRepository = medicationRepository;
        this.treatmentRepository = treatmentRepository;
        this.sheepRepository = sheepRepository;
    }

    @Transactional
    public ResponseEntity<PlannedSheepTreatmentResponse> create(@Valid @RequestBody PlannedSheepTreatmentRequest r) {
        Sheep a = sheepRepository.findById(r.sheepId()).orElseThrow(() -> new NoSuchElementException("Sheep not found"));
        if (!a.getFarmId().equals(r.farmId()))
            throw new IllegalArgumentException("Sheep does not belong to supplied farm");
        Medication m = medicationRepository.findById(r.medicationId()).orElseThrow(() -> new NoSuchElementException("Medication not found"));

        PlannedSheepTreatment plannedSheepTreatment = mapper.toEntity(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(repository.save(plannedSheepTreatment)));
    }

    @Transactional
    public ResponseEntity<PlannedSheepTreatmentResponse> get(UUID id) {
        PlannedSheepTreatment farm = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found with ID: " + id
                ));

        return ResponseEntity.ok(mapper.toResponse(farm));
    }

    @Transactional
    public ResponseEntity<List<PlannedSheepTreatmentResponse>> getPlannedSheepTreatmentByFarmId(UUID id) {
        List<PlannedSheepTreatment> farm = repository.findByFarmId(id);
        return ResponseEntity.ok(mapper.toResponseList(farm));
    }
}
