package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import za.co.sheeptracker.enums.FarmEventType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** One entry in the farm's chronological history. Append-only: every column is non-updatable. Maps farm_event (V18). */
@Entity
@Table(name = "farm_event")
@Getter
@Setter
@NoArgsConstructor
public class FarmEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "farm_id", nullable = false, updatable = false)
    private UUID farmId;

    @Column(name = "sheep_id", updatable = false)
    private UUID sheepId;

    @Column(name = "health_incident_id", updatable = false)
    private UUID healthIncidentId;

    @Column(name = "treatment_id", updatable = false)
    private UUID treatmentId;

    @Column(name = "planned_treatment_id", updatable = false)
    private UUID plannedTreatmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 40)
    private FarmEventType eventType;

    @Column(name = "title", nullable = false, updatable = false, length = 200)
    private String title;

    @Column(name = "description", updatable = false, columnDefinition = "text")
    private String description;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "recorded_by", updatable = false)
    private UUID recordedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, updatable = false)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static FarmEvent of(UUID farmId, FarmEventType type, String title) {
        FarmEvent e = new FarmEvent();
        e.setFarmId(farmId);
        e.setEventType(type);
        e.setTitle(title);
        return e;
    }
}
