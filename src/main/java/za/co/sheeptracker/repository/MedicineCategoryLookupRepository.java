package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.MedicineCategory;

public interface MedicineCategoryLookupRepository extends JpaRepository<MedicineCategory, Integer> {
}
