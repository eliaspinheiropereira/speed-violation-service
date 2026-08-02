package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationDetailResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.HeaderValidationException;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.ViolationNotFoundException;
import io.gitub.eliaspinheiropereira.speed_violation_service.mapper.ViolationMapper;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import io.gitub.eliaspinheiropereira.speed_violation_service.repository.ViolationRepository;
import io.gitub.eliaspinheiropereira.speed_violation_service.validation.HeaderValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViolationService Tests")
class ViolationServiceTest {

    @Mock
    private ViolationRepository violationRepository;

    @Mock
    private ViolationMapper violationMapper;

    @Mock
    private HeaderValidation headerValidation;

    @Mock
    private ViolationCalculatorService violationCalculatorService;

    @InjectMocks
    private ViolationService violationService;

    private ViolationRequest violationRequest;
    private Violation violation;
    private ViolationResponse violationResponse;

    @BeforeEach
    void setUp() {
        violationRequest = new ViolationRequest(
                "ABC1234",
                85,
                60,
                "EQ-001",
                Timestamp.valueOf(LocalDateTime.now())
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
        violation.setCaptureTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        violation.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        violation.setOrigin(Origin.FIXED);

        violationResponse = new ViolationResponse(
                "ABC1234",
                "EQ-001",
                85,
                82,
                60,
                36.7,
                true,
                new ViolationDetailResponse("GRAVE", "221-A"),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }


    @Test
    @DisplayName("Should evaluate violation with valid data successfully")
    void evaluateViolation_WithValidData_ShouldSucceed() {
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(headerValidation).validation(Origin.FIXED.name());
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        assertThatNoException()
                .isThrownBy(() -> violationService.evaluateViolation(Origin.FIXED, violationRequest));

        verify(headerValidation, times(1)).validation(Origin.FIXED.name());
        verify(violationMapper, times(1)).toEntity(violationRequest);
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
        verify(violationRepository, times(1)).save(violation);
    }

    @Test
    @DisplayName("Should throw HeaderValidationException when header is invalid")
    void evaluateViolation_WithInvalidHeader_ShouldThrowHeaderValidationException() {
        doThrow(new HeaderValidationException("Invalid header"))
                .when(headerValidation).validation(Origin.MOBILE.name());

        assertThatThrownBy(() -> violationService.evaluateViolation(Origin.MOBILE, violationRequest))
                .isInstanceOf(HeaderValidationException.class)
                .hasMessage("Invalid header");

        verify(headerValidation, times(1)).validation(Origin.MOBILE.name());
        verify(violationMapper, never()).toEntity(any());
        verify(violationCalculatorService, never()).calculateViolation(any());
        verify(violationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should verify that violation calculation is called")
    void evaluateViolation_VerifyCalculationIsCalled() {
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(headerValidation).validation(Origin.FIXED.name());
        doNothing().when(violationCalculatorService).calculateViolation(any());
        when(violationRepository.save(any())).thenReturn(violation);

        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        verify(violationCalculatorService, times(1)).calculateViolation(argThat(v ->
                v.getLicensePlate().equals("ABC1234") && v.getMeasuredSpeed() == 85
        ));
    }

    @Test
    @DisplayName("Should verify that repository save is called")
    void evaluateViolation_VerifyRepositorySaveIsCalled() {
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(headerValidation).validation(Origin.FIXED.name());
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        verify(violationRepository, times(1)).save(violation);
    }

    @Test
    @DisplayName("Should validate correctly with multiple origins")
    void evaluateViolation_WithMultipleOrigins_ShouldValidateCorrectly() {
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(headerValidation).validation(anyString());
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        violationService.evaluateViolation(Origin.FIXED, violationRequest);
        verify(headerValidation).validation(Origin.FIXED.name());

        reset(headerValidation, violationRepository);

        when(violationRepository.save(violation)).thenReturn(violation);
        doNothing().when(headerValidation).validation(Origin.MOBILE.name());
        violationService.evaluateViolation(Origin.MOBILE, violationRequest);
        verify(headerValidation).validation(Origin.MOBILE.name());
    }


    @Test
    @DisplayName("Should return ViolationResponse when license plate exists")
    void getViolation_WithExistingLicensePlate_ShouldReturnViolationResponse() {
        String licensePlate = "ABC1234";
        when(violationRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation)).thenReturn(violationResponse);

        ViolationResponse result = violationService.getViolation(licensePlate);

        assertThat(result)
                .isNotNull()
                .isEqualTo(violationResponse);

        verify(violationRepository, times(1)).findByLicensePlate(licensePlate);
        verify(violationMapper, times(1)).toDto(violation);
    }

    @Test
    @DisplayName("Should throw ViolationNotFoundException when license plate does not exist")
    void getViolation_WithNonExistentLicensePlate_ShouldThrowViolationNotFoundException() {
        String licensePlate = "XYZ9999";
        when(violationRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> violationService.getViolation(licensePlate))
                .isInstanceOf(ViolationNotFoundException.class)
                .hasMessageContaining("Violation not found for license plate: " + licensePlate);

        verify(violationRepository, times(1)).findByLicensePlate(licensePlate);
        verify(violationMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should verify all response fields are correct")
    void getViolation_VerifyResponseFields() {
        String licensePlate = "ABC1234";
        when(violationRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation)).thenReturn(violationResponse);

        ViolationResponse result = violationService.getViolation(licensePlate);

        assertThat(result)
                .hasFieldOrPropertyWithValue("licensePlate", "ABC1234")
                .hasFieldOrPropertyWithValue("equipmentId", "EQ-001")
                .hasFieldOrPropertyWithValue("measuredSpeed", 85)
                .hasFieldOrPropertyWithValue("consideredSpeed", 82)
                .hasFieldOrPropertyWithValue("speedLimit", 60)
                .hasFieldOrPropertyWithValue("excessPercentage", 36.7)
                .hasFieldOrPropertyWithValue("hasViolation", true);
    }

    @Test
    @DisplayName("Should not call mapper when result is empty")
    void getViolation_WithEmptyResult_ShouldNotCallMapper() {
        String licensePlate = "NOTFOUND";
        when(violationRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> violationService.getViolation(licensePlate))
                .isInstanceOf(ViolationNotFoundException.class);

        verify(violationMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should verify mapper is called with correct violation")
    void getViolation_VerifyMapperIsCalledWithCorrectViolation() {
        String licensePlate = "ABC1234";
        when(violationRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation)).thenReturn(violationResponse);

        violationService.getViolation(licensePlate);

        verify(violationMapper, times(1)).toDto(argThat(v ->
                v.getLicensePlate().equals("ABC1234") &&
                v.getMeasuredSpeed() == 85 &&
                v.getSpeedLimit() == 60
        ));
    }

    @Test
    @DisplayName("Should query correctly with different license plates")
    void getViolation_WithDifferentLicensePlates_ShouldQueryCorrectly() {
        String licensePlate1 = "ABC1234";
        String licensePlate2 = "DEF5678";

        when(violationRepository.findByLicensePlate(licensePlate1)).thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation)).thenReturn(violationResponse);

        ViolationResponse result1 = violationService.getViolation(licensePlate1);

        assertThat(result1).isNotNull();
        verify(violationRepository).findByLicensePlate(licensePlate1);

        reset(violationRepository);
        when(violationRepository.findByLicensePlate(licensePlate2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> violationService.getViolation(licensePlate2))
                .isInstanceOf(ViolationNotFoundException.class);

        verify(violationRepository).findByLicensePlate(licensePlate2);
    }

    @Test
    @DisplayName("Should verify exception message contains license plate")
    void getViolation_VerifyExceptionMessage() {
        String licensePlate = "NOTEXISTS";
        when(violationRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> violationService.getViolation(licensePlate))
                .isInstanceOf(ViolationNotFoundException.class)
                .hasMessageContaining("Violation not found for license plate")
                .hasMessageContaining(licensePlate);
    }

}