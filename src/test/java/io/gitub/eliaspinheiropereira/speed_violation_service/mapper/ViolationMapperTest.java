package io.gitub.eliaspinheiropereira.speed_violation_service.mapper;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationDetailResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ViolationMapper Tests")
class ViolationMapperTest {

    @Autowired
    private ViolationMapper violationMapper;

    @Autowired
    private ViolationDetailMapper violationDetailMapper;

    private ViolationRequest violationRequest;
    private Violation violation;
    private ViolationDetail violationDetail;

    @BeforeEach
    void setUp() {
        violationRequest = new ViolationRequest(
                "ABC1234",
                85,
                60,
                "EQ-001",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1))
        );

        violation = new Violation();
        violation.setId(1L);
        violation.setLicensePlate("ABC1234");
        violation.setEquipmentId("EQ-001");
        violation.setMeasuredSpeed(85);
        violation.setConsideredSpeed(82);
        violation.setSpeedLimit(60);
        violation.setExcessPercentage(36.7);
        violation.setHasViolation(true);
        violation.setCaptureTimestamp(Timestamp.valueOf(LocalDateTime.now().minusHours(1)));
        violation.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        violation.setOrigin(Origin.FIXED);

        violationDetail = new ViolationDetail();
        violationDetail.setId(1L);
        violationDetail.setSeverity("SERIOUS");
        violationDetail.setCtbCode("218-II");
        violationDetail.setViolation(violation);
        violation.setViolationDetail(violationDetail);
    }


    @Test
    @DisplayName("Should map ViolationRequest to Violation entity")
    void toEntity_WithValidRequest_ShouldMapCorrectly() {
        Violation result = violationMapper.toEntity(violationRequest);

        assertThat(result)
                .isNotNull()
                .hasFieldOrPropertyWithValue("licensePlate", "ABC1234")
                .hasFieldOrPropertyWithValue("equipmentId", "EQ-001")
                .hasFieldOrPropertyWithValue("measuredSpeed", 85)
                .hasFieldOrPropertyWithValue("speedLimit", 60);
    }

    @Test
    @DisplayName("Should map all fields from ViolationRequest to Violation")
    void toEntity_VerifyAllFields_ShouldMapCompleteData() {
        Violation result = violationMapper.toEntity(violationRequest);

        assertThat(result.getLicensePlate()).isEqualTo("ABC1234");
        assertThat(result.getEquipmentId()).isEqualTo("EQ-001");
        assertThat(result.getMeasuredSpeed()).isEqualTo(85);
        assertThat(result.getSpeedLimit()).isEqualTo(60);
        assertThat(result.getCaptureTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should map ViolationRequest with different license plate format")
    void toEntity_WithMercosulLicensePlate_ShouldMapCorrectly() {
        ViolationRequest request = new ViolationRequest(
                "ABC1D23",
                90,
                80,
                "EQ-002",
                Timestamp.valueOf(LocalDateTime.now())
        );

        Violation result = violationMapper.toEntity(request);

        assertThat(result.getLicensePlate()).isEqualTo("ABC1D23");
        assertThat(result.getEquipmentId()).isEqualTo("EQ-002");
        assertThat(result.getMeasuredSpeed()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should preserve timestamp when mapping ViolationRequest to entity")
    void toEntity_VerifyTimestamp_ShouldPreserveCaptureTime() {
        Violation result = violationMapper.toEntity(violationRequest);

        assertThat(result.getCaptureTimestamp()).isNotNull();
        assertThat(result.getCaptureTimestamp())
                .isEqualTo(violationRequest.captureTimestamp());
    }

    @Test
    @DisplayName("Should create new entity instance for each mapping")
    void toEntity_CallTwice_ShouldCreateDifferentInstances() {
        Violation result1 = violationMapper.toEntity(violationRequest);
        Violation result2 = violationMapper.toEntity(violationRequest);

        assertThat(result1).isNotSameAs(result2);
        assertThat(result1)
                .usingRecursiveComparison()
                .isEqualTo(result2);
    }


    @Test
    @DisplayName("Should map Violation entity to ViolationResponse")
    void toDto_WithValidEntity_ShouldMapCorrectly() {
        ViolationResponse result = violationMapper.toDto(violation);

        assertThat(result)
                .isNotNull()
                .hasFieldOrPropertyWithValue("licensePlate", "ABC1234")
                .hasFieldOrPropertyWithValue("equipmentId", "EQ-001")
                .hasFieldOrPropertyWithValue("measuredSpeed", 85)
                .hasFieldOrPropertyWithValue("consideredSpeed", 82)
                .hasFieldOrPropertyWithValue("speedLimit", 60)
                .hasFieldOrPropertyWithValue("excessPercentage", 36.7)
                .hasFieldOrPropertyWithValue("hasViolation", true);
    }

    @Test
    @DisplayName("Should map all fields from Violation entity to ViolationResponse")
    void toDto_VerifyAllFields_ShouldMapCompleteData() {
        ViolationResponse result = violationMapper.toDto(violation);

        assertThat(result.licensePlate()).isEqualTo("ABC1234");
        assertThat(result.equipmentId()).isEqualTo("EQ-001");
        assertThat(result.measuredSpeed()).isEqualTo(85);
        assertThat(result.consideredSpeed()).isEqualTo(82);
        assertThat(result.speedLimit()).isEqualTo(60);
        assertThat(result.excessPercentage()).isEqualTo(36.7);
        assertThat(result.hasViolation()).isTrue();
        assertThat(result.processedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map ViolationDetail to nested ViolationDetailResponse")
    void toDto_VerifyNestedMapping_ShouldMapViolationDetail() {
        ViolationResponse result = violationMapper.toDto(violation);

        assertThat(result.violation())
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "SERIOUS")
                .hasFieldOrPropertyWithValue("ctbCode", "218-II");
    }

    @Test
    @DisplayName("Should handle null ViolationDetail gracefully")
    void toDto_WithNullViolationDetail_ShouldMapWithoutError() {
        violation.setViolationDetail(null);

        ViolationResponse result = violationMapper.toDto(violation);

        assertThat(result).isNotNull();
        assertThat(result.violation()).isNull();
    }

    @Test
    @DisplayName("Should map violation with different severity levels")
    void toDto_WithDifferentSeverity_ShouldMapCorrectly() {
        violationDetail.setSeverity("MEDIUM");
        violationDetail.setCtbCode("218-I");

        ViolationResponse result = violationMapper.toDto(violation);

        assertThat(result.violation().severity()).isEqualTo("MEDIUM");
        assertThat(result.violation().ctbCode()).isEqualTo("218-I");
    }

    @Test
    @DisplayName("Should preserve all numeric precision in mapping")
    void toDto_VerifyNumericPrecision_ShouldPreserveDecimalPlaces() {
        violation.setExcessPercentage(36.67);
        violation.setMeasuredSpeed(95);
        violation.setConsideredSpeed(88);

        ViolationResponse result = violationMapper.toDto(violation);

        assertThat(result.excessPercentage()).isEqualTo(36.67);
        assertThat(result.measuredSpeed()).isEqualTo(95);
        assertThat(result.consideredSpeed()).isEqualTo(88);
    }


    @Test
    @DisplayName("Should map ViolationDetail entity to ViolationDetailResponse")
    void violationDetailMapper_toDto_WithValidEntity_ShouldMapCorrectly() {
        ViolationDetailResponse result = violationDetailMapper.toDto(violationDetail);

        assertThat(result)
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "SERIOUS")
                .hasFieldOrPropertyWithValue("ctbCode", "218-II");
    }

    @Test
    @DisplayName("Should map all ViolationDetail fields correctly")
    void violationDetailMapper_toDto_VerifyAllFields_ShouldMapCompleteData() {
        ViolationDetailResponse result = violationDetailMapper.toDto(violationDetail);

        assertThat(result.severity()).isEqualTo("SERIOUS");
        assertThat(result.ctbCode()).isEqualTo("218-II");
    }

    @Test
    @DisplayName("Should map ViolationDetail with VERY_SERIOUS severity")
    void violationDetailMapper_toDto_WithVerySeriousSeverity_ShouldMapCorrectly() {
        violationDetail.setSeverity("VERY_SERIOUS");
        violationDetail.setCtbCode("218-III");

        ViolationDetailResponse result = violationDetailMapper.toDto(violationDetail);

        assertThat(result.severity()).isEqualTo("VERY_SERIOUS");
        assertThat(result.ctbCode()).isEqualTo("218-III");
    }

    @Test
    @DisplayName("Should map ViolationDetail with MEDIUM severity")
    void violationDetailMapper_toDto_WithMediumSeverity_ShouldMapCorrectly() {
        violationDetail.setSeverity("MEDIUM");
        violationDetail.setCtbCode("218-I");

        ViolationDetailResponse result = violationDetailMapper.toDto(violationDetail);

        assertThat(result.severity()).isEqualTo("MEDIUM");
        assertThat(result.ctbCode()).isEqualTo("218-I");
    }


    @Test
    @DisplayName("Should perform complete mapping cycle Request -> Entity -> Response")
    void completeMappingCycle_ToEntityThenToDto_ShouldPreserveData() {
        Violation entity = violationMapper.toEntity(violationRequest);
        entity.setId(1L);
        entity.setConsideredSpeed(82);
        entity.setExcessPercentage(36.7);
        entity.setHasViolation(true);
        entity.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        entity.setViolationDetail(violationDetail);

        ViolationResponse response = violationMapper.toDto(entity);

        assertThat(response.licensePlate()).isEqualTo(violationRequest.licensePlate());
        assertThat(response.equipmentId()).isEqualTo(violationRequest.equipmentId());
        assertThat(response.measuredSpeed()).isEqualTo(violationRequest.measuredSpeed());
        assertThat(response.speedLimit()).isEqualTo(violationRequest.speedLimit());
    }

    @Test
    @DisplayName("Should map violation without violation detail")
    void toDto_WithoutViolationDetail_ShouldMapEntityWithoutError() {
        Violation simpleViolation = new Violation();
        simpleViolation.setId(2L);
        simpleViolation.setLicensePlate("DEF5678");
        simpleViolation.setEquipmentId("EQ-003");
        simpleViolation.setMeasuredSpeed(50);
        simpleViolation.setConsideredSpeed(43);
        simpleViolation.setSpeedLimit(60);
        simpleViolation.setExcessPercentage(0.0);
        simpleViolation.setHasViolation(false);
        simpleViolation.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        simpleViolation.setViolationDetail(null);

        ViolationResponse result = violationMapper.toDto(simpleViolation);

        assertThat(result)
                .isNotNull()
                .hasFieldOrPropertyWithValue("licensePlate", "DEF5678")
                .hasFieldOrPropertyWithValue("hasViolation", false);
        assertThat(result.violation()).isNull();
    }

    @Test
    @DisplayName("Should handle multiple mappings independently")
    void toDto_MultipleViolations_ShouldMapIndependently() {
        Violation violation2 = new Violation();
        violation2.setId(2L);
        violation2.setLicensePlate("XYZ9999");
        violation2.setEquipmentId("EQ-004");
        violation2.setMeasuredSpeed(110);
        violation2.setConsideredSpeed(102);
        violation2.setSpeedLimit(80);
        violation2.setExcessPercentage(27.5);
        violation2.setHasViolation(true);
        violation2.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));

        ViolationDetail detail2 = new ViolationDetail();
        detail2.setSeverity("VERY_SERIOUS");
        detail2.setCtbCode("218-III");
        violation2.setViolationDetail(detail2);

        ViolationResponse result1 = violationMapper.toDto(violation);
        ViolationResponse result2 = violationMapper.toDto(violation2);

        assertThat(result1.licensePlate()).isEqualTo("ABC1234");
        assertThat(result2.licensePlate()).isEqualTo("XYZ9999");
        assertThat(result1.violation().severity()).isEqualTo("SERIOUS");
        assertThat(result2.violation().severity()).isEqualTo("VERY_SERIOUS");
    }
}

