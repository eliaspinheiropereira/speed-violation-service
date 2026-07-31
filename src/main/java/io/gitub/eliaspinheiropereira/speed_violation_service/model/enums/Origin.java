package io.gitub.eliaspinheiropereira.speed_violation_service.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.HeaderValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Origin {
    FIXED,
    MOBILE,
    HANDHELD;

    @JsonCreator
    public static Origin fromString(String value){
        try{
            return Origin.valueOf(value.toUpperCase());
        } catch (HeaderValidationException e){
            log.error("Invalid Origin value: {}", value);
            throw new HeaderValidationException("Invalid Origin value: " + value);
        }
    }
}
