package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "farm")
@Getter
@Setter
@NoArgsConstructor
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "registration_number", nullable = false, length = 80)
    private String regNumber;
    @Column(name = "address", length = 225)
    private String address;
    @Column(name = "region")
    private String region;
    @Column(name = "latitude", length = 30)
    private BigDecimal latitude;
    @Column(name = "longitude", length = 30)
    private BigDecimal longitude;
    @Column(name = "contact_name")
    private String contactName;
    @Column(name = "contact_phone")
    private String contactPhone;
    @Column(name = "contact_email")
    private String contactEmail;
    @Column(name = "is_active")
    private Boolean active;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
}
