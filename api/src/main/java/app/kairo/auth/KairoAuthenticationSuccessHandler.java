package app.kairo.auth;

import app.kairo.config.KairoProperties;
import app.kairo.users.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KairoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final KairoProperties properties;
  private final UserProvisioningService provisioningService;
  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  public KairoAuthenticationSuccessHandler(
      KairoProperties properties, UserProvisioningService provisioningService) {
    this.properties = properties;
    this.provisioningService = provisioningService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
      KairoPrincipal principal = resolvePrincipal(oauthUser.getAttributes());
      Authentication sessionAuth =
          new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(sessionAuth);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, request, response);
    }
    response.sendRedirect(properties.getFrontendUrl() + "/auth/callback");
  }

  private KairoPrincipal resolvePrincipal(Map<String, Object> attrs) {
    Object userId = attrs.get("userId");
    if (userId != null && StringUtils.hasText(String.valueOf(userId)) && !"null".equals(String.valueOf(userId))) {
      return new KairoPrincipal(
          UUID.fromString(String.valueOf(userId)),
          String.valueOf(attrs.getOrDefault("email", "")),
          String.valueOf(attrs.getOrDefault("name", "")),
          String.valueOf(attrs.getOrDefault("avatarUrl", attrs.getOrDefault("picture", ""))));
    }

    String googleSub = String.valueOf(attrs.get("sub"));
    String email = String.valueOf(attrs.getOrDefault("email", ""));
    String name = String.valueOf(attrs.getOrDefault("name", email));
    String avatar =
        String.valueOf(attrs.getOrDefault("picture", attrs.getOrDefault("avatarUrl", "")));
    UserEntity user = provisioningService.upsertFromGoogle(googleSub, email, name, avatar);
    return provisioningService.toPrincipal(user);
  }
}
