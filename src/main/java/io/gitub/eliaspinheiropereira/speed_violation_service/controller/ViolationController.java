package io.gitub.eliaspinheiropereira.speed_violation_service.controller;

import io.gitub.eliaspinheiropereira.speed_violation_service.dto.request.ViolationRequest;
import io.gitub.eliaspinheiropereira.speed_violation_service.dto.response.ViolationResponse;
import io.gitub.eliaspinheiropereira.speed_violation_service.model.enums.Origin;
import io.gitub.eliaspinheiropereira.speed_violation_service.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/violations")
@RequiredArgsConstructor
@Slf4j
public class ViolationController {

    private final ViolationService violationService;


    @PostMapping("/evaluate")
    @Operation(summary = "evaluate violation", description = "Assesses a speeding violation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "saving violation"),
            @ApiResponse(responseCode = "400", description = "invalid data", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "internal server error", content = @Content(schema = @Schema()))})
    public ResponseEntity<Void> evaluateViolation(
            @RequestHeader("x-origin") Origin origin,
            @Valid @RequestBody ViolationRequest violationRequest
    ) {
        log.info("POST -> /api/v1/violations/evaluate");
        this.violationService.evaluateViolation(origin, violationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "search for violation", description = "search violation by license plate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "violation found", content = @Content(schema = @Schema(implementation = ViolationResponse.class))),
            @ApiResponse(responseCode = "404", description = "violation not found", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "internal server error", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<ViolationResponse> getViolation(
            @RequestParam("licensePlate") String licensePlate
    ) {
        log.info("GET -> /api/v1/violations?licensePlate={}", licensePlate);
        ViolationResponse violationResponse = this.violationService.getViolation(licensePlate);
        return ResponseEntity.ok(violationResponse);
    }
}
