package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.Sheep;

import java.util.List;
import java.util.UUID;

public interface SheepRepository extends JpaRepository<Sheep, UUID> {
    List<Sheep> findByFarmId(UUID farmId);
}
