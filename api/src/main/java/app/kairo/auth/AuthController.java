package app.kairo.auth;

import app.kairo.config.KairoProperties;
import app.kairo.users.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final KairoProperties properties;
  private final UserProvisioningService provisioningService;
  private final KairoOAuth2UserService oAuth2UserService;
  private final HttpSessionSecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  public AuthController(
      KairoProperties properties,
      UserProvisioningService provisioningService,
      KairoOAuth2UserService oAuth2UserService) {
    this.properties = properties;
    this.provisioningService = provisioningService;
    this.oAuth2UserService = oAuth2UserService;
  }

  @GetMapping("/google")
  public void startGoogleLogin(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (properties.getAuth().isMockEnabled()) {
      UserEntity user = provisioningService.ensureMockUser();
      var principal = provisioningService.toPrincipal(user);
      var authentication = oAuth2UserService.authenticationFor(principal);
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, request, response);
      response.sendRedirect(properties.getFrontendUrl() + "/auth/callback");
      return;
    }
    response.sendRedirect("/oauth2/authorization/google");
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> me() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof KairoPrincipal principal)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(provisioningService.toMeResponse(principal));
  }
}
