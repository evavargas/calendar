package app.kairo.types;

import app.kairo.auth.CurrentUser;
import app.kairo.auth.KairoPrincipal;
import app.kairo.common.ApiException;
import app.kairo.plans.PlanRepository;
import app.kairo.validation.HexColor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EventTypeService {

  private static final String DEFAULT_COLOR = "#0f7a6c";

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
    String name = requireTrimmed(request.name(), "name");
    String color = normalizeColor(request.color(), true);
    int sortOrder = repository.findByUserIdOrderBySortOrderAsc(user.id()).size() + 1;
    EventTypeEntity entity =
        new EventTypeEntity(UUID.randomUUID(), user.id(), name, color, sortOrder);
    return EventTypeResponse.from(repository.save(entity));
  }

  @Transactional
  public EventTypeResponse update(UUID id, UpdateEventTypeRequest request) {
    KairoPrincipal user = CurrentUser.require();
    EventTypeEntity entity =
        repository
            .findByIdAndUserId(id, user.id())
            .orElseThrow(() -> ApiException.notFound("Tipo no encontrado"));
    String name = request.name() == null ? null : requireTrimmed(request.name(), "name");
    String color = request.color() == null ? null : normalizeColor(request.color(), false);
    String icon =
        request.icon() == null
            ? null
            : (request.icon().isBlank() ? "" : request.icon().trim());
    if (request.sortOrder() != null && request.sortOrder() < 0) {
      throw ApiException.badRequest("sortOrder: debe ser >= 0");
    }
    entity.update(name, color, icon, request.sortOrder());
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

  private static String requireTrimmed(String value, String field) {
    if (!StringUtils.hasText(value)) {
      throw ApiException.badRequest(field + ": no puede estar vacío");
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw ApiException.badRequest(field + ": no puede estar vacío");
    }
    return trimmed;
  }

  private static String normalizeColor(String color, boolean allowDefault) {
    if (!StringUtils.hasText(color)) {
      if (allowDefault) {
        return DEFAULT_COLOR;
      }
      throw ApiException.badRequest("color: no puede estar vacío");
    }
    String trimmed = color.trim();
    if (!trimmed.matches("^#[0-9A-Fa-f]{6}$")) {
      throw ApiException.badRequest("color: debe ser un color hex (#RRGGBB)");
    }
    return trimmed.toLowerCase();
  }

  public record CreateEventTypeRequest(
      @NotBlank @Size(max = 80) String name,
      @NotBlank @HexColor @Size(max = 16) String color) {}

  public record UpdateEventTypeRequest(
      @Size(max = 80) String name,
      @HexColor @Size(max = 16) String color,
      @Size(max = 40) String icon,
      @Min(0) @Max(10_000) Integer sortOrder) {}

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
