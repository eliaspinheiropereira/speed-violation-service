package io.gitub.eliaspinheiropereira.speed_violation_service.handler;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ErrorFieldResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ErrorResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.ViolationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<ErrorFieldResponse> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorFieldResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "There was an error validating the field.",
                erros
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ORIGIN",
                List.of(new ErrorFieldResponse(
                        ex.getName(),
                        "Invalid x-origin header value: " + ex.getValue()
                ))
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ViolationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleViolationNotFoundException(
            ViolationNotFoundException ex) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
