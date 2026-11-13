package za.co.sheeptracker.service;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import za.co.sheeptracker.controller.FarmController;
import za.co.sheeptracker.entity.Farm;
import za.co.sheeptracker.repository.FarmRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class FarmService {

    private final FarmRepository farmRepository;

    public FarmService(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    public ResponseEntity<Farm> create(@Valid @RequestBody FarmController.FarmRequest farmRequest) {
        Farm farm = new Farm();
        farm.setActive(farmRequest.active());
        farm.setCreatedAt(farmRequest.createdAt());
        farm.setUpdatedAt(farmRequest.updatedAt());
        farm.setRegNumber(farmRequest.regNumber());
        farm.setRegion(farmRequest.region());
        farm.setContactPhone(farmRequest.contactPhone());
        farm.setContactName(farmRequest.contactName());
        farm.setContactEmail(farmRequest.contactEmail());
        farm.setAddress(farmRequest.address());
        farm.setLatitude(farmRequest.latitude());
        farm.setLongitude(farmRequest.longitude());
        farm.setName(farmRequest.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(farmRepository.save(farm));
    }

    @Transactional
    public ResponseEntity<List<Farm>> getFarmList() {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmRepository.findAll());
    }

    @Transactional
    public ResponseEntity<Farm> get(UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmRepository.getReferenceById(id));
    }

}
