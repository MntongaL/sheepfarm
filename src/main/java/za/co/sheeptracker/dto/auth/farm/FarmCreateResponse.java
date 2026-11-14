package za.co.sheeptracker.dto.auth.farm;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FarmCreateResponse(UUID id,
                                 String name,
                                 String regNumber,
                                 String address,
                                 String region,
                                 BigDecimal latitude,
                                 BigDecimal longitude,
                                 String contactName,
                                 String contactPhone,
                                 String contactEmail,
                                 boolean active,
                                 OffsetDateTime createdAt,
                                 OffsetDateTime updatedAt) {
}
