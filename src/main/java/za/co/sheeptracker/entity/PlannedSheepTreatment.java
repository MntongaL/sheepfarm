package za.co.sheeptracker.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import za.co.sheeptracker.enums.PlannedTreatmentStatus;

/** One planned administration of one plan item for one sheep. Separate from the actual {@link Treatment} history. */
@Entity
@Table(name = "planned_sheep_treatment")
@Getter
@Setter
@NoArgsConstructor
public class PlannedSheepTreatment {

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
    @JoinColumn(name = "treatment_plan_sheep_id", nullable = false, updatable = false)
    private TreatmentPlanSheep treatmentPlanSheep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_plan_item_id", nullable = false, updatable = false)
    private TreatmentPlanItem treatmentPlanItem;

    /** Null means "use the plan item's date". */
    @Column(name = "planned_date")
    private LocalDate plannedDate;

    /** Null means "use the plan item's dosage". */
    @Column(name = "dosage", precision = 12, scale = 3)
    private BigDecimal dosage;

    @Column(name = "dose_unit", length = 30)
    private String doseUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlannedTreatmentStatus status = PlannedTreatmentStatus.PLANNED;

    /** Link to the real treatment record. No cascade: JPA never creates or deletes treatment history here. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_treatment_id")
    private Treatment completedTreatment;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** User who completed it. Only allowed while status = COMPLETED (ck_pst_completed_by). */
    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void syncParents() {
        if (treatmentPlanSheep != null) {
            treatmentPlan = treatmentPlanSheep.getTreatmentPlan();
            farm = treatmentPlanSheep.getFarm();
        }
    }

    public LocalDate effectiveDate() {
        return plannedDate != null ? plannedDate : treatmentPlanItem.getPlannedDate();
    }

    public BigDecimal effectiveDosage() {
        return dosage != null ? dosage : treatmentPlanItem.getDosage();
    }
}
