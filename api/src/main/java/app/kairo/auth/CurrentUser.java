package app.kairo.auth;

import app.kairo.common.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

  private CurrentUser() {}

  public static KairoPrincipal require() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof KairoPrincipal principal)) {
      throw ApiException.unauthorized("No hay sesión activa");
    }
    return principal;
  }
}
