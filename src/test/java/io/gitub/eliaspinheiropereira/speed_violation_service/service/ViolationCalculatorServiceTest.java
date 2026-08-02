package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.CtbCode;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ViolationCalculatorService Tests")
class ViolationCalculatorServiceTest {

    private ViolationCalculatorService violationCalculatorService;
    private Violation violation;

    @BeforeEach
    void setUp() {
        violationCalculatorService = new ViolationCalculatorService();
        violation = new Violation();
        violation.setId(1L);
        violation.setLicensePlate("ABC1234");
        violation.setEquipmentId("EQ-001");
        violation.setCaptureTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        violation.setOrigin(Origin.FIXED);
    }


    @DisplayName("Should calculate considered speed reducing 7 km/h when speed <= 100")
    @ParameterizedTest(name = "Measured Speed: {0} km/h, Expected: {1} km/h")
    @CsvSource({
            "50, 43",
            "60, 53",
            "80, 73",
            "100, 93",
            "30, 23",
            "7, 0",
    })
    void testCalculateConsideredSpeed_UpTo100kmh(int measuredSpeed, int expectedConsideredSpeed) {
        violation.setMeasuredSpeed(measuredSpeed);
        violation.setSpeedLimit(0);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getConsideredSpeed()).isEqualTo(expectedConsideredSpeed);
    }

    @DisplayName("Should calculate considered speed reducing 7% when speed > 100")
    @ParameterizedTest(name = "Measured Speed: {0} km/h, Expected: {1} km/h")
    @CsvSource({
            "101, 93",
            "150, 139",
            "200, 186",
            "120, 111",
    })
    void testCalculateConsideredSpeed_Above100kmh(int measuredSpeed, int expectedConsideredSpeed) {
        violation.setMeasuredSpeed(measuredSpeed);
        violation.setSpeedLimit(0);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getConsideredSpeed()).isEqualTo(expectedConsideredSpeed);
    }


    @DisplayName("Should detect violation when considered speed exceeds speed limit")
    @ParameterizedTest(name = "Measured: {0} km/h, Speed Limit: {1} km/h, Has Violation: true")
    @CsvSource({
            "70, 60",
            "90, 80",
            "110, 90",
    })
    void testViolation_ShouldDetectViolation(int measuredSpeed, int speedLimit) {
        violation.setMeasuredSpeed(measuredSpeed);
        violation.setSpeedLimit(speedLimit);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.isHasViolation()).isTrue();
        assertThat(violation.getExcessPercentage()).isGreaterThan(0);
    }

    @DisplayName("Should not detect violation when considered speed <= speed limit")
    @ParameterizedTest(name = "Measured: {0} km/h, Speed Limit: {1} km/h, Has Violation: false")
    @CsvSource({
            "60, 60",
            "65, 60",
            "80, 90",
    })
    void testViolation_ShouldNotDetectViolation(int measuredSpeed, int speedLimit) {
        violation.setMeasuredSpeed(measuredSpeed);
        violation.setSpeedLimit(speedLimit);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.isHasViolation()).isFalse();
        assertThat(violation.getExcessPercentage()).isZero();
    }


    @DisplayName("Should calculate percentage excess correctly with 2 decimal places")
    @ParameterizedTest(name = "Measured: {0} km/h, Limit: {1} km/h, Expected Excess: {2}%")
    @CsvSource({
            "70, 60, 5.0",
            "80, 60, 21.67",
            "90, 60, 38.33",
            "100, 60, 55.0",
    })
    void testPercentageExcess_ShouldCalculateCorrectly(int measuredSpeed, int speedLimit, double expectedExcess) {
        violation.setMeasuredSpeed(measuredSpeed);
        violation.setSpeedLimit(speedLimit);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getExcessPercentage()).isEqualTo(expectedExcess);
    }


    @Test
    @DisplayName("Should classify as MEDIUM severity when excess <= 20%")
    void testSeverity_MediumClassification() {
        violation.setMeasuredSpeed(70);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.isHasViolation()).isTrue();
        assertThat(violation.getExcessPercentage()).isEqualTo(5.0);
        assertThat(violation.getViolationDetail().getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("Should classify as SERIOUS severity when 20% < excess <= 50%")
    void testSeverity_SeriousClassification() {
        violation.setMeasuredSpeed(85);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.isHasViolation()).isTrue();
        assertThat(violation.getExcessPercentage()).isGreaterThan(20.0);
        assertThat(violation.getExcessPercentage()).isLessThanOrEqualTo(50.0);
        assertThat(violation.getViolationDetail().getSeverity()).isEqualTo("SERIOUS");
    }

    @Test
    @DisplayName("Should classify as VERY_SERIOUS severity when excess > 50%")
    void testSeverity_VerySeriousClassification() {
        violation.setMeasuredSpeed(100);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.isHasViolation()).isTrue();
        assertThat(violation.getExcessPercentage()).isGreaterThan(50.0);
        assertThat(violation.getViolationDetail().getSeverity()).isEqualTo("VERY_SERIOUS");
    }


    @Test
    @DisplayName("Should handle minimum measured speed")
    void testEdgeCase_MinimumMeasuredSpeed() {
        violation.setMeasuredSpeed(1);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getConsideredSpeed()).isEqualTo(-6);
        assertThat(violation.isHasViolation()).isFalse();
    }

    @Test
    @DisplayName("Should handle high measured speed (above 100 km/h)")
    void testEdgeCase_HighMeasuredSpeed() {
        violation.setMeasuredSpeed(200);
        violation.setSpeedLimit(100);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getConsideredSpeed()).isEqualTo(186);
        assertThat(violation.isHasViolation()).isTrue();
        assertThat(violation.getExcessPercentage()).isEqualTo(86.0);
    }

    @Test
    @DisplayName("Should set processedAt timestamp when calculating violation")
    void testProcessedAt_ShouldBeSet() {
        violation.setMeasuredSpeed(70);
        violation.setSpeedLimit(60);
        long beforeCalculation = System.currentTimeMillis();

        violationCalculatorService.calculateViolation(violation);
        long afterCalculation = System.currentTimeMillis();

        assertThat(violation.getProcessedAt())
                .isNotNull();

        assertThat(violation.getProcessedAt().getTime())
                .isGreaterThanOrEqualTo(beforeCalculation)
                .isLessThanOrEqualTo(afterCalculation);
    }

    @Test
    @DisplayName("Should create ViolationDetail when violation is detected")
    void testViolationDetail_ShouldBeCreated() {
        violation.setMeasuredSpeed(70);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        if (violation.isHasViolation()) {
            assertThat(violation.getViolationDetail()).isNotNull();
        }
    }

    @Test
    @DisplayName("Should not create ViolationDetail when no violation is detected")
    void testViolationDetail_ShouldNotBeCreatedWhenNoViolation() {
        violation.setMeasuredSpeed(60);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getViolationDetail()).isNull();
        assertThat(violation.isHasViolation()).isFalse();
    }

    @Test
    @DisplayName("Should verify severity codes are correctly assigned")
    void testSeverity_VerifyCtbCodes() {
        violation.setMeasuredSpeed(70);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        if (violation.isHasViolation()) {
            assertThat(violation.getViolationDetail().getCtbCode())
                    .isIn(CtbCode.MEDIUM.getCode(), CtbCode.SERIOUS.getCode(), CtbCode.VERY_SERIOUS.getCode());
        }
    }

    @Test
    @DisplayName("Should set considered speed even when no violation")
    void testConsideredSpeed_ShouldBeSetWhenNoViolation() {
        violation.setMeasuredSpeed(65);
        violation.setSpeedLimit(60);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getConsideredSpeed()).isEqualTo(58);
        assertThat(violation.isHasViolation()).isFalse();
    }

    @Test
    @DisplayName("Should accurately handle boundary case at exactly 100 km/h")
    void testBoundary_ExactlyAt100kmh() {
        violation.setMeasuredSpeed(100);
        violation.setSpeedLimit(50);

        violationCalculatorService.calculateViolation(violation);

        assertThat(violation.getConsideredSpeed()).isEqualTo(93);
        assertThat(violation.getExcessPercentage()).isEqualTo(86.0);
    }
}

