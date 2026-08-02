package io.gitub.eliaspinheiropereira.speed_violation_service.model;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String licensePlate;
    private String equipmentId;
    private Integer measuredSpeed;
    private Integer consideredSpeed;
    private Integer speedLimit;
    private Double excessPercentage;
    private Boolean hasViolation;
    private Timestamp captureTimestamp;
    private Timestamp processedAt;
    private Origin origin;

    @OneToOne(mappedBy = "violation", cascade = CascadeType.ALL, orphanRemoval = true)
    private ViolationDetail violationDetail;
}
