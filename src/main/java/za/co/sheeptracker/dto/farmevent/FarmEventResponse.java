package za.co.sheeptracker.dto.farmevent;

import za.co.sheeptracker.entity.FarmEvent;
import za.co.sheeptracker.enums.FarmEventType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record FarmEventResponse(
        UUID id,
        UUID farmId,
        UUID sheepId,
        UUID healthIncidentId,
        UUID treatmentId,
        UUID plannedTreatmentId,
        FarmEventType eventType,
        String title,
        String description,
        OffsetDateTime occurredAt,
        UUID recordedBy,
        Map<String, Object> metadata,
        Instant createdAt
) {
    public static FarmEventResponse from(FarmEvent e) {
        return new FarmEventResponse(e.getId(), e.getFarmId(), e.getSheepId(), e.getHealthIncidentId(),
                e.getTreatmentId(), e.getPlannedTreatmentId(), e.getEventType(), e.getTitle(), e.getDescription(),
                e.getOccurredAt(), e.getRecordedBy(), e.getMetadata(), e.getCreatedAt());
    }
}
