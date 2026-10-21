package za.co.sheeptracker.dto.medication;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public record MedicationUpdateRequest(UUID id, @NotBlank String name, UUID farmId, String standardDosage,
                                      @Min(0) Integer withdrawalDays, String dosingFrequency, BigDecimal quantityPurchased,
                                      BigDecimal quantityRemaining, String quantityUnit, Short categoryId,
                                      Short administrationTypeId, String treats, String notes, boolean active) {
}