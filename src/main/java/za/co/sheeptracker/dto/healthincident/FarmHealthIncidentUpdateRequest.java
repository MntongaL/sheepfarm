package za.co.sheeptracker.dto.healthincident;

import jakarta.validation.constraints.Size;
import za.co.sheeptracker.enums.HealthSeverity;
import za.co.sheeptracker.enums.IncidentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Partial update: null means "leave unchanged". Use the resolve endpoint to resolve an incident. */
public record FarmHealthIncidentUpdateRequest(
        @Size(max = 200) String title,
        HealthSeverity severity,
        IncidentStatus status,
        UUID assignedTo,
        String symptoms,
        String diagnosis,
        String notes,
        OffsetDateTime followUpAt
) {
}
