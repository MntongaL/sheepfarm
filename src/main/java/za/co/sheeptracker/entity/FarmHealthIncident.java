package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import za.co.sheeptracker.enums.HealthSeverity;
import za.co.sheeptracker.enums.IncidentStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A health problem affecting one sheep. Maps farm_health_incident (V18). */
@Entity
@Table(name = "farm_health_incident")
@Getter
@Setter
@NoArgsConstructor
public class FarmHealthIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "farm_id", nullable = false, updatable = false)
    private UUID farmId;

    @Column(name = "sheep_id", nullable = false, updatable = false)
    private UUID sheepId;

    @Column(name = "incident_number", length = 50)
    private String incidentNumber;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "incident_type", nullable = false, length = 40)
    private String incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private HealthSeverity severity = HealthSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "observed_at", nullable = false)
    private OffsetDateTime observedAt;

    @Column(name = "reported_by")
    private UUID reportedBy;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "symptoms", columnDefinition = "text")
    private String symptoms;

    /** Null until a diagnosis is known. */
    @Column(name = "diagnosis", columnDefinition = "text")
    private String diagnosis;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "follow_up_at")
    private OffsetDateTime followUpAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "outcome", columnDefinition = "text")
    private String outcome;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
