package io.gitub.eliaspinheiropereira.speed_violation_service.mapper;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationDetailResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ViolationDetailMapper {

    ViolationDetailResponse toDto(ViolationDetail entity);
}
