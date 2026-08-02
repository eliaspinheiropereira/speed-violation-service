package io.gitub.eliaspinheiropereira.speed_violation_service.validation;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.HeaderValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Validation Tests")
class ValidationTest {

    @Autowired
    private Validator validator;

    @Autowired
    private HeaderValidation headerValidation;

    private Timestamp validTimestamp;

    @BeforeEach
    void setUp() {
        validTimestamp = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
    }


    @DisplayName("Should accept license plate in format ABC1234")
    @ParameterizedTest(name = "License plate: {0}")
    @ValueSource(strings = {
            "ABC1234",
            "XYZ9999",
            "DEF0000",
            "QWE1111",
            "ZZZ9999"
    })
    void testLicensePlate_Format1_ShouldBeValid(String licensePlate) {
        ViolationRequest request = new ViolationRequest(
                licensePlate,
                70,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isEmpty();
    }

    @DisplayName("Should accept license plate in format ABC1D23 (Mercosul)")
    @ParameterizedTest(name = "License plate: {0}")
    @ValueSource(strings = {
            "ABC1D23",
            "XYZ9K88",
            "DEF0A00",
            "QWE1Z11",
            "ZZZ9X99"
    })
    void testLicensePlate_Format2_ShouldBeValid(String licensePlate) {
        ViolationRequest request = new ViolationRequest(
                licensePlate,
                70,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isEmpty();
    }

    @DisplayName("Should reject invalid license plate formats")
    @ParameterizedTest(name = "Invalid license plate: {0}")
    @ValueSource(strings = {
            "abc1234",
            "AB12345",
            "ABCD1234",
            "ABC-1234",
            "1234ABC",
            "ABC12D4",
            "AB1234",
            "ABC123",
            "   ",
            ""
    })
    void testLicensePlate_InvalidFormats(String licensePlate) {
        ViolationRequest request = new ViolationRequest(
                licensePlate,
                70,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
    }


    @DisplayName("Should accept valid measured speeds")
    @ParameterizedTest(name = "Measured speed: {0} km/h")
    @ValueSource(ints = {
            1,
            50,
            70,
            100,
            150,
            200,
            Integer.MAX_VALUE
    })
    void testMeasuredSpeed_ValidSpeeds(int measuredSpeed) {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                measuredSpeed,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("measuredSpeed"));
    }

    @DisplayName("Should reject zero or negative measured speeds")
    @ParameterizedTest(name = "Measured speed: {0} km/h")
    @ValueSource(ints = {
            0,
            -1,
            -10,
            -100,
            Integer.MIN_VALUE
    })
    void testMeasuredSpeed_InvalidSpeeds(int measuredSpeed) {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                measuredSpeed,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("measuredSpeed"));
    }

    @Test
    @DisplayName("Should reject null measured speed")
    void testMeasuredSpeed_Null() {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                null,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("measuredSpeed"));
    }


    @DisplayName("Should accept valid speed limits")
    @ParameterizedTest(name = "Speed limit: {0} km/h")
    @ValueSource(ints = {
            1,
            40,
            60,
            80,
            120,
            Integer.MAX_VALUE
    })
    void testSpeedLimit_ValidLimits(int speedLimit) {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                speedLimit,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("speedLimit"));
    }

    @DisplayName("Should reject zero or negative speed limits")
    @ParameterizedTest(name = "Speed limit: {0} km/h")
    @ValueSource(ints = {
            0,
            -1,
            -50,
            Integer.MIN_VALUE
    })
    void testSpeedLimit_InvalidLimits(int speedLimit) {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                speedLimit,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("speedLimit"));
    }

    @Test
    @DisplayName("Should reject null speed limit")
    void testSpeedLimit_Null() {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                null,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("speedLimit"));
    }


    @DisplayName("Should accept current or past timestamps")
    @ParameterizedTest(name = "Timestamp offset hours: {0}")
    @CsvSource({
            "-1",
            "-5",
            "-24",
            "-100"
    })
    void testCaptureTimestamp_ValidTimestamps(int hoursOffset) {
        Timestamp validTs = Timestamp.valueOf(LocalDateTime.now().plusHours(hoursOffset));
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                "EQ-001",
                validTs
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("captureTimestamp"));
    }

    @DisplayName("Should reject future timestamps")
    @ParameterizedTest(name = "Timestamp offset hours: {0}")
    @CsvSource({
            "1",
            "5",
            "24",
            "100"
    })
    void testCaptureTimestamp_FutureTimestamps(int hoursOffset) {
        Timestamp futureTs = Timestamp.valueOf(LocalDateTime.now().plusHours(hoursOffset));
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                "EQ-001",
                futureTs
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("captureTimestamp"));
    }

    @Test
    @DisplayName("Should accept timestamp at exactly current time")
    void testCaptureTimestamp_ExactlyNow() {
        Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                "EQ-001",
                nowTs
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("captureTimestamp"));
    }

    @Test
    @DisplayName("Should reject null timestamp")
    void testCaptureTimestamp_Null() {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                "EQ-001",
                null
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("captureTimestamp"));
    }


    @DisplayName("Should accept valid equipment IDs")
    @ParameterizedTest(name = "Equipment ID: {0}")
    @ValueSource(strings = {
            "EQ-001",
            "EQ-999",
            "EQ001",
            "EQUIPMENT1",
            "123",
            "ABC"
    })
    void testEquipmentId_ValidIds(String equipmentId) {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                equipmentId,
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("equipmentId"));
    }

    @DisplayName("Should reject blank equipment IDs")
    @ParameterizedTest(name = "Equipment ID: '{0}'")
    @ValueSource(strings = {
            "",
            "   ",
            "\t",
            "\n"
    })
    void testEquipmentId_BlankIds(String equipmentId) {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                equipmentId,
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("equipmentId"));
    }

    @Test
    @DisplayName("Should reject null equipment ID")
    void testEquipmentId_Null() {
        ViolationRequest request = new ViolationRequest(
                "ABC1234",
                70,
                60,
                null,
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .isNotEmpty()
                .anyMatch(v -> v.getPropertyPath().toString().equals("equipmentId"));
    }


    @Test
    @DisplayName("Should accept valid origin header")
    void testHeaderValidation_ValidOrigin() {
        assertThatNoException()
                .isThrownBy(() -> headerValidation.validation("FIXED"));

        assertThatNoException()
                .isThrownBy(() -> headerValidation.validation("MOBILE"));

        assertThatNoException()
                .isThrownBy(() -> headerValidation.validation("any_value"));
    }

    @Test
    @DisplayName("Should reject null origin header")
    void testHeaderValidation_NullOrigin() {
        assertThatThrownBy(() -> headerValidation.validation(null))
                .isInstanceOf(HeaderValidationException.class)
                .hasMessageContaining("Origin header is missing or empty");
    }

    @Test
    @DisplayName("Should reject empty origin header")
    void testHeaderValidation_EmptyOrigin() {
        assertThatThrownBy(() -> headerValidation.validation(""))
                .isInstanceOf(HeaderValidationException.class)
                .hasMessageContaining("Origin header is missing or empty");
    }

    @Test
    @DisplayName("Should reject whitespace-only origin header")
    void testHeaderValidation_WhitespaceOrigin() {
        assertThatThrownBy(() -> headerValidation.validation("   "))
                .isInstanceOf(HeaderValidationException.class)
                .hasMessageContaining("Origin header is missing or empty");

        assertThatThrownBy(() -> headerValidation.validation("\t"))
                .isInstanceOf(HeaderValidationException.class)
                .hasMessageContaining("Origin header is missing or empty");
    }


    @Test
    @DisplayName("Should accumulate multiple validation errors")
    void testMultipleViolations_AllFieldsInvalid() {
        ViolationRequest request = new ViolationRequest(
                "invalid",
                0,
                -10,
                "",
                Timestamp.valueOf(LocalDateTime.now().plusDays(1))
        );

        Set<ConstraintViolation<ViolationRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty().hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("Should validate each field independently")
    void testValidation_FieldIndependence() {
        ViolationRequest request1 = new ViolationRequest(
                "invalid",
                70,
                60,
                "EQ-001",
                validTimestamp
        );

        Set<ConstraintViolation<ViolationRequest>> violations1 = validator.validate(request1);

        assertThat(violations1)
                .isNotEmpty()
                .allMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
    }
}

