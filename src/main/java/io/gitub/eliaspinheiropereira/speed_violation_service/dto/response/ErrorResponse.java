package io.gitub.eliaspinheiropereira.speed_violation_service.dto.response;

import java.util.List;

public record ErrorResponse(
        Integer status,
        String message,
        List<ErrorFieldResponse> errors
) {
}
