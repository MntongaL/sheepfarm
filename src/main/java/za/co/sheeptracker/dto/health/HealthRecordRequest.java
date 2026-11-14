package za.co.sheeptracker.dto.health;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public record HealthRecordRequest(OffsetDateTime createdAt, @NotBlank String address, String region,
                                  BigDecimal longitude, BigDecimal latitude, @NotBlank String name, String contactEmail, @NotBlank String contactPhone,
                                  @NotBlank String contactName, String regNumber, OffsetDateTime updatedAt,
                                  Boolean active) {
}