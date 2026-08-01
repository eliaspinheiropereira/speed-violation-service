package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.ViolationNotFoundException;
import io.gitub.eliaspinheiropereira.speed_violation_service.mapper.ViolationMapper;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import io.gitub.eliaspinheiropereira.speed_violation_service.repository.ViolationRepository;
import io.gitub.eliaspinheiropereira.speed_violation_service.validation.HeaderValidation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final ViolationMapper violationMapper;
    private final HeaderValidation headerValidation;
    private final ViolationCalculatorService violationCalculatorService;

    @Transactional
    public void evaluateViolation(Origin origin, ViolationRequest dto) {
        this.headerValidation.validation(origin.name());
        var violation = violationMapper.toEntity(dto);
        this.violationCalculatorService.calculateViolation(violation);
        violationRepository.save(violation);
    }

    public ViolationResponse getViolation(String licensePlate) {
        Optional<Violation> searchViolation = this.violationRepository.findByLicensePlate(licensePlate);

        if(searchViolation.isEmpty()){
            log.error("Violation; not found for license plate: {}", licensePlate);
            throw new ViolationNotFoundException("Violation not found for license plate: " + licensePlate);
        }

        return violationMapper.toDto(searchViolation.get());
    }
}
