package za.co.sheeptracker.dto.healthincident;

import za.co.sheeptracker.entity.FarmHealthIncident;
import za.co.sheeptracker.enums.HealthSeverity;
import za.co.sheeptracker.enums.IncidentStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FarmHealthIncidentResponse(
        UUID id,
        UUID farmId,
        UUID sheepId,
        String incidentNumber,
        String title,
        String incidentType,
        HealthSeverity severity,
        IncidentStatus status,
        OffsetDateTime observedAt,
        UUID reportedBy,
        UUID assignedTo,
        String symptoms,
        String diagnosis,
        String notes,
        OffsetDateTime followUpAt,
        OffsetDateTime resolvedAt,
        UUID resolvedBy,
        String outcome,
        Instant createdAt,
        Instant updatedAt
) {
    public static FarmHealthIncidentResponse from(FarmHealthIncident i) {
        return new FarmHealthIncidentResponse(i.getId(), i.getFarmId(), i.getSheepId(), i.getIncidentNumber(),
                i.getTitle(), i.getIncidentType(), i.getSeverity(), i.getStatus(), i.getObservedAt(),
                i.getReportedBy(), i.getAssignedTo(), i.getSymptoms(), i.getDiagnosis(), i.getNotes(),
                i.getFollowUpAt(), i.getResolvedAt(), i.getResolvedBy(), i.getOutcome(),
                i.getCreatedAt(), i.getUpdatedAt());
    }
}
