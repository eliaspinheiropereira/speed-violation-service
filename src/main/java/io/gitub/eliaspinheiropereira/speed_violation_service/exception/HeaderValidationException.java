package io.gitub.eliaspinheiropereira.speed_violation_service.exception;

public class HeaderValidationException extends RuntimeException{
    public HeaderValidationException(String message) {
        super(message);
    }
}
