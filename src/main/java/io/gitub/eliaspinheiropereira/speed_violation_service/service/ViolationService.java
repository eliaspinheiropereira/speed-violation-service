package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.mapper.ViolationMapper;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import io.gitub.eliaspinheiropereira.speed_violation_service.repository.ViolationRepository;
import io.gitub.eliaspinheiropereira.speed_violation_service.validation.HeaderValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final ViolationMapper violationMapper;
    private final HeaderValidation headerValidation;

    public void evaluateViolation(Origin origin, ViolationRequest dto) {
        this.headerValidation.validation(origin.name());
        var violation = violationMapper.toEntity(dto);
        violationRepository.save(violation);
    }
}
