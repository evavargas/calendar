package app.kairo.plans;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlanRepository
    extends JpaRepository<PlanEntity, UUID>, JpaSpecificationExecutor<PlanEntity> {

  Optional<PlanEntity> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByTypeIdAndUserId(UUID typeId, UUID userId);

  default List<PlanEntity> search(
      UUID userId, PlanStatus status, UUID typeId, Instant from, Instant to) {
    return findAll(filter(userId, status, typeId, from, to));
  }

  /**
   * Builds predicates in Java so Postgres never receives untyped NULL bind parameters (which
   * fail with {@code could not determine data type of parameter $N}).
   */
  static Specification<PlanEntity> filter(
      UUID userId, PlanStatus status, UUID typeId, Instant from, Instant to) {
    return (root, query, cb) -> {
      List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("userId"), userId));
      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (typeId != null) {
        predicates.add(cb.equal(root.get("typeId"), typeId));
      }
      if (from != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("startsAt"), from));
      }
      if (to != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("startsAt"), to));
      }
      if (query != null) {
        query.orderBy(cb.asc(root.get("startsAt")));
      }
      return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
  }
}
