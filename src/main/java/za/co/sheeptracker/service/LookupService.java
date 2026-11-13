package za.co.sheeptracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.sheeptracker.entity.AdministrationType;
import za.co.sheeptracker.entity.MedicineCategory;
import za.co.sheeptracker.repository.AdministrationTypeLookupRepository;
import za.co.sheeptracker.repository.MedicineCategoryLookupRepository;

import java.util.List;

@Service
public class LookupService {

    private final AdministrationTypeLookupRepository administrationTypeLookupRepository;
    private final MedicineCategoryLookupRepository medicineCategoryLookupRepository;

    public LookupService(AdministrationTypeLookupRepository administrationTypeLookupRepository, MedicineCategoryLookupRepository medicineCategoryLookupRepository) {
        this.administrationTypeLookupRepository = administrationTypeLookupRepository;
        this.medicineCategoryLookupRepository = medicineCategoryLookupRepository;
    }

    @Transactional
    public List<AdministrationType> retrieveAdministrationTypes() {
        return administrationTypeLookupRepository.findAll();
    }

    @Transactional
    public List<MedicineCategory> retrieveMedicineCategories() {
        return medicineCategoryLookupRepository.findAll();
    }
}
