package app.kairo.google;

import app.kairo.common.ApiException;
import app.kairo.common.TokenEncryptionService;
import app.kairo.config.KairoProperties;
import app.kairo.plans.PlanEntity;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GoogleCalendarClient {

  private static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

  private final KairoProperties properties;
  private final TokenEncryptionService encryptionService;
  private final RestClient restClient = RestClient.create();

  public GoogleCalendarClient(
      KairoProperties properties, TokenEncryptionService encryptionService) {
    this.properties = properties;
    this.encryptionService = encryptionService;
  }

  public Map<String, Object> exchangeAuthorizationCode(String code) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("code", code);
    form.add("client_id", properties.getGoogle().getClientId());
    form.add("client_secret", properties.getGoogle().getClientSecret());
    form.add("redirect_uri", properties.getGoogle().getCalendarRedirectUri());
    form.add("grant_type", "authorization_code");

    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        restClient
            .post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map.class);

    if (response == null || response.get("refresh_token") == null) {
      throw ApiException.badRequest(
          "Google no devolvió refresh_token. Revocá el acceso de la app y volvé a conectar con consent.");
    }
    return response;
  }

  public EventPushResult createOrUpdateEvent(
      String encryptedRefreshToken, PlanEntity plan, String existingEventId) {
    try {
      String refreshToken = encryptionService.decrypt(encryptedRefreshToken);
      UserCredentials credentials =
          UserCredentials.newBuilder()
              .setClientId(properties.getGoogle().getClientId())
              .setClientSecret(properties.getGoogle().getClientSecret())
              .setRefreshToken(refreshToken)
              .build();

      Calendar calendar =
          new Calendar.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(),
                  GsonFactory.getDefaultInstance(),
                  new HttpCredentialsAdapter(credentials))
              .setApplicationName("Kairo")
              .build();

      Event event = toGoogleEvent(plan);
      Event saved;
      if (existingEventId == null || existingEventId.isBlank()) {
        saved = calendar.events().insert("primary", event).execute();
      } else {
        saved = calendar.events().update("primary", existingEventId, event).execute();
      }
      return new EventPushResult(saved.getId(), saved.getHtmlLink());
    } catch (GeneralSecurityException | IOException ex) {
      throw new ApiException(
          org.springframework.http.HttpStatus.BAD_GATEWAY,
          "google_api_error",
          "No se pudo sincronizar con Google Calendar: " + ex.getMessage());
    }
  }

  private Event toGoogleEvent(PlanEntity plan) {
    Event event =
        new Event()
            .setSummary(plan.getTitle())
            .setDescription(plan.getDescription() == null ? "" : plan.getDescription());

    if (plan.isAllDay()) {
      event.setStart(new EventDateTime().setDate(new DateTime(DATE.format(plan.getStartsAt()))));
      event.setEnd(new EventDateTime().setDate(new DateTime(DATE.format(plan.getEndsAt()))));
    } else {
      event.setStart(new EventDateTime().setDateTime(toGoogleDateTime(plan.getStartsAt())));
      event.setEnd(new EventDateTime().setDateTime(toGoogleDateTime(plan.getEndsAt())));
    }
    return event;
  }

  private DateTime toGoogleDateTime(Instant instant) {
    return new DateTime(instant.toEpochMilli());
  }

  public record EventPushResult(String eventId, String htmlLink) {}
}
