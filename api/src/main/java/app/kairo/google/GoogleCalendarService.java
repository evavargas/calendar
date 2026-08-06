package app.kairo.google;

import app.kairo.auth.CurrentUser;
import app.kairo.auth.KairoPrincipal;
import app.kairo.common.ApiException;
import app.kairo.common.TokenEncryptionService;
import app.kairo.config.KairoProperties;
import app.kairo.plans.PlanEntity;
import app.kairo.plans.PlanRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleCalendarService {

  static final String SESSION_STATE_KEY = "KAIRO_GOOGLE_OAUTH_STATE";

  private final GoogleConnectionRepository connectionRepository;
  private final GoogleEventLinkRepository linkRepository;
  private final PlanRepository planRepository;
  private final KairoProperties properties;
  private final TokenEncryptionService encryptionService;
  private final GoogleCalendarClient calendarClient;

  public GoogleCalendarService(
      GoogleConnectionRepository connectionRepository,
      GoogleEventLinkRepository linkRepository,
      PlanRepository planRepository,
      KairoProperties properties,
      TokenEncryptionService encryptionService,
      GoogleCalendarClient calendarClient) {
    this.connectionRepository = connectionRepository;
    this.linkRepository = linkRepository;
    this.planRepository = planRepository;
    this.properties = properties;
    this.encryptionService = encryptionService;
    this.calendarClient = calendarClient;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> status() {
    KairoPrincipal user = CurrentUser.require();
    return connectionRepository
        .findById(user.id())
        .map(
            connection ->
                Map.<String, Object>of(
                    "connected",
                    true,
                    "scopes",
                    Arrays.stream(connection.getScopes().split(" "))
                        .filter(s -> !s.isBlank())
                        .toList()))
        .orElse(Map.of("connected", false, "scopes", List.of()));
  }

  public String beginConnect(HttpSession session) {
    KairoPrincipal user = CurrentUser.require();
    if (properties.getAuth().isMockEnabled()) {
      return null;
    }
    requireGoogleConfig();
    String state = user.id() + ":" + UUID.randomUUID();
    session.setAttribute(SESSION_STATE_KEY, state);
    return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
        .queryParam("client_id", properties.getGoogle().getClientId())
        .queryParam("redirect_uri", properties.getGoogle().getCalendarRedirectUri())
        .queryParam("response_type", "code")
        .queryParam("scope", properties.getGoogle().getCalendarScope())
        .queryParam("access_type", "offline")
        .queryParam("prompt", "consent")
        .queryParam("include_granted_scopes", "true")
        .queryParam("state", state)
        .build(true)
        .toUriString();
  }

  @Transactional
  public Map<String, Object> connectMock() {
    KairoPrincipal user = CurrentUser.require();
    if (!properties.getAuth().isMockEnabled()) {
      throw ApiException.badRequest("Mock de Google Calendar deshabilitado.");
    }
    String encrypted = encryptionService.encrypt("mock-refresh-token");
    GoogleConnectionEntity connection =
        connectionRepository
            .findById(user.id())
            .map(
                existing -> {
                  existing.reconnect(encrypted, properties.getGoogle().getCalendarScope());
                  return existing;
                })
            .orElseGet(
                () ->
                    new GoogleConnectionEntity(
                        user.id(), encrypted, properties.getGoogle().getCalendarScope()));
    connectionRepository.save(connection);
    return status();
  }

  @Transactional
  public void handleOAuthCallback(String code, String state, HttpSession session) {
    requireGoogleConfig();
    Object expected = session.getAttribute(SESSION_STATE_KEY);
    if (expected == null || !expected.toString().equals(state) || !state.contains(":")) {
      throw ApiException.badRequest("State OAuth inválido o expirado");
    }
    session.removeAttribute(SESSION_STATE_KEY);
    UUID userId = UUID.fromString(state.substring(0, state.indexOf(':')));

    Map<String, Object> tokens = calendarClient.exchangeAuthorizationCode(code);
    String refreshToken = String.valueOf(tokens.get("refresh_token"));
    String scope =
        String.valueOf(tokens.getOrDefault("scope", properties.getGoogle().getCalendarScope()));
    String encrypted = encryptionService.encrypt(refreshToken);

    GoogleConnectionEntity connection =
        connectionRepository
            .findById(userId)
            .map(
                existing -> {
                  existing.reconnect(encrypted, scope);
                  return existing;
                })
            .orElseGet(() -> new GoogleConnectionEntity(userId, encrypted, scope));
    connectionRepository.save(connection);
  }

  @Transactional
  public Map<String, Object> disconnect() {
    KairoPrincipal user = CurrentUser.require();
    connectionRepository.deleteById(user.id());
    return Map.of("connected", false, "scopes", List.of());
  }

  @Transactional
  public Map<String, Object> pushPlan(UUID planId) {
    KairoPrincipal user = CurrentUser.require();
    GoogleConnectionEntity connection =
        connectionRepository
            .findById(user.id())
            .orElseThrow(() -> ApiException.badRequest("Conectá Google Calendar primero."));
    PlanEntity plan =
        planRepository
            .findByIdAndUserId(planId, user.id())
            .orElseThrow(() -> ApiException.notFound("Plan no encontrado"));

    if (properties.getAuth().isMockEnabled()) {
      String googleEventId = "kairo-mock-" + plan.getId();
      linkRepository.save(new GoogleEventLinkEntity(plan.getId(), googleEventId, "primary"));
      return Map.of(
          "planId",
          plan.getId().toString(),
          "googleEventId",
          googleEventId,
          "htmlLink",
          "https://calendar.google.com/",
          "scopes",
          connection.getScopes());
    }

    requireGoogleConfig();
    String existingId =
        linkRepository.findById(plan.getId()).map(GoogleEventLinkEntity::getGoogleEventId).orElse(null);
    GoogleCalendarClient.EventPushResult result =
        calendarClient.createOrUpdateEvent(connection.getRefreshTokenEnc(), plan, existingId);
    linkRepository.save(new GoogleEventLinkEntity(plan.getId(), result.eventId(), "primary"));
    return Map.of(
        "planId",
        plan.getId().toString(),
        "googleEventId",
        result.eventId(),
        "htmlLink",
        result.htmlLink() == null ? "https://calendar.google.com/" : result.htmlLink(),
        "scopes",
        connection.getScopes());
  }

  private void requireGoogleConfig() {
    if (properties.getGoogle().getClientId() == null
        || properties.getGoogle().getClientId().isBlank()
        || properties.getGoogle().getClientSecret() == null
        || properties.getGoogle().getClientSecret().isBlank()) {
      throw ApiException.badRequest("Faltan GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET.");
    }
  }
}
