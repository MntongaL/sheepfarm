package za.co.sheeptracker.dto.plannedsheeptreatment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlannedSheepTreatmentRequest(
        @NotNull UUID farmId,
        @NotNull UUID sheepId,
        @NotNull UUID medicationId,
        @NotNull @Positive BigDecimal doseAmount,
        String doseUnit,
        @NotNull OffsetDateTime administeredAt,
        Integer withdrawalDaysApplied,
        String administeredBy,
        String notes,
        UUID plannedSheepTreatmentId
) {
}