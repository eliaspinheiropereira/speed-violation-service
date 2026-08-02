package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.CtbCode;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ViolationCalculatorService Tests")
class ViolationCalculatorServiceTest {

    @Autowired
    private ViolationCalculatorService violationCalculatorService;

    private Violation violation;

    @BeforeEach
    void setUp() {
        // Set the tolerance values via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(violationCalculatorService, "toleranceKmh", 7);
        ReflectionTestUtils.setField(violationCalculatorService, "tolerancePercentage", 0.07);

        violation = new Violation();
        violation.setLicensePlate("ABC-1234");
        violation.setEquipmentId("EQ-001");
        violation.setCaptureTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        violation.setOrigin(Origin.FIXED);
        violation.setHasViolation(false);
    }

    @Test
    @DisplayName("Should calculate considered speed when measured speed is 85 km/h (deduct 7)")
    void testCalculateConsideredSpeedBelowOrEqual100() {
        // Arrange
        violation.setMeasuredSpeed(85);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(78) // 85 - 7 = 78
                .isNotZero()
                .isGreaterThan(0);

        assertThat(violation.getProcessedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("Should calculate considered speed when measured speed is above 100 km/h (deduct 7%)")
    void testCalculateConsideredSpeedAbove100() {
        // Arrange
        violation.setMeasuredSpeed(120);
        violation.setSpeedLimit(80);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // 120 - (120 * 0.07) = 120 - 8.4 = 111.6 -> floor = 111
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(111)
                .isGreaterThan(100);

        assertThat(violation.getProcessedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("Should detect medium severity violation (20% excess)")
    void testMediumSeverityViolation() {
        // Arrange
        violation.setMeasuredSpeed(79);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // Considered speed: 79 - 7 = 72
        // Excess: ((72 - 60) / 60) * 100 = 20%
        assertThat(violation.getHasViolation())
                .isTrue();

        assertThat(violation.getExcessPercentage())
                .isGreaterThan(0)
                .isLessThanOrEqualTo(20);

        assertThat(violation.getViolationDetail())
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "MEDIUM")
                .hasFieldOrPropertyWithValue("ctbCode", "218-I");
    }

    @Test
    @DisplayName("Should detect serious severity violation (between 20% and 50% excess)")
    void testSeriousSeverityViolation() {
        // Arrange
        violation.setMeasuredSpeed(95);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // Considered speed: 95 - 7 = 88
        // Excess: ((88 - 60) / 60) * 100 = 46.67%
        assertThat(violation.getHasViolation())
                .isTrue();
        
        assertThat(violation.getExcessPercentage())
                .isGreaterThan(20)
                .isLessThanOrEqualTo(50);
        
        assertThat(violation.getViolationDetail())
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "SERIOUS")
                .hasFieldOrPropertyWithValue("ctbCode", "218-II");
    }

    @Test
    @DisplayName("Should detect very serious severity violation (above 50% excess)")
    void testVerySeriouspSeverityViolation() {
        // Arrange
        violation.setMeasuredSpeed(150);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation.getHasViolation())
                .isTrue();

        assertThat(violation.getExcessPercentage())
                .isGreaterThan(50);

        assertThat(violation.getViolationDetail())
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "VERY_SERIOUS")
                .hasFieldOrPropertyWithValue("ctbCode", "218-III");
    }

    @Test
    @DisplayName("Should not detect violation when considered speed equals speed limit")
    void testNoViolationWhenSpeedEqualsLimit() {
        // Arrange
        violation.setMeasuredSpeed(67);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation.getHasViolation())
                .isFalse();

        assertThat(violation.getConsideredSpeed())
                .isEqualTo(60); // 67 - 7 = 60

        assertThat(violation.getViolationDetail())
                .isNull();

        assertThat(violation.getProcessedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("Should not detect violation when considered speed is below speed limit")
    void testNoViolationWhenSpeedBelowLimit() {
        // Arrange
        violation.setMeasuredSpeed(50);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation.getHasViolation())
                .isFalse();

        assertThat(violation.getConsideredSpeed())
                .isEqualTo(43) // 50 - 7 = 43
                .isLessThan(violation.getSpeedLimit());

        assertThat(violation.getViolationDetail())
                .isNull();

        assertThat(violation.getProcessedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("Should calculate exact percentage excess correctly")
    void testPercentageExcessCalculation() {
        // Arrange
        violation.setMeasuredSpeed(90);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // Considered speed: 90 - 7 = 83
        // Excess: ((83 - 60) / 60) * 100 = (23 / 60) * 100 = 38.33%
        assertThat(violation.getExcessPercentage())
                .isCloseTo(38.33, within(0.1));
    }

    @Test
    @DisplayName("Should set processed timestamp when violation is calculated")
    void testProcessedTimestampIsSet() {
        // Arrange
        Timestamp beforeCalculation = new Timestamp(System.currentTimeMillis());
        violation.setMeasuredSpeed(85);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation.getProcessedAt())
                .isNotNull()
                .isAfterOrEqualTo(beforeCalculation)
                .isBeforeOrEqualTo(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("Should calculate high speed violation with floor operation correctly")
    void testHighSpeedViolationWithFloorOperation() {
        // Arrange
        violation.setMeasuredSpeed(105);
        violation.setSpeedLimit(80);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // Considered speed: 105 - (105 * 0.07) = 105 - 7.35 = 97.65 -> floor = 97
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(97)
                .isLessThan(violation.getMeasuredSpeed());

        assertThat(violation.getHasViolation())
                .isTrue();
    }

    @Test
    @DisplayName("Should set considered speed even when no violation is detected")
    void testConsideredSpeedSetWhenNoViolation() {
        // Arrange
        violation.setMeasuredSpeed(55);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(48); // 55 - 7 = 48

        assertThat(violation.getHasViolation())
                .isFalse();
    }

    @Test
    @DisplayName("Should preserve violation object references after calculation")
    void testViolationObjectIntegrity() {
        // Arrange
        violation.setMeasuredSpeed(100);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        assertThat(violation)
                .isNotNull();

        assertThat(violation.getLicensePlate())
                .isEqualTo("ABC-1234");

        assertThat(violation.getEquipmentId())
                .isEqualTo("EQ-001");

        assertThat(violation.getOrigin())
                .isEqualTo(Origin.FIXED);
    }

    @Test
    @DisplayName("Should use configured tolerance KMH value from properties")
    void testCalculateConsideredSpeedWithConfiguredToleranceKmh() {
        // Arrange
        ReflectionTestUtils.setField(violationCalculatorService, "toleranceKmh", 5);
        violation.setMeasuredSpeed(75);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // With tolerance of 5: 75 - 5 = 70
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(70)
                .isNotEqualTo(68); // Would be 68 with default tolerance of 7
    }

    @Test
    @DisplayName("Should use configured tolerance percentage value from properties")
    void testCalculateConsideredSpeedWithConfiguredTolerancePercentage() {
        // Arrange
        ReflectionTestUtils.setField(violationCalculatorService, "tolerancePercentage", 0.10);
        violation.setMeasuredSpeed(120);
        violation.setSpeedLimit(80);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // With tolerance 10%: 120 - (120 * 0.10) = 120 - 12 = 108
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(108)
                .isNotEqualTo(111); // Would be 111 with default tolerance of 7%
    }

    @Test
    @DisplayName("Should respect different tolerance values for violations")
    void testDifferentToleranceValuesAffectViolationDetection() {
        // Arrange - Use custom tolerance
        ReflectionTestUtils.setField(violationCalculatorService, "toleranceKmh", 10);
        violation.setMeasuredSpeed(75);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // With 10 km/h tolerance: 75 - 10 = 65
        // Excess: (65-60)/60 * 100 = 8.33% -> MEDIUM
        assertThat(violation.getHasViolation())
                .isTrue();

        assertThat(violation.getConsideredSpeed())
                .isEqualTo(65);

        assertThat(violation.getViolationDetail())
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "MEDIUM");
    }

    @Test
    @DisplayName("Should apply tolerance correctly for speeds above 100 km/h with percentage")
    void testTolerancePercentageAppliedCorrectlyForHighSpeeds() {
        // Arrange
        ReflectionTestUtils.setField(violationCalculatorService, "tolerancePercentage", 0.05);
        violation.setMeasuredSpeed(150);
        violation.setSpeedLimit(100);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // With 5% tolerance: 150 - (150 * 0.05) = 150 - 7.5 = 142.5 -> floor = 142
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(142);

        assertThat(violation.getHasViolation())
                .isTrue();
    }

    @Test
    @DisplayName("Should calculate violation correctly with custom tolerance at boundary")
    void testViolationCalculationAtBoundaryWithCustomTolerance() {
        // Arrange
        ReflectionTestUtils.setField(violationCalculatorService, "toleranceKmh", 8);
        violation.setMeasuredSpeed(72);
        violation.setSpeedLimit(60);

        // Act
        violationCalculatorService.calculateViolation(violation);

        // Assert
        // With 8 km/h tolerance: 72 - 8 = 64
        // Excess: (64-60)/60 * 100 = 6.67% -> MEDIUM
        assertThat(violation.getConsideredSpeed())
                .isEqualTo(64);

        assertThat(violation.getExcessPercentage())
                .isCloseTo(6.67, within(0.1));
    }
}

