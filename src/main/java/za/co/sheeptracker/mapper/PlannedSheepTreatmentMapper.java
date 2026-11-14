package za.co.sheeptracker.mapper;

import org.mapstruct.*;
import za.co.sheeptracker.dto.plannedsheeptreatment.PlannedSheepTreatmentRequest;
import za.co.sheeptracker.dto.plannedsheeptreatment.PlannedSheepTreatmentResponse;
import za.co.sheeptracker.dto.treatment.TreatmentCreateRequest;
import za.co.sheeptracker.dto.treatment.TreatmentResponse;
import za.co.sheeptracker.entity.PlannedSheepTreatment;
import za.co.sheeptracker.entity.Treatment;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface PlannedSheepTreatmentMapper {
    PlannedSheepTreatmentResponse toResponse(PlannedSheepTreatment treatment);

    List<PlannedSheepTreatmentResponse> toResponseList(List<PlannedSheepTreatment> treatment);

    PlannedSheepTreatment toEntity(PlannedSheepTreatmentRequest request);

    // OffsetDateTime -> Instant
    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    // Instant -> OffsetDateTime
    default OffsetDateTime map(Instant value) {
        return value == null
                ? null
                : value.atOffset(ZoneOffset.UTC);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PlannedSheepTreatmentRequest request, @MappingTarget PlannedSheepTreatment treatment);
}
