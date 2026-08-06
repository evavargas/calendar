package app.kairo.auth;

import app.kairo.config.KairoProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class KairoAuthenticationFailureHandler implements AuthenticationFailureHandler {

  private static final Logger log = LoggerFactory.getLogger(KairoAuthenticationFailureHandler.class);

  private final KairoProperties properties;

  public KairoAuthenticationFailureHandler(KairoProperties properties) {
    this.properties = properties;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    log.warn("Google OAuth login failed: {}", exception.getMessage());
    String message =
        URLEncoder.encode(
            exception.getMessage() == null ? "oauth_failed" : exception.getMessage(),
            StandardCharsets.UTF_8);
    response.sendRedirect(properties.getFrontendUrl() + "/?authError=" + message);
  }
}
