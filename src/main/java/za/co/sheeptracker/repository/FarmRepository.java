package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.Farm;

import java.util.UUID;

public interface FarmRepository extends JpaRepository<Farm, UUID> {
}
