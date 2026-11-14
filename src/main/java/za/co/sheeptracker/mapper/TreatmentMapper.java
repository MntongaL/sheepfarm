package za.co.sheeptracker.mapper;

import org.mapstruct.*;
import za.co.sheeptracker.dto.treatment.TreatmentCreateRequest;
import za.co.sheeptracker.dto.treatment.TreatmentResponse;
import za.co.sheeptracker.entity.Treatment;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface TreatmentMapper {
    TreatmentResponse toResponse(Treatment treatment);

    List<TreatmentResponse> toResponseList(List<Treatment> treatment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "sheepId", ignore = true)
    @Mapping(target = "medicationId", ignore = true)
    Treatment toEntity(TreatmentCreateRequest request);

    // OffsetDateTime -> Instant
    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    // Instant -> OffsetDateTime
    default OffsetDateTime map(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "sheepId", ignore = true)
    @Mapping(target = "medicationId", ignore = true)
    void updateEntity(TreatmentCreateRequest request, @MappingTarget Treatment treatment);
}
