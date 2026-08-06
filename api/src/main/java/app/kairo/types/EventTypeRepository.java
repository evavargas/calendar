package app.kairo.types;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTypeRepository extends JpaRepository<EventTypeEntity, UUID> {
  List<EventTypeEntity> findByUserIdOrderBySortOrderAsc(UUID userId);

  Optional<EventTypeEntity> findByIdAndUserId(UUID id, UUID userId);

  boolean existsByUserId(UUID userId);

  long countByIdAndUserId(UUID id, UUID userId);
}
