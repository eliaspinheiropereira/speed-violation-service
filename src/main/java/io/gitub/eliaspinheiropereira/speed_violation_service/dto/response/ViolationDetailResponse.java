package io.gitub.eliaspinheiropereira.speed_violation_service.dto.response;

public record ViolationDetailResponse(
        String severity,
        String ctbCode
) {
}
