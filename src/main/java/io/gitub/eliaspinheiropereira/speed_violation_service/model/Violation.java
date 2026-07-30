package io.gitub.eliaspinheiropereira.speed_violation_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String licensePlate;
    private String equipamentId;
    private int measuredSpeed;
    private int consideredSpeed;
    private int speedLimit;
    private double excessPercentage;
    private boolean hasViolation;
    private Timestamp captureTimestamp;
    private Timestamp processedAt;

    @OneToOne(mappedBy = "violation", cascade = CascadeType.ALL, orphanRemoval = true)
    private ViolationDetail violationDetail;
}
