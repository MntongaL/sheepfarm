package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "treatments")
public class Treatment {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Setter
    @Getter
    @Column(name = "farm_id", nullable = false)
    private UUID farmId;
    @Setter
    @Getter
    @Column(name = "sheep_id", nullable = false)
    private UUID sheepId;
    @Setter
    @Getter
    @Column(name = "medication_id", nullable = false)
    private UUID medicationId;
    @Setter
    @Getter
    @Column(name = "administered_at", nullable = false)
    private OffsetDateTime administeredAt;
    @Setter
    @Getter
    @Column(name = "dose_amount", precision = 12, scale = 3)
    private BigDecimal doseAmount;
    @Setter
    @Getter
    @Column(name = "dose_unit", length = 30)
    private String doseUnit;
    @Setter
    @Getter
    @Column(name = "withdrawal_days_applied")
    private Integer withdrawalDaysApplied;
    @Setter
    @Getter
    @Column(name = "withdrawal_until")
    private OffsetDateTime withdrawalUntil;
    @Setter
    @Getter
    @Column(name = "administered_by", length = 160)
    private UUID administeredBy;
    @Setter
    @Getter
    @Column(columnDefinition = "text")
    private String notes;
    @Setter
    @Getter
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

}
