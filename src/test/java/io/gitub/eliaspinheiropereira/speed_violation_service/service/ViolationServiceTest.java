package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.HeaderValidationException;
import io.gitub.eliaspinheiropereira.speed_violation_service.exception.ViolationNotFoundException;
import io.gitub.eliaspinheiropereira.speed_violation_service.mapper.ViolationMapper;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.CtbCode;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import io.gitub.eliaspinheiropereira.speed_violation_service.repository.ViolationRepository;
import io.gitub.eliaspinheiropereira.speed_violation_service.validation.HeaderValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
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
        // Setup ViolationRequest
        violationRequest = new ViolationRequest(
                "ABC-1234",
                85,
                60,
                "EQ-001",
                Timestamp.valueOf(LocalDateTime.now())
        );

        // Setup Violation entity
        violation = new Violation();
        violation.setId(1L);
        violation.setLicensePlate("ABC-1234");
        violation.setEquipmentId("EQ-001");
        violation.setMeasuredSpeed(85);
        violation.setConsideredSpeed(78);
        violation.setSpeedLimit(60);
        violation.setExcessPercentage(30.0);
        violation.setHasViolation(true);
        violation.setCaptureTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        violation.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        violation.setOrigin(Origin.FIXED);

        // Setup ViolationDetail
        ViolationDetail violationDetail = new ViolationDetail(
                CtbCode.SERIOUS.toString(),
                CtbCode.SERIOUS.getCode(),
                violation
        );
        violation.setViolationDetail(violationDetail);

        // Setup ViolationResponse
        violationResponse = new ViolationResponse(
                "ABC-1234",
                "EQ-001",
                85,
                78,
                60,
                30.0,
                true,
                null,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Should evaluate violation successfully with valid header")
    void testEvaluateViolationSuccess() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.empty());
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert
        verify(headerValidation, times(1)).validation("FIXED");
        verify(violationRepository, times(1)).findByLicensePlate(violationRequest.licensePlate());
        verify(violationMapper, times(1)).toEntity(violationRequest);
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
        verify(violationRepository, times(1)).save(violation);

        verifyNoMoreInteractions(headerValidation, violationCalculatorService);
    }

    @Test
    @DisplayName("Should throw HeaderValidationException when origin header is invalid")
    void testEvaluateViolationWithInvalidHeader() {
        // Arrange
        doThrow(new HeaderValidationException("Origin header is missing or empty"))
                .when(headerValidation).validation(anyString());

        // Act & Assert
        assertThatThrownBy(() -> violationService.evaluateViolation(Origin.FIXED, violationRequest))
                .isInstanceOf(HeaderValidationException.class)
                .hasMessageContaining("Origin header is missing or empty");

        verify(headerValidation, times(1)).validation("FIXED");
        verifyNoInteractions(violationMapper, violationCalculatorService, violationRepository);
    }

    @Test
    @DisplayName("Should call calculator service to process violation")
    void testEvaluateViolationCallsCalculatorService() {
        // Arrange
        doNothing().when(headerValidation).validation("MOBILE");
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.MOBILE, violationRequest);

        // Assert
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
    }

    @Test
    @DisplayName("Should save violation to repository after calculation")
    void testEvaluateViolationSavesViolation() {
        // Arrange
        doNothing().when(headerValidation).validation("HANDHELD");
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.HANDHELD, violationRequest);

        // Assert
        verify(violationRepository, times(1)).save(violation);

        assertThat(violation)
                .isNotNull();
    }

    @Test
    @DisplayName("Should get violation by license plate successfully")
    void testGetViolationSuccess() {
        // Arrange
        when(violationRepository.findByLicensePlate("ABC-1234"))
                .thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation))
                .thenReturn(violationResponse);

        // Act
        ViolationResponse result = violationService.getViolation("ABC-1234");

        // Assert
        assertThat(result)
                .isNotNull()
                .isEqualTo(violationResponse);

        verify(violationRepository, times(1)).findByLicensePlate("ABC-1234");
        verify(violationMapper, times(1)).toDto(violation);
    }

    @Test
    @DisplayName("Should throw ViolationNotFoundException when violation does not exist")
    void testGetViolationNotFound() {
        // Arrange
        when(violationRepository.findByLicensePlate("XYZ-9999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> violationService.getViolation("XYZ-9999"))
                .isInstanceOf(ViolationNotFoundException.class)
                .hasMessageContaining("Violation not found for license plate: XYZ-9999");

        verify(violationRepository, times(1)).findByLicensePlate("XYZ-9999");
        verifyNoInteractions(violationMapper);
    }

    @Test
    @DisplayName("Should return correct ViolationResponse when violation exists")
    void testGetViolationReturnsCorrectData() {
        // Arrange
        String licensePlate = "ABC-1234";
        when(violationRepository.findByLicensePlate(licensePlate))
                .thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation))
                .thenReturn(violationResponse);

        // Act
        ViolationResponse result = violationService.getViolation(licensePlate);

        // Assert
        assertThat(result)
                .isNotNull()
                .hasFieldOrPropertyWithValue("licensePlate", "ABC-1234")
                .hasFieldOrPropertyWithValue("equipmentId", "EQ-001")
                .hasFieldOrPropertyWithValue("measuredSpeed", 85)
                .hasFieldOrPropertyWithValue("consideredSpeed", 78)
                .hasFieldOrPropertyWithValue("speedLimit", 60)
                .hasFieldOrPropertyWithValue("excessPercentage", 30.0)
                .hasFieldOrPropertyWithValue("hasViolation", true);
    }

    @Test
    @DisplayName("Should use repository to find violation by license plate")
    void testGetViolationQueriesRepository() {
        // Arrange
        String licensePlate = "ABC-1234";
        when(violationRepository.findByLicensePlate(licensePlate))
                .thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation))
                .thenReturn(violationResponse);

        // Act
        violationService.getViolation(licensePlate);

        // Assert
        verify(violationRepository, times(1)).findByLicensePlate(licensePlate);
    }

    @Test
    @DisplayName("Should map violation entity to response DTO")
    void testGetViolationMapperIsUsed() {
        // Arrange
        when(violationRepository.findByLicensePlate("ABC-1234"))
                .thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation))
                .thenReturn(violationResponse);

        // Act
        violationService.getViolation("ABC-1234");

        // Assert
        verify(violationMapper, times(1)).toDto(violation);
    }

    @Test
    @DisplayName("Should map request DTO to violation entity during evaluation")
    void testEvaluateViolationMapperIsUsed() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert
        verify(violationMapper, times(1)).toEntity(violationRequest);
    }

    @Test
    @DisplayName("Should validate header before processing violation")
    void testEvaluateViolationValidatesHeaderFirst() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert
        InOrder inOrder = inOrder(headerValidation, violationMapper, violationCalculatorService);
        inOrder.verify(headerValidation).validation("FIXED");
        inOrder.verify(violationMapper).toEntity(violationRequest);
        inOrder.verify(violationCalculatorService).calculateViolation(violation);
    }

    @Test
    @DisplayName("Should not save violation if header validation fails")
    void testEvaluateViolationDoesNotSaveIfHeaderValidationFails() {
        // Arrange
        doThrow(new HeaderValidationException("Origin header is missing or empty"))
                .when(headerValidation).validation(anyString());

        // Act & Assert
        assertThatThrownBy(() -> violationService.evaluateViolation(Origin.FIXED, violationRequest))
                .isInstanceOf(HeaderValidationException.class);

        verifyNoInteractions(violationRepository);
    }

    @Test
    @DisplayName("Should handle different origins correctly during evaluation")
    void testEvaluateViolationWithDifferentOrigins() {
        // Arrange
        doNothing().when(headerValidation).validation(anyString());
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.MOBILE, violationRequest);
        violationService.evaluateViolation(Origin.HANDHELD, violationRequest);

        // Assert
        verify(headerValidation, times(2)).validation(anyString());
        verify(headerValidation).validation("MOBILE");
        verify(headerValidation).validation("HANDHELD");
    }

    @Test
    @DisplayName("Should include processed timestamp in violation response")
    void testGetViolationIncludesProcessedTimestamp() {
        // Arrange
        when(violationRepository.findByLicensePlate("ABC-1234"))
                .thenReturn(Optional.of(violation));
        when(violationMapper.toDto(violation))
                .thenReturn(violationResponse);

        // Act
        ViolationResponse result = violationService.getViolation("ABC-1234");

        // Assert
        assertThat(result.processedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("Should throw exception with license plate in message when violation not found")
    void testGetViolationExceptionMessageContainsLicensePlate() {
        // Arrange
        String licensePlate = "XYZ-5678";
        when(violationRepository.findByLicensePlate(licensePlate))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> violationService.getViolation(licensePlate))
                .isInstanceOf(ViolationNotFoundException.class)
                .hasMessageContaining("XYZ-5678");
    }

    @Test
    @DisplayName("Should create new violation when violation does not exist")
    void testEvaluateViolationCreatesNewViolation() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.empty());
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert
        verify(violationRepository, times(1)).findByLicensePlate(violationRequest.licensePlate());
        verify(violationMapper, times(1)).toEntity(violationRequest);
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
        verify(violationRepository, times(1)).save(violation);
    }

    @Test
    @DisplayName("Should update existing violation when violation already exists")
    void testEvaluateViolationUpdatesExistingViolation() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.of(violation));
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert
        verify(violationRepository, times(1)).findByLicensePlate(violationRequest.licensePlate());
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
        verify(violationRepository, times(1)).save(violation);

        // Verify mapper is NOT called (since we're updating, not creating)
        verifyNoInteractions(violationMapper);
    }

    @Test
    @DisplayName("Should update violation fields when updating existing violation")
    void testUpdateViolationUpdatesAllFields() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.of(violation));
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        int newMeasuredSpeed = 95;
        int newSpeedLimit = 70;
        String newEquipmentId = "EQ-002";
        Timestamp newTimestamp = Timestamp.valueOf(LocalDateTime.now().plusHours(1));

        ViolationRequest updatedRequest = new ViolationRequest(
                "ABC-1234",
                newMeasuredSpeed,
                newSpeedLimit,
                newEquipmentId,
                newTimestamp
        );

        // Act
        violationService.evaluateViolation(Origin.FIXED, updatedRequest);

        // Assert - Verify the violation object was modified
        assertThat(violation.getMeasuredSpeed()).isEqualTo(newMeasuredSpeed);
        assertThat(violation.getSpeedLimit()).isEqualTo(newSpeedLimit);
        assertThat(violation.getEquipmentId()).isEqualTo(newEquipmentId);
        assertThat(violation.getCaptureTimestamp()).isEqualTo(newTimestamp);
    }

    @Test
    @DisplayName("Should reset violation detail when updating existing violation")
    void testUpdateViolationResetsViolationDetail() {
        // Arrange
        // Setup existing violation with detail
        ViolationDetail existingDetail = new ViolationDetail(
                CtbCode.SERIOUS.toString(),
                CtbCode.SERIOUS.getCode(),
                violation
        );
        violation.setViolationDetail(existingDetail);
        violation.setHasViolation(true);
        violation.setConsideredSpeed(88);
        violation.setExcessPercentage(46.67);

        doNothing().when(headerValidation).validation("FIXED");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.of(violation));
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert - Verify fields were reset before recalculation
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
        verify(violationRepository, times(1)).save(violation);
    }

    @Test
    @DisplayName("Should recalculate violation when updating existing violation")
    void testUpdateViolationRecalculatesViolation() {
        // Arrange
        doNothing().when(headerValidation).validation("MOBILE");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.of(violation));
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.MOBILE, violationRequest);

        // Assert
        verify(violationCalculatorService, times(1)).calculateViolation(violation);
    }

    @Test
    @DisplayName("Should save violation after updating fields")
    void testUpdateViolationSavesAfterUpdate() {
        // Arrange
        doNothing().when(headerValidation).validation("HANDHELD");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.of(violation));
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.HANDHELD, violationRequest);

        // Assert
        verify(violationRepository, times(1)).save(violation);
    }

    @Test
    @DisplayName("Should check for existing violation before creating new one")
    void testEvaluateViolationChecksForExistence() {
        // Arrange
        doNothing().when(headerValidation).validation("FIXED");
        when(violationRepository.findByLicensePlate(violationRequest.licensePlate()))
                .thenReturn(Optional.empty());
        when(violationMapper.toEntity(violationRequest)).thenReturn(violation);
        doNothing().when(violationCalculatorService).calculateViolation(violation);
        when(violationRepository.save(violation)).thenReturn(violation);

        // Act
        violationService.evaluateViolation(Origin.FIXED, violationRequest);

        // Assert
        verify(violationRepository, times(1)).findByLicensePlate(violationRequest.licensePlate());
    }
}
