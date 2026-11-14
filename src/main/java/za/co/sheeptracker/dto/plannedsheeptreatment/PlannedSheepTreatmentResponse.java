package za.co.sheeptracker.dto.plannedsheeptreatment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlannedSheepTreatmentResponse(
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