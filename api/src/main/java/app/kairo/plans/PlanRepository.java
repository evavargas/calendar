package app.kairo.plans;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<PlanEntity, UUID> {

  Optional<PlanEntity> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByTypeIdAndUserId(UUID typeId, UUID userId);

  @Query(
      """
      SELECT p FROM PlanEntity p
      WHERE p.userId = :userId
        AND (:status IS NULL OR p.status = :status)
        AND (:typeId IS NULL OR p.typeId = :typeId)
        AND (:from IS NULL OR p.startsAt >= :from)
        AND (:to IS NULL OR p.startsAt <= :to)
      ORDER BY p.startsAt ASC
      """)
  List<PlanEntity> search(
      @Param("userId") UUID userId,
      @Param("status") PlanStatus status,
      @Param("typeId") UUID typeId,
      @Param("from") Instant from,
      @Param("to") Instant to);
}
