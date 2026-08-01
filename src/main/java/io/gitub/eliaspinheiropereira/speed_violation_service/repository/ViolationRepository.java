package io.gitub.eliaspinheiropereira.speed_violation_service.repository;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, Long> {
    Optional<Violation> findByLicensePlate(String licensePlate);
}
