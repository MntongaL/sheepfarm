package za.co.sheeptracker.dto.healthincident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import za.co.sheeptracker.enums.HealthSeverity;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Report an incident. Diagnosis may be left empty and filled in later. */
public record FarmHealthIncidentRequest(
        @NotNull UUID farmId,
        @NotNull UUID sheepId,
        @Size(max = 50) String incidentNumber,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "must be UPPER_SNAKE_CASE") String incidentType,
        HealthSeverity severity,
        OffsetDateTime observedAt,
        UUID assignedTo,
        String symptoms,
        String diagnosis,
        String notes,
        OffsetDateTime followUpAt
) {
}
