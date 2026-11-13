package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "sheep", uniqueConstraints = @UniqueConstraint(name = "uq_sheep_farm_tag", columnNames = {"farm_id", "tag_number"}))
@Getter
@Setter
@NoArgsConstructor
public class Sheep {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "farm_id", nullable = false)
    private UUID farmId;
    @Column(name = "tag_number", nullable = false, length = 80)
    private String tagNumber;
    @Column(name = "birth_month", length = 20)
    private String birthMonth;
    @Column(name = "birth_year")
    private Integer birthYear;
    @Column(name = "sex", length = 30)
    private String sex;
    @Column(columnDefinition = "text")
    private String notes;
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
}
