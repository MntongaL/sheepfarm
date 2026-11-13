package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.TreatmentPlanItem;

import java.util.UUID;

public interface TreatmentPlanItemRepository extends JpaRepository<TreatmentPlanItem, UUID> {
}
