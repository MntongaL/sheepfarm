package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.TreatmentPlanSheep;

import java.util.UUID;

public interface TreatmentPlanSheepRepository extends JpaRepository<TreatmentPlanSheep, UUID> {
}
