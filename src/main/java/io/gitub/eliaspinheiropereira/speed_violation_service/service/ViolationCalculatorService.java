package io.gitub.eliaspinheiropereira.speed_violation_service.service;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.CtbCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@Slf4j
public class ViolationCalculatorService {

    private int calculateConsideredSpeed(int measuredSpeed) {
        int consideredSpeed = 0;
        if (measuredSpeed <= 100) {
            consideredSpeed = measuredSpeed - 7;
        } else {
            double reduced = measuredSpeed - (measuredSpeed * 0.07);
            consideredSpeed = (int) Math.floor(reduced);
        }
        return consideredSpeed;
    }

    private boolean checkViolation(int consideredSpeed, int speedLimit) {
        return consideredSpeed > speedLimit;
    }

    private double percentageExcess(int consideredSpeed, int speedLimit) {
        double percentageExcess = ((double) (consideredSpeed - speedLimit) / speedLimit) * 100;
        return Math.round(percentageExcess * 100.0) / 100.0;
    }

    private ViolationDetail determineSeverity(double percentageExcess, Violation violation) {
        if (percentageExcess <= 20) {
            return new ViolationDetail(CtbCode.MEDIUM.toString(), CtbCode.MEDIUM.getCode(), violation);
        } else if (percentageExcess <= 50) {
            return new ViolationDetail(CtbCode.SERIOUS.toString(), CtbCode.SERIOUS.getCode(), violation);
        } else {
            return new ViolationDetail(CtbCode.VERY_SERIOUS.toString(), CtbCode.VERY_SERIOUS.getCode(), violation);
        }
    }

    public void calculateViolation(Violation violation) {
        int consideredSpeed = this.calculateConsideredSpeed(violation.getMeasuredSpeed());
        boolean checkViolation = this.checkViolation(consideredSpeed, violation.getSpeedLimit());
        double percentageExcess = this.percentageExcess(consideredSpeed, violation.getSpeedLimit());
        ViolationDetail violationDetail = this.determineSeverity(percentageExcess, violation);

        if (checkViolation) {
            violation.setConsideredSpeed(consideredSpeed);
            violation.setHasViolation(true);
            violation.setExcessPercentage(percentageExcess);
            violation.setViolationDetail(violationDetail);
            violation.setProcessedAt(new Timestamp(System.currentTimeMillis()));
        } else{
            violation.setConsideredSpeed(consideredSpeed);
            violation.setProcessedAt(new Timestamp(System.currentTimeMillis()));
        }
    }
}
