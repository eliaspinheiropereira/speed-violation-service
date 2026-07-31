package io.gitub.eliaspinheiropereira.speed_violation_service.validation;

import io.gitub.eliaspinheiropereira.speed_violation_service.exception.HeaderValidationException;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.service.ViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeaderValidation {

    public void validation(String origin){
        if(origin == null || origin.trim().isEmpty()){
            log.error("Origin header is missing or empty");
            throw new HeaderValidationException("Origin header is missing or empty");
        }
    }
}
