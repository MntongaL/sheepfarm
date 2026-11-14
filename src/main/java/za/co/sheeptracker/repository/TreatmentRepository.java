package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.Treatment;

import java.util.List;
import java.util.UUID;

public interface TreatmentRepository extends JpaRepository<Treatment, UUID> {
    List<Treatment> findBySheepId(UUID sheepId);
    List<Treatment> findByFarmId(UUID farmId);

    List<Treatment> findByFarmIdAndAdministeredAtBetweenOrderByAdministeredAtDesc(UUID farmId, java.time.LocalDate from, java.time.LocalDate to);

}
