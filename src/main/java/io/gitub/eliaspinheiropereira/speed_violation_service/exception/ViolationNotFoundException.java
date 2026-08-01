package io.gitub.eliaspinheiropereira.speed_violation_service.exception;

public class ViolationNotFoundException extends RuntimeException{
    public ViolationNotFoundException(String message) {
        super(message);
    }
}
