package za.co.sheeptracker.mapper;

import org.mapstruct.*;
import za.co.sheeptracker.dto.medication.MedicationCreateRequest;
import za.co.sheeptracker.dto.medication.MedicationCreateResponse;
import za.co.sheeptracker.dto.medication.MedicationUpdateRequest;
import za.co.sheeptracker.entity.Medication;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface MedicationMapper {
    @Mapping(target = "createdAt", ignore = true)
    MedicationCreateResponse toResponse(Medication medication);

    List<MedicationCreateResponse> toResponseList(List<Medication> medications);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Medication toEntity(MedicationCreateRequest request);

    @Mapping(target = "createdAt", ignore = true)
    Medication toEntity(MedicationUpdateRequest updateRequest);

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
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(MedicationCreateRequest request, @MappingTarget Medication medication);
}
