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
import za.co.sheeptracker.entity.Medication;
import za.co.sheeptracker.entity.Sheep;
import za.co.sheeptracker.entity.Treatment;
import za.co.sheeptracker.mapper.TreatmentMapper;
import za.co.sheeptracker.repository.MedicationRepository;
import za.co.sheeptracker.repository.SheepRepository;
import za.co.sheeptracker.repository.TreatmentRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper treatmentMapper;
    private final SheepRepository sheep;
    private final MedicationRepository medications;

    public TreatmentService(TreatmentRepository treatmentRepository, TreatmentMapper treatmentMapper, SheepRepository sheepRepository, MedicationRepository medications) {
        this.treatmentRepository = treatmentRepository;
        this.treatmentMapper = treatmentMapper;
        this.medications = medications;
        this.sheep = sheepRepository;
    }

    @Transactional
    public ResponseEntity<TreatmentResponse> create(@Valid @RequestBody TreatmentCreateRequest request) {
        Sheep a = sheep.findById(request.sheepId()).orElseThrow(() -> new NoSuchElementException("Sheep not found"));
        if (!a.getFarmId().equals(request.farmId()))
            throw new IllegalArgumentException("Sheep does not belong to supplied farm");
        Medication m = medications.findById(request.medicationId()).orElseThrow(() -> new NoSuchElementException("Medication not found"));
        Treatment t = this.treatmentMapper.toEntity(request);
        Integer wd = m.getWithdrawalDays();
        if (wd != null && wd > 0) {
            t.setWithdrawalDaysApplied(wd);
            t.setWithdrawalUntil(request.administeredAt().plusDays(wd));
            return ResponseEntity.status(HttpStatus.CREATED).body(treatmentMapper.toResponse(treatmentRepository.save(t)));
        }
        log.info("Creating treatment");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(treatmentMapper.toResponse(treatmentRepository.save(treatmentMapper.toEntity(request))));
        } catch (RuntimeException e) {
            log.error("Failed to create treatment. administeredBy={}", request.administeredBy(), e);
            throw e;
        }
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
                        "Farm not found with ID: " + id
                ));

        return ResponseEntity.ok(treatmentMapper.toResponse(treatment));
    }

}
