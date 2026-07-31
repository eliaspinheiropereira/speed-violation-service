package io.gitub.eliaspinheiropereira.speed_violation_service.dto.request;

import jakarta.validation.constraints.*;

import java.sql.Timestamp;

public record ViolationRequest(
        @NotBlank(message = "License plate is required")
        @Pattern(
                regexp = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$",
                message = "Invalid license plate format"
        )
        String licensePlate,

        @NotNull(message = "Measured speed is required")
        @Positive(message = "Measured speed must be greater than zero")
        Integer measuredSpeed,

        @NotNull(message = "Speed limit is required")
        @Positive(message = "Speed limit must be greater than zero")
        Integer speedLimit,

        @NotBlank(message = "Equipment ID is required")
        String equipmentId,

        @NotNull(message = "Capture timestamp is required")
        @PastOrPresent(message = "Capture timestamp cannot be in the future")
        Timestamp captureTimestamp
) {
}
