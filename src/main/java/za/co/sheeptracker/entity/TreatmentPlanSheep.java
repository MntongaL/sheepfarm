package za.co.sheeptracker.entity;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.sheeptracker.enums.PlanSheepStatus;

/** Explicit association entity: which sheep are in which plan (plus per-sheep progress). */
@Entity
@Table(name = "treatment_plan_sheep",
        uniqueConstraints = @UniqueConstraint(name = "uq_tps_plan_sheep", columnNames = {"treatment_plan_id", "sheep_id"}))
@Getter
@Setter
@NoArgsConstructor
public class TreatmentPlanSheep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Copy of the plan's farm. The database composite keys guarantee plan and sheep share it. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false, updatable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_plan_id", nullable = false, updatable = false)
    private TreatmentPlan treatmentPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sheep_id", nullable = false, updatable = false)
    private Sheep sheep;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlanSheepStatus status = PlanSheepStatus.PENDING;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    // No cascade: planned administrations are managed explicitly and never deleted through membership changes.
    @OneToMany(mappedBy = "treatmentPlanSheep", fetch = FetchType.LAZY)
    private Set<PlannedSheepTreatment> plannedTreatments = new LinkedHashSet<>();

    @PrePersist
    void onCreate() {
        if (addedAt == null) addedAt = Instant.now();
        syncAndCheckFarm();
    }

    @PreUpdate
    void onUpdate() {
        syncAndCheckFarm();
    }

    private void syncAndCheckFarm() {
        if (treatmentPlan != null) farm = treatmentPlan.getFarm();
        if (farm != null && sheep != null && sheep.getFarmId() != null
                && !Objects.equals(sheep.getFarmId(), farm.getId())) {
            throw new IllegalStateException("Sheep belongs to a different farm than the treatment plan");
        }
    }
}
