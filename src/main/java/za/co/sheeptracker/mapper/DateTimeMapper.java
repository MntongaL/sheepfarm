package za.co.sheeptracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface DateTimeMapper {

    @Named("offsetDateTimeToInstant")
    default Instant offsetDateTimeToInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    @Named("instantToOffsetDateTime")
    default OffsetDateTime instantToOffsetDateTime(Instant value) {
        return value == null
                ? null
                : value.atOffset(java.time.ZoneOffset.UTC);
    }
}