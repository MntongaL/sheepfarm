package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.AdministrationType;

public interface AdministrationTypeLookupRepository extends JpaRepository<AdministrationType, Integer> {
}
