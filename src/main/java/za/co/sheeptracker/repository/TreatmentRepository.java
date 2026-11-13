package za.co.sheeptracker.repository; import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sheeptracker.entity.Treatment;

import java.util.*; public interface TreatmentRepository extends JpaRepository<Treatment,UUID> { List<Treatment> findBySheepIdOrderByAdministeredAtDesc(UUID sheepId); List<Treatment> findByFarmIdAndAdministeredAtBetweenOrderByAdministeredAtDesc(UUID farmId, java.time.LocalDate from, java.time.LocalDate to); }
