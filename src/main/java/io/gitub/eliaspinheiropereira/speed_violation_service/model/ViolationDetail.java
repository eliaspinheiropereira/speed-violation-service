package io.gitub.eliaspinheiropereira.speed_violation_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;

@Entity
@Data
public class ViolationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String severity;
    private String ctbCode;

    @JoinColumn
    @OneToOne
    private Violation violation;
}
