package io.gitub.eliaspinheiropereira.speed_violation_service.dto.response;

import java.sql.Timestamp;

public record ViolationWithInfractionResponse(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        boolean hasViolation,
        ViolationDetailResponse violation,
        Timestamp processedAt
) {
}
