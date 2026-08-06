package app.kairo.types;

import app.kairo.auth.CurrentUser;
import app.kairo.auth.KairoPrincipal;
import app.kairo.common.ApiException;
import app.kairo.plans.PlanRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventTypeService {

  private final EventTypeRepository repository;
  private final PlanRepository planRepository;

  public EventTypeService(EventTypeRepository repository, PlanRepository planRepository) {
    this.repository = repository;
    this.planRepository = planRepository;
  }

  @Transactional(readOnly = true)
  public List<EventTypeResponse> list() {
    KairoPrincipal user = CurrentUser.require();
    return repository.findByUserIdOrderBySortOrderAsc(user.id()).stream()
        .map(EventTypeResponse::from)
        .toList();
  }

  @Transactional
  public EventTypeResponse create(CreateEventTypeRequest request) {
    KairoPrincipal user = CurrentUser.require();
    int sortOrder = repository.findByUserIdOrderBySortOrderAsc(user.id()).size() + 1;
    EventTypeEntity entity =
        new EventTypeEntity(
            UUID.randomUUID(),
            user.id(),
            request.name().trim(),
            request.color() == null || request.color().isBlank() ? "#0f7a6c" : request.color(),
            sortOrder);
    return EventTypeResponse.from(repository.save(entity));
  }

  @Transactional
  public EventTypeResponse update(UUID id, UpdateEventTypeRequest request) {
    KairoPrincipal user = CurrentUser.require();
    EventTypeEntity entity =
        repository
            .findByIdAndUserId(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Tipo no encontrado"));
    entity.update(request.name(), request.color(), request.icon(), request.sortOrder());
    return EventTypeResponse.from(entity);
  }

  @Transactional
  public void delete(UUID id) {
    KairoPrincipal user = CurrentUser.require();
    EventTypeEntity entity =
        repository
            .findByIdAndUserId(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Tipo no encontrado"));
    if (planRepository.existsByTypeIdAndUserId(id, user.id())) {
      throw ApiException.conflict("No se puede borrar un tipo con planes asociados.");
    }
    repository.delete(entity);
  }

  public record CreateEventTypeRequest(
      @NotBlank @Size(max = 80) String name, @Size(max = 16) String color) {}

  public record UpdateEventTypeRequest(
      @Size(max = 80) String name,
      @Size(max = 16) String color,
      @Size(max = 40) String icon,
      Integer sortOrder) {}

  public record EventTypeResponse(
      String id, String name, String color, String icon, int sortOrder) {
    static EventTypeResponse from(EventTypeEntity entity) {
      return new EventTypeResponse(
          entity.getId().toString(),
          entity.getName(),
          entity.getColor(),
          entity.getIcon(),
          entity.getSortOrder());
    }
  }
}
