package za.co.sheeptracker.dto.treatment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TreatmentResponse(
        UUID id,
        UUID farmId,
        UUID sheepId,
        UUID medicationId,
        BigDecimal doseAmount,
        String doseUnit,
        OffsetDateTime administeredAt,
        Integer withdrawalDaysApplied,
        OffsetDateTime withdrawalUntil,
        String administeredBy,
        String notes,
        Instant createdAt
) {
}