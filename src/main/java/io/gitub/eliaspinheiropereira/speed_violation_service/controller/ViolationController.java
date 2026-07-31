package io.gitub.eliaspinheiropereira.speed_violation_service.controller;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import io.gitub.eliaspinheiropereira.speed_violation_service.service.ViolationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/violations/evaluate")
@RequiredArgsConstructor
@Slf4j
public class ViolationController {

    private final ViolationService violationService;

    @PostMapping
    public ResponseEntity<Void> evaluateViolation(
            @RequestHeader("x-origin") Origin origin,
            @Valid @RequestBody ViolationRequest violationRequest
    ) {
        log.info("POST -> /api/v1/violations/evaluate");
        this.violationService.evaluateViolation(origin, violationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
