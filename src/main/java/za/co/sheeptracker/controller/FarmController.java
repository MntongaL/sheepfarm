package za.co.sheeptracker.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.dto.farm.FarmCreateRequest;
import za.co.sheeptracker.dto.farm.FarmCreateResponse;
import za.co.sheeptracker.service.FarmService;

import java.util.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/farm")
public class FarmController {
    private final FarmService repo;

    public FarmController(FarmService repo) {
        this.repo = repo;
    }


    @PostMapping
    public ResponseEntity<FarmCreateResponse> create(@Valid @RequestBody FarmCreateRequest r) {
        return repo.create(r);
    }

    @GetMapping
    public ResponseEntity<List<FarmCreateResponse>> list() {
        return repo.getFarmList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmCreateResponse> get(@PathVariable UUID id) {
        return repo.get(id);
    }
}
