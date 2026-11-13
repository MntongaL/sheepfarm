package za.co.sheeptracker.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.poi.hpsf.Decimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.entity.Farm;
import za.co.sheeptracker.service.FarmService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/farm")
public class FarmController {
    private final FarmService repo;

    public FarmController(FarmService repo) {
        this.repo = repo;
    }

    public record FarmRequest(@NotBlank OffsetDateTime createdAt, String address, String region,
                              BigDecimal longitude, BigDecimal latitude, String name, String contactEmail, String contactPhone,
                              String contactName, String regNumber, OffsetDateTime updatedAt,
                              Boolean active) {
    }

    @PostMapping
    public ResponseEntity<Farm> create(@Valid @RequestBody FarmRequest r) {
        return repo.create(r);
    }

    @GetMapping
    public ResponseEntity<List<Farm>> list() {
        return repo.getFarmList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Farm> get(@PathVariable UUID id) {
        return repo.get(id);
    }
}
