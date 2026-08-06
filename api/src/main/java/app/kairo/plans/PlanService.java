package app.kairo.plans;

import app.kairo.auth.CurrentUser;
import app.kairo.auth.KairoPrincipal;
import app.kairo.common.ApiException;
import app.kairo.types.EventTypeRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PlanService {

  private final PlanRepository planRepository;
  private final EventTypeRepository eventTypeRepository;
  private final IcsExportService icsExportService;

  public PlanService(
      PlanRepository planRepository,
      EventTypeRepository eventTypeRepository,
      IcsExportService icsExportService) {
    this.planRepository = planRepository;
    this.eventTypeRepository = eventTypeRepository;
    this.icsExportService = icsExportService;
  }

  @Transactional(readOnly = true)
  public List<PlanResponse> list(String status, UUID typeId, Instant from, Instant to) {
    KairoPrincipal user = CurrentUser.require();
    PlanStatus parsedStatus = parseStatus(status);
    return planRepository.search(user.id(), parsedStatus, typeId, from, to).stream()
        .map(PlanResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public PlanResponse get(UUID id) {
    return PlanResponse.from(requireOwned(id));
  }

  @Transactional
  public PlanResponse create(CreatePlanRequest request) {
    KairoPrincipal user = CurrentUser.require();
    String title = requireTrimmed(request.title(), "title");
    String description = normalizeDescription(request.description());
    validateType(user.id(), request.typeId());
    validateRange(request.startsAt(), request.endsAt());
    PlanEntity entity =
        new PlanEntity(
            UUID.randomUUID(),
            user.id(),
            request.typeId(),
            title,
            description,
            request.startsAt(),
            request.endsAt(),
            Boolean.TRUE.equals(request.allDay()),
            PlanStatus.planned);
    return PlanResponse.from(planRepository.save(entity));
  }

  @Transactional
  public PlanResponse update(UUID id, UpdatePlanRequest request) {
    KairoPrincipal user = CurrentUser.require();
    PlanEntity entity = requireOwned(id);
    if (request.typeId() != null) {
      validateType(user.id(), request.typeId());
    }
    String title = null;
    if (request.title() != null) {
      title = requireTrimmed(request.title(), "title");
    }
    String description =
        request.description() == null ? null : normalizeDescription(request.description());
    Instant starts = request.startsAt() != null ? request.startsAt() : entity.getStartsAt();
    Instant ends = request.endsAt() != null ? request.endsAt() : entity.getEndsAt();
    validateRange(starts, ends);
    entity.apply(
        request.typeId(),
        title,
        description,
        request.startsAt(),
        request.endsAt(),
        request.allDay(),
        request.status());
    return PlanResponse.from(entity);
  }

  @Transactional
  public void delete(UUID id) {
    planRepository.delete(requireOwned(id));
  }

  @Transactional(readOnly = true)
  public byte[] exportOneIcs(UUID id) {
    return icsExportService.export(List.of(requireOwned(id)));
  }

  @Transactional(readOnly = true)
  public byte[] exportFilteredIcs(String status, UUID typeId, Instant from, Instant to) {
    KairoPrincipal user = CurrentUser.require();
    List<PlanEntity> plans =
        planRepository.search(user.id(), parseStatus(status), typeId, from, to);
    return icsExportService.export(plans);
  }

  private PlanEntity requireOwned(UUID id) {
    KairoPrincipal user = CurrentUser.require();
    return planRepository
        .findByIdAndUserId(id, user.id())
        .orElseThrow(() -> ApiException.notFound("Plan no encontrado"));
  }

  private void validateType(UUID userId, UUID typeId) {
    eventTypeRepository
        .findByIdAndUserId(typeId, userId)
        .orElseThrow(() -> ApiException.badRequest("Tipo inválido"));
  }

  private void validateRange(Instant startsAt, Instant endsAt) {
    if (startsAt == null || endsAt == null) {
      throw ApiException.badRequest("Inicio y fin son obligatorios.");
    }
    if (endsAt.isBefore(startsAt)) {
      throw ApiException.badRequest("El fin no puede ser anterior al inicio.");
    }
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

  private static String normalizeDescription(String description) {
    if (description == null) {
      return "";
    }
    return description.trim();
  }

  private PlanStatus parseStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    try {
      return PlanStatus.valueOf(status.trim());
    } catch (IllegalArgumentException ex) {
      throw ApiException.badRequest("Estado inválido");
    }
  }

  public record CreatePlanRequest(
      @NotBlank @Size(max = 160) String title,
      @NotNull UUID typeId,
      @Size(max = 4000) String description,
      @NotNull Instant startsAt,
      @NotNull Instant endsAt,
      Boolean allDay) {}

  public record UpdatePlanRequest(
      @Size(max = 160) String title,
      UUID typeId,
      @Size(max = 4000) String description,
      Instant startsAt,
      Instant endsAt,
      Boolean allDay,
      PlanStatus status) {}

  public record PlanResponse(
      String id,
      String typeId,
      String title,
      String description,
      String startsAt,
      String endsAt,
      boolean allDay,
      String status,
      String createdAt,
      String updatedAt) {
    static PlanResponse from(PlanEntity entity) {
      return new PlanResponse(
          entity.getId().toString(),
          entity.getTypeId().toString(),
          entity.getTitle(),
          entity.getDescription() == null ? "" : entity.getDescription(),
          entity.getStartsAt().toString(),
          entity.getEndsAt().toString(),
          entity.isAllDay(),
          entity.getStatus().name(),
          entity.getCreatedAt().toString(),
          entity.getUpdatedAt().toString());
    }
  }
}
