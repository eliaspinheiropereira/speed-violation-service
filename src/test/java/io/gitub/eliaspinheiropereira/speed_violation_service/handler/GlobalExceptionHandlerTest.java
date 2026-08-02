package io.gitub.eliaspinheiropereira.speed_violation_service.handler;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ErrorFieldResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ErrorResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.ViolationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }



    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException for invalid origin header")
    void handleMethodArgumentTypeMismatchException_WithInvalidOrigin_ShouldReturnBadRequest() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("x-origin");
        when(exception.getValue()).thenReturn("INVALID_VALUE");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isNotNull()
                .hasFieldOrPropertyWithValue("status", 400)
                .hasFieldOrPropertyWithValue("message", "INVALID_ORIGIN");

        assertThat(response.getBody().errors())
                .hasSize(1)
                .allMatch(error -> error.field().equals("x-origin") &&
                        error.message().contains("Invalid x-origin header value: INVALID_VALUE"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with specific header value")
    void handleMethodArgumentTypeMismatchException_VerifyErrorMessage_ShouldContainActualValue() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("x-origin");
        when(exception.getValue()).thenReturn("WRONG_ORIGIN");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception);

        assertThat(response.getBody().errors())
                .hasSize(1)
                .allMatch(error -> error.message().contains("WRONG_ORIGIN"));
    }

    @Test
    @DisplayName("Should return empty errors list for type mismatch exception")
    void handleMethodArgumentTypeMismatchException_VerifyErrorsStructure_ShouldHaveOneError() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("x-origin");
        when(exception.getValue()).thenReturn("INVALID");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(exception);

        assertThat(response.getBody().errors())
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
    }


    @Test
    @DisplayName("Should handle ViolationNotFoundException")
    void handleViolationNotFoundException_WithNotFoundViolation_ShouldReturnNotFound() {
        String message = "Violation not found for license plate: ABC1234";
        ViolationNotFoundException exception = new ViolationNotFoundException(message);

        ResponseEntity<ErrorResponse> response = handler.handleViolationNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .isNotNull()
                .hasFieldOrPropertyWithValue("status", 404)
                .hasFieldOrPropertyWithValue("message", message);
    }

    @Test
    @DisplayName("Should return empty errors list for ViolationNotFoundException")
    void handleViolationNotFoundException_VerifyErrorsList_ShouldBeEmpty() {
        ViolationNotFoundException exception = new ViolationNotFoundException(
                "Violation not found for license plate: XYZ9999");

        ResponseEntity<ErrorResponse> response = handler.handleViolationNotFoundException(exception);

        assertThat(response.getBody().errors())
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should preserve exception message in error response")
    void handleViolationNotFoundException_VerifyMessage_ShouldContainExceptionMessage() {
        String expectedMessage = "Violation not found for license plate: DEF5678";
        ViolationNotFoundException exception = new ViolationNotFoundException(expectedMessage);

        ResponseEntity<ErrorResponse> response = handler.handleViolationNotFoundException(exception);

        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Should return NOT_FOUND status for ViolationNotFoundException")
    void handleViolationNotFoundException_VerifyStatus_ShouldBe404() {
        ViolationNotFoundException exception = new ViolationNotFoundException(
                "Violation not found");

        ResponseEntity<ErrorResponse> response = handler.handleViolationNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
    }


    @Test
    @DisplayName("Should handle all exception types with proper HTTP status codes")
    void allExceptionHandlers_VerifyStatusCodes_ShouldBeCorrect() {
        MethodArgumentTypeMismatchException mismatchException = mock(MethodArgumentTypeMismatchException.class);
        when(mismatchException.getName()).thenReturn("x-origin");
        when(mismatchException.getValue()).thenReturn("INVALID");

        ViolationNotFoundException notFoundException = new ViolationNotFoundException("Not found");

        assertThat(handler.handleMethodArgumentTypeMismatchException(mismatchException)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(handler.handleViolationNotFoundException(notFoundException)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

