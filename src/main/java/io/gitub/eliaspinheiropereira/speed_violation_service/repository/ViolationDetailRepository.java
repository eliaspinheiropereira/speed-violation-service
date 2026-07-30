package io.gitub.eliaspinheiropereira.speed_violation_service.repository;

import io.gitub.eliaspinheiropereira.speed_violation_service.model.ViolationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViolationDetailRepository extends JpaRepository<ViolationDetail, Long> {
}
