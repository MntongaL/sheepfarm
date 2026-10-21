package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import za.co.sheeptracker.entity.FarmHealthIncident;

import java.util.UUID;

public interface FarmHealthIncidentRepository
        extends JpaRepository<FarmHealthIncident, UUID>, JpaSpecificationExecutor<FarmHealthIncident> {
}
