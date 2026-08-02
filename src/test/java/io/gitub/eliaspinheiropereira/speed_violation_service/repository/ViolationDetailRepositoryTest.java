package io.gitub.eliaspinheiropereira.speed_violation_service.repository;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
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
@DisplayName("ViolationDetailRepository Tests")
class ViolationDetailRepositoryTest {

    @Autowired
    private ViolationDetailRepository violationDetailRepository;

    @Autowired
    private ViolationRepository violationRepository;

    private ViolationDetail violationDetail;
    private Violation violation;

    @BeforeEach
    void setUp() {
        violationDetailRepository.deleteAll();
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

        violation = violationRepository.save(violation);

        violationDetail = new ViolationDetail();
        violationDetail.setSeverity("GRAVE");
        violationDetail.setCtbCode("221-A");
        violationDetail.setViolation(violation);
    }

    @Test
    @DisplayName("Should insert ViolationDetail and verify all fields")
    void testInsert() {
        ViolationDetail saved = violationDetailRepository.save(violationDetail);

        assertThat(saved)
                .isNotNull()
                .hasFieldOrPropertyWithValue("severity", "GRAVE")
                .hasFieldOrPropertyWithValue("ctbCode", "221-A")
                .hasFieldOrPropertyWithValue("violation", violation);

        assertThat(saved.getId()).isPositive();
    }

    @Test
    @DisplayName("Should find ViolationDetail by ID and verify all fields")
    void testFindById() {
        ViolationDetail saved = violationDetailRepository.save(violationDetail);

        var found = violationDetailRepository.findById(saved.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(vd -> {
                    assertThat(vd.getSeverity()).isEqualTo("GRAVE");
                    assertThat(vd.getCtbCode()).isEqualTo("221-A");
                    assertThat(vd.getViolation()).isEqualTo(violation);
                });
    }

    @Test
    @DisplayName("Should update ViolationDetail and verify persistence")
    void testUpdate() {
        ViolationDetail saved = violationDetailRepository.save(violationDetail);

        saved.setSeverity("LEVE");
        saved.setCtbCode("221-B");

        ViolationDetail updated = violationDetailRepository.save(saved);

        assertThat(updated)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", saved.getId())
                .hasFieldOrPropertyWithValue("severity", "LEVE")
                .hasFieldOrPropertyWithValue("ctbCode", "221-B");

        var found = violationDetailRepository.findById(updated.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(vd -> {
                    assertThat(vd.getSeverity()).isEqualTo("LEVE");
                    assertThat(vd.getCtbCode()).isEqualTo("221-B");
                });
    }

    @Test
    @DisplayName("Should delete ViolationDetail and verify it no longer exists")
    void testDelete() {
        ViolationDetail saved = violationDetailRepository.save(violationDetail);
        long id = saved.getId();

        violationDetailRepository.deleteById(id);

        var found = violationDetailRepository.findById(id);

        assertThat(found)
                .isEmpty();
    }
}