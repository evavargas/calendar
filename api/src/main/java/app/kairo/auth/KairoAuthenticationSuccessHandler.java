package app.kairo.auth;

import app.kairo.config.KairoProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class KairoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final KairoProperties properties;
  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  public KairoAuthenticationSuccessHandler(KairoProperties properties) {
    this.properties = properties;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
      Map<String, Object> attrs = oauthUser.getAttributes();
      KairoPrincipal principal =
          new KairoPrincipal(
              UUID.fromString(String.valueOf(attrs.get("userId"))),
              String.valueOf(attrs.get("email")),
              String.valueOf(attrs.get("name")),
              String.valueOf(attrs.getOrDefault("avatarUrl", "")));
      Authentication sessionAuth =
          new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
              principal, null, principal.authorities());
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(sessionAuth);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, request, response);
    }
    response.sendRedirect(properties.getFrontendUrl() + "/auth/callback");
  }
}
