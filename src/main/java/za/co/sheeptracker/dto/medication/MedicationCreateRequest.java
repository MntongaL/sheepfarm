package za.co.sheeptracker.dto.medication;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public record MedicationCreateRequest(@NotBlank String name, @NotBlank UUID farmId, @NotBlank String standardDosage,
                                      @Min(0) Integer withdrawalDays, String dosingFrequency, BigDecimal quantityPurchased,
                                      BigDecimal quantityRemaining, String quantityUnit, Short categoryId,
                                      Short administrationTypeId, String treats, String notes) {
}