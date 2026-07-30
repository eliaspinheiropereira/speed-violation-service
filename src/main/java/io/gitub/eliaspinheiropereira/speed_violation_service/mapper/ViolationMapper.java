package io.gitub.eliaspinheiropereira.speed_violation_service.mapper;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationWithInfractionResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationWithoutInfractionResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ViolationDetailMapper.class})
public interface ViolationMapper {

    Violation toEntity(ViolationRequest dto);
    ViolationWithInfractionResponse toDtoWithInfraction(Violation entity);
    ViolationWithoutInfractionResponse toDtoWithoutInfraction(Violation entity);
}
