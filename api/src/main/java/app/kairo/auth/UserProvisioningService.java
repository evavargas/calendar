package app.kairo.auth;

import app.kairo.config.KairoProperties;
import app.kairo.plans.PlanEntity;
import app.kairo.plans.PlanRepository;
import app.kairo.plans.PlanStatus;
import app.kairo.types.EventTypeEntity;
import app.kairo.types.EventTypeRepository;
import app.kairo.users.UserEntity;
import app.kairo.users.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProvisioningService {

  private static final List<SeedType> DEFAULT_TYPES =
      List.of(
          new SeedType("Trabajo", "#0f7a6c", 1),
          new SeedType("Personal", "#2563eb", 2),
          new SeedType("Salud", "#c2410c", 3),
          new SeedType("Estudio", "#7c3aed", 4));

  private final UserRepository userRepository;
  private final EventTypeRepository eventTypeRepository;
  private final PlanRepository planRepository;
  private final KairoProperties properties;

  public UserProvisioningService(
      UserRepository userRepository,
      EventTypeRepository eventTypeRepository,
      PlanRepository planRepository,
      KairoProperties properties) {
    this.userRepository = userRepository;
    this.eventTypeRepository = eventTypeRepository;
    this.planRepository = planRepository;
    this.properties = properties;
  }

  @Transactional
  public UserEntity upsertFromGoogle(String googleSub, String email, String name, String avatarUrl) {
    boolean isNew = userRepository.findByGoogleSub(googleSub).isEmpty();
    UserEntity user =
        userRepository
            .findByGoogleSub(googleSub)
            .map(
                existing -> {
                  existing.updateProfile(email, name, avatarUrl);
                  return existing;
                })
            .orElseGet(
                () ->
                    userRepository.save(
                        new UserEntity(UUID.randomUUID(), googleSub, email, name, avatarUrl)));

    seedTypesIfNeeded(user.getId());
    if (isNew && properties.getAuth().isMockEnabled()) {
      seedDemoPlansIfNeeded(user.getId());
    }
    return user;
  }

  @Transactional
  public UserEntity ensureMockUser() {
    return upsertFromGoogle("mock-google-sub", "eva@example.com", "Eva Estrella", "");
  }

  private void seedTypesIfNeeded(UUID userId) {
    if (eventTypeRepository.existsByUserId(userId)) {
      return;
    }
    for (SeedType seed : DEFAULT_TYPES) {
      eventTypeRepository.save(
          new EventTypeEntity(
              UUID.randomUUID(), userId, seed.name(), seed.color(), seed.sortOrder()));
    }
  }

  private void seedDemoPlansIfNeeded(UUID userId) {
    if (!planRepository.search(userId, null, null, null, null).isEmpty()) {
      return;
    }
    List<EventTypeEntity> types = eventTypeRepository.findByUserIdOrderBySortOrderAsc(userId);
    if (types.isEmpty()) {
      return;
    }
    Instant tomorrowNine = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(33, ChronoUnit.HOURS);
    Instant tomorrowTen = tomorrowNine.plus(1, ChronoUnit.HOURS);
    Instant studyStart = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(42, ChronoUnit.HOURS);
    Instant studyEnd = studyStart.plus(1, ChronoUnit.HOURS);
    Instant healthStart = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS);
    Instant healthEnd = healthStart.plus(1, ChronoUnit.HOURS);

    planRepository.save(
        new PlanEntity(
            UUID.randomUUID(),
            userId,
            types.get(0).getId(),
            "Kickoff del sprint",
            "Definir alcance de la demo Kairo y criterios de listo.",
            tomorrowNine,
            tomorrowTen,
            false,
            PlanStatus.planned));
    planRepository.save(
        new PlanEntity(
            UUID.randomUUID(),
            userId,
            types.size() > 3 ? types.get(3).getId() : types.get(0).getId(),
            "Repaso Spring Security",
            "OAuth2 Login + tokens para el backend Java.",
            studyStart,
            studyEnd,
            false,
            PlanStatus.planned));
    planRepository.save(
        new PlanEntity(
            UUID.randomUUID(),
            userId,
            types.size() > 2 ? types.get(2).getId() : types.get(0).getId(),
            "Entrenamiento",
            "Sesión de fuerza 45 min.",
            healthStart,
            healthEnd,
            false,
            PlanStatus.done));
  }

  public KairoPrincipal toPrincipal(UserEntity user) {
    return new KairoPrincipal(user.getId(), user.getEmail(), user.getName(), user.getAvatarUrl());
  }

  public Map<String, Object> toMeResponse(KairoPrincipal principal) {
    return Map.of(
        "id", principal.id().toString(),
        "email", principal.email(),
        "name", principal.name(),
        "avatarUrl", principal.avatarUrl() == null ? "" : principal.avatarUrl(),
        "demoMode", properties.getAuth().isMockEnabled());
  }

  private record SeedType(String name, String color, int sortOrder) {}
}
