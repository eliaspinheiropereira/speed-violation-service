package io.gitub.eliaspinheiropereira.speed_violation_service.mapper;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ViolationDetailMapper.class})
public interface ViolationMapper {
    Violation toEntity(ViolationRequest dto);

    @Mapping(source = "violationDetail", target = "violation")
    ViolationResponse toDto(Violation entity);

}

