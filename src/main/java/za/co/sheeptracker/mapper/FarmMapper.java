package za.co.sheeptracker.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import za.co.sheeptracker.dto.farm.FarmCreateRequest;
import za.co.sheeptracker.dto.farm.FarmCreateResponse;
import za.co.sheeptracker.entity.Farm;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FarmMapper {

    FarmCreateResponse toResponse(Farm farm);
    List<FarmCreateResponse> toResponseList(List<Farm> farms);

    Farm toEntity(FarmCreateRequest request);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            FarmCreateRequest request,
            @MappingTarget Farm farm
    );
}