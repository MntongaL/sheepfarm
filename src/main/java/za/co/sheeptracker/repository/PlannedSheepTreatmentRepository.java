package za.co.sheeptracker.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.sheeptracker.entity.PlannedSheepTreatment;
import za.co.sheeptracker.entity.Sheep;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlannedSheepTreatmentRepository extends JpaRepository<PlannedSheepTreatment, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select p
                from PlannedSheepTreatment p
                where p.id = :id
            """)
    Optional<PlannedSheepTreatment> findByIdForUpdate(
            @Param("id") UUID id
    );

    List<PlannedSheepTreatment> findByFarmId(UUID farmId);
}
