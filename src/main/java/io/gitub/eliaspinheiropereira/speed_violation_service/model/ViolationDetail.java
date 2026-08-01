package io.gitub.eliaspinheiropereira.speed_violation_service.model;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.CtbCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ViolationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String severity;
    private String ctbCode;

    @JoinColumn
    @OneToOne
    private Violation violation;

    public ViolationDetail(String severity, String ctbCode, Violation violation) {
        this.severity = severity;
        this.ctbCode = ctbCode;
        this.violation = violation;
    }
}
