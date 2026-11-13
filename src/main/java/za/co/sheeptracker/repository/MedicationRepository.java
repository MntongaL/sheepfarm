package za.co.sheeptracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.Medication;

import java.util.*;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    List<Medication> findByActiveTrueOrderByNameAsc();

    Optional<Medication> findByNameIgnoreCase(String name);
}
