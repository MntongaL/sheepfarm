package za.co.sheeptracker.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import za.co.sheeptracker.dto.farm.FarmCreateRequest;
import za.co.sheeptracker.dto.farm.FarmCreateResponse;
import za.co.sheeptracker.entity.Farm;
import za.co.sheeptracker.mapper.FarmMapper;
import za.co.sheeptracker.repository.FarmRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SheepHealthRecordService {

    private final FarmRepository farmRepository;
    private final FarmMapper farmMapper;

    public SheepHealthRecordService(FarmRepository farmRepository, FarmMapper farmMapper) {
        this.farmRepository = farmRepository;
        this.farmMapper = farmMapper;
    }

    public ResponseEntity<FarmCreateResponse> create(@Valid @RequestBody FarmCreateRequest farmRequest) {
        log.info("Creating farm");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(farmMapper.toResponse(farmRepository.save(farmMapper.toEntity(farmRequest))));
        } catch (RuntimeException e) {
            log.error("Failed to create farm. farmName={}", farmRequest.name(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<FarmCreateResponse>> getFarmList() {
        List<Farm> farmList = farmRepository.findAll();
        return ResponseEntity.ok(farmMapper.toResponseList(farmList));
    }

    @Transactional
    public ResponseEntity<FarmCreateResponse> get(UUID id) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found with ID: " + id
                ));

        return ResponseEntity.ok(farmMapper.toResponse(farm));
    }

}
