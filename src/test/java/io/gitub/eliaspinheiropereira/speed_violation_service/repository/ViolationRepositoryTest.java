package io.gitub.eliaspinheiropereira.speed_violation_service.repository;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ViolationRepository Tests")
class ViolationRepositoryTest {

    @Autowired
    private ViolationRepository violationRepository;

    private Violation violation;

    @BeforeEach
    void setUp() {
        violationRepository.deleteAll();
        
        violation = new Violation();
        violation.setLicensePlate("ABC-1234");
        violation.setEquipmentId("EQ-001");
        violation.setMeasuredSpeed(85);
        violation.setConsideredSpeed(82);
        violation.setSpeedLimit(60);
        violation.setExcessPercentage(36.7);
        violation.setHasViolation(true);
        violation.setCaptureTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        violation.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        violation.setOrigin(Origin.FIXED);
    }

    @Test
    @DisplayName("Should insert Violation and verify all fields")
    void testInsert() {
        Violation saved = violationRepository.save(violation);

        assertThat(saved)
                .isNotNull()
                .hasFieldOrPropertyWithValue("licensePlate", "ABC-1234")
                .hasFieldOrPropertyWithValue("equipmentId", "EQ-001")
                .hasFieldOrPropertyWithValue("measuredSpeed", 85)
                .hasFieldOrPropertyWithValue("consideredSpeed", 82)
                .hasFieldOrPropertyWithValue("speedLimit", 60)
                .hasFieldOrPropertyWithValue("excessPercentage", 36.7)
                .hasFieldOrPropertyWithValue("hasViolation", true)
                .hasFieldOrPropertyWithValue("origin", Origin.FIXED);
        
        assertThat(saved.getId()).isPositive();
    }

    @Test
    @DisplayName("Should find Violation by ID and verify all fields")
    void testFindById() {
        Violation saved = violationRepository.save(violation);

        var found = violationRepository.findById(saved.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(v -> {
                    assertThat(v.getLicensePlate()).isEqualTo("ABC-1234");
                    assertThat(v.getEquipmentId()).isEqualTo("EQ-001");
                    assertThat(v.getMeasuredSpeed()).isEqualTo(85);
                    assertThat(v.getConsideredSpeed()).isEqualTo(82);
                    assertThat(v.getSpeedLimit()).isEqualTo(60);
                    assertThat(v.getExcessPercentage()).isEqualTo(36.7);
                    assertThat(v.isHasViolation()).isTrue();
                    assertThat(v.getOrigin()).isEqualTo(Origin.FIXED);
                });
    }

    @Test
    @DisplayName("Should update Violation and verify persistence")
    void testUpdate() {
        Violation saved = violationRepository.save(violation);
        
        saved.setMeasuredSpeed(95);
        saved.setExcessPercentage(58.3);
        saved.setSpeedLimit(65);
        saved.setLicensePlate("XYZ-9999");
        
        Violation updated = violationRepository.save(saved);

        assertThat(updated)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", saved.getId())
                .hasFieldOrPropertyWithValue("licensePlate", "XYZ-9999")
                .hasFieldOrPropertyWithValue("measuredSpeed", 95)
                .hasFieldOrPropertyWithValue("excessPercentage", 58.3)
                .hasFieldOrPropertyWithValue("speedLimit", 65);

        var found = violationRepository.findById(updated.getId());
        
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(v -> {
                    assertThat(v.getMeasuredSpeed()).isEqualTo(95);
                    assertThat(v.getLicensePlate()).isEqualTo("XYZ-9999");
                    assertThat(v.getSpeedLimit()).isEqualTo(65);
                });
    }

    @Test
    @DisplayName("Should delete Violation and verify it no longer exists")
    void testDelete() {
        Violation saved = violationRepository.save(violation);
        long id = saved.getId();

        violationRepository.deleteById(id);

        var found = violationRepository.findById(id);

        assertThat(found)
                .isEmpty();
    }
}
