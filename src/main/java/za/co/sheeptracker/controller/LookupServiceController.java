package za.co.sheeptracker.controller;

import org.springframework.web.bind.annotation.*;
import za.co.sheeptracker.entity.AdministrationType;
import za.co.sheeptracker.entity.MedicineCategory;
import za.co.sheeptracker.service.LookupService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medication")
public class LookupServiceController {
    private final LookupService lookupService;

    public LookupServiceController(LookupService t) {
        lookupService = t;
    }

    @GetMapping("/administration-types")
    public List<AdministrationType> retrieveAdministrationTypes() {
        return lookupService.retrieveAdministrationTypes();
    }

    @GetMapping("/categories")
    public List<MedicineCategory> retrieveCategories() {
        return lookupService.retrieveMedicineCategories();
    }
}
