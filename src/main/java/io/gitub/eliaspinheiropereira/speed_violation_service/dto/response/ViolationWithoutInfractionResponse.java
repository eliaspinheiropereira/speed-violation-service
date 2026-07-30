package io.gitub.eliaspinheiropereira.speed_violation_service.dto.response;

import java.sql.Timestamp;

public record ViolationWithoutInfractionResponse(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        boolean hasViolation,
        ViolationDetailResponse violation, // será null
        Timestamp processedAt
) {
}
