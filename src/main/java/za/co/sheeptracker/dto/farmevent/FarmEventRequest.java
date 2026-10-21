package za.co.sheeptracker.dto.farmevent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import za.co.sheeptracker.enums.FarmEventType;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Manually added event. Only HEALTH_OBSERVED, SHEEP_MOVED, VACCINATION, WEIGHT_RECORDED and GENERAL_NOTE are
 * accepted; incident and treatment events are written by their own flows.
 */
public record FarmEventRequest(
        @NotNull UUID farmId,
        UUID sheepId,
        UUID healthIncidentId,
        @NotNull FarmEventType eventType,
        @NotBlank @Size(max = 200) String title,
        String description,
        OffsetDateTime occurredAt,
        Map<String, Object> metadata
) {
}
