package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.TreatmentPlan;

import java.util.UUID;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, UUID> {

}
