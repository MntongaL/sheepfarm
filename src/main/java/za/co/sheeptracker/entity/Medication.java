package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "farm_id")
    private UUID farmId;
    @Column(nullable = false, unique = true, length = 160)
    private String name;
    @Column(name = "standard_dosage", columnDefinition = "text")
    private String standardDosage;
    @Column(name = "withdrawal_days")
    private Integer withdrawalDays;
    @Column(name = "quantity_purchased")
    private BigDecimal quantityPurchased;
    @Column(name = "quantity_remaining")
    private BigDecimal quantityRemaining;
    @Column(name = "quantity_unit")
    private String quantityUnit;
    @Column(name = "category_id")
    private Short categoryId;
    @Column(name = "administration_type_id")
    private Short administrationTypeId;
    @Column(name = "dosing_frequency", columnDefinition = "text")
    private String dosingFrequency;
    @Column(columnDefinition = "text")
    private String treats;
    @Column(columnDefinition = "text")
    private String notes;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
}
