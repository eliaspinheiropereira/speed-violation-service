package io.gitub.eliaspinheiropereira.speed_violation_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public record ViolationResponse(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        boolean hasViolation,
        ViolationDetailResponse violation,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Sao_Paulo")
        Timestamp processedAt
) {
}
