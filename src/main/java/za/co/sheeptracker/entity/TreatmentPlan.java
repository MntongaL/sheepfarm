package za.co.sheeptracker.entity;


import jakarta.persistence.*;
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
import za.co.sheeptracker.enums.*;

@Entity
@Table(name = "treatment_plan")
@Getter
@Setter
@NoArgsConstructor
public class TreatmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false, updatable = false)
    private Farm farm;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TreatmentPlanStatus status = TreatmentPlanStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 20)
    private PlanSelectionType selectionType = PlanSelectionType.INDIVIDUAL;

    /** Original selection criteria only; membership is always read from {@link #sheep}. */
    @Column(name = "birth_year")
    private Integer birthYear;

    /** Same representation as sheep.birth_month (varchar). */
    @Column(name = "birth_month", length = 20)
    private String birthMonth;

    // PERSIST/MERGE only. No REMOVE and no orphanRemoval: plan history and membership must never be deleted by JPA.
    @OneToMany(mappedBy = "treatmentPlan", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<TreatmentPlanSheep> sheep = new LinkedHashSet<>();

    @OneToMany(mappedBy = "treatmentPlan", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<TreatmentPlanItem> items = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addSheep(TreatmentPlanSheep membership) {
        membership.setTreatmentPlan(this);
        sheep.add(membership);
    }

    public void addItem(TreatmentPlanItem item) {
        item.setTreatmentPlan(this);
        items.add(item);
    }
}
