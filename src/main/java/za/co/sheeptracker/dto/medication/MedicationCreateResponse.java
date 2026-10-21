package za.co.sheeptracker.dto.medication;

import jakarta.validation.constraints.Min;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicationCreateResponse(UUID id, String name, UUID farmId,
                                       String standardDosage, Integer withdrawalDays, String dosingFrequency,
                                       BigDecimal quantityPurchased, BigDecimal quantityRemaining, String quantityUnit,
                                       Short categoryId, Short administrationTypeId, String treats, String notes, boolean active, OffsetDateTime createdAt) {
}