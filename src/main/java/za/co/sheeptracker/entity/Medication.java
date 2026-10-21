package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(nullable = false, unique = true, length = 160)
    private String name;
    @Column(length = 100)
    private String category;
    @Column(name = "administration_type", length = 100)
    private String administrationType;
    @Column(name = "standard_dosage", columnDefinition = "text")
    private String standardDosage;
    @Column(name = "withdrawal_days")
    private Integer withdrawalDays;
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
