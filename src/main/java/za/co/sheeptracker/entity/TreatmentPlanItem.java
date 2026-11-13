package za.co.sheeptracker.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import za.co.sheeptracker.enums.PlanItemStatus;

/** A medication entry in a plan. The same medication may appear several times on different dates. */
@Entity
@Table(name = "treatment_plan_item")
@Getter
@Setter
@NoArgsConstructor
public class TreatmentPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false, updatable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_plan_id", nullable = false, updatable = false)
    private TreatmentPlan treatmentPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "dosage", nullable = false, precision = 12, scale = 3)
    private BigDecimal dosage;

    @Column(name = "dose_unit", nullable = false, length = 30)
    private String doseUnit;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlanItemStatus status = PlanItemStatus.PLANNED;

    @OneToMany(mappedBy = "treatmentPlanItem", fetch = FetchType.LAZY)
    private Set<PlannedSheepTreatment> plannedTreatments = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void syncFarm() {
        if (treatmentPlan != null) farm = treatmentPlan.getFarm();
    }
}
