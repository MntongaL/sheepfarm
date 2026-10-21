package za.co.sheeptracker.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import za.co.sheeptracker.dto.medication.MedicationCreateRequest;
import za.co.sheeptracker.dto.medication.MedicationCreateResponse;
import za.co.sheeptracker.dto.medication.MedicationUpdateRequest;
import za.co.sheeptracker.entity.Medication;
import za.co.sheeptracker.mapper.MedicationMapper;
import za.co.sheeptracker.repository.MedicationRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationMapper medicationMapper;

    public MedicationService(MedicationRepository medicationRepository, MedicationMapper medicationMapper) {
        this.medicationRepository = medicationRepository;
        this.medicationMapper = medicationMapper;
    }

    public ResponseEntity<MedicationCreateResponse> create(@Valid @RequestBody MedicationCreateRequest request) {
        log.info("Creating farm");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(medicationMapper.toResponse(medicationRepository.save(medicationMapper.toEntity(request))));
        } catch (RuntimeException e) {
            log.error("Failed to create farm. farmName={}", request.name(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<MedicationCreateResponse>> getFarmList() {
        return ResponseEntity.ok(medicationMapper.toResponseList(medicationRepository.findAll()));
    }

    @Transactional
    public ResponseEntity<MedicationCreateResponse> get(UUID id) {
        return ResponseEntity.ok(medicationMapper.toResponse(medicationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found with ID: " + id))));
    }

    @Transactional
    public ResponseEntity<MedicationCreateResponse> updateMedication(UUID id, MedicationUpdateRequest request) {
        Medication medication = medicationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found with ID: " + id));
        if (medication == null) {
            throw (new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found with ID: " + id));
        }
        return ResponseEntity.ok(medicationMapper.toResponse(medicationRepository.save(medicationMapper.toEntity(request))));
    }

}
