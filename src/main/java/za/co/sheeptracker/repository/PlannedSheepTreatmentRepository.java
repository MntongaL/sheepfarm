package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.PlannedSheepTreatment;

import java.util.UUID;

public interface PlannedSheepTreatmentRepository extends JpaRepository<PlannedSheepTreatment, UUID> {
}
