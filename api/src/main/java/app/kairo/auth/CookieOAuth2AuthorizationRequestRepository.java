package app.kairo.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Persists the OAuth2 authorization request in a browser cookie so the Google round-trip works on
 * Cloud Run without relying on in-memory HTTP sessions.
 */
@Component
public class CookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  static final String COOKIE_NAME = "KAIRO_OAUTH2_AUTH_REQUEST";
  private static final Duration MAX_AGE = Duration.ofMinutes(3);

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    String value = readCookie(request, COOKIE_NAME);
    if (!StringUtils.hasText(value)) {
      return null;
    }
    try {
      return deserialize(value);
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      clearCookie(request, response);
      return;
    }
    writeCookie(request, response, serialize(authorizationRequest), MAX_AGE);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
    clearCookie(request, response);
    return authorizationRequest;
  }

  private void clearCookie(HttpServletRequest request, HttpServletResponse response) {
    writeCookie(request, response, "", Duration.ZERO);
  }

  private void writeCookie(
      HttpServletRequest request, HttpServletResponse response, String value, Duration maxAge) {
    // Host-only: OAuth must start and finish on the same public origin (Vercel).
    ResponseCookie cookie =
        ResponseCookie.from(COOKIE_NAME, value)
            .path("/")
            .httpOnly(true)
            .secure(
                request.isSecure()
                    || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto")))
            .sameSite("Lax")
            .maxAge(maxAge)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private static String readCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private static String serialize(OAuth2AuthorizationRequest authorizationRequest) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        oos.writeObject(authorizationRequest);
      }
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray());
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to serialize OAuth2 authorization request", ex);
    }
  }

  private static OAuth2AuthorizationRequest deserialize(String value) throws Exception {
    byte[] bytes = Base64.getUrlDecoder().decode(value);
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return (OAuth2AuthorizationRequest) ois.readObject();
    }
  }
}
