package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import za.co.sheeptracker.entity.FarmEvent;

import java.util.UUID;

public interface FarmEventRepository extends JpaRepository<FarmEvent, UUID>, JpaSpecificationExecutor<FarmEvent> {
}
